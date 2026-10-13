import { useEffect, useState } from 'react';
import { apiRequest } from '../api.js';
import MedioPago from './MedioPago.jsx';
import PanelBarbero from './PanelBarbero.jsx';
import Reseña from './Resena.jsx';
import Servicios from './Servicios.jsx';
import { COP, IMAGENES_SERVICIOS, METODOS_PAGO } from '../data.js';

// Estado inicial inmutable del formulario de reserva
const FORMULARIO_VACIO = {
  cliente: '',
  telefono: '',
  correo: '',
  servicioId: '',
  barberoId: '',
  fechaHora: '',
  metodoPago: '',
};

/**
 * Función auxiliar para convertir una fecha de JavaScript al formato requerido
 * por los inputs de tipo `datetime-local` (YYYY-MM-DDTHH:mm), ajustando la zona horaria local.
 *
 * @param {Date} fecha - Objeto Date a formatear.
 * @returns {string} Fecha formateada en la zona horaria local.
 */
function fechaHoraLocal(fecha) {
  const desplazamiento = fecha.getTimezoneOffset() * 60_000;
  return new Date(fecha.getTime() - desplazamiento).toISOString().slice(0, 16);
}

/**
 * Calcula el próximo bloque de horario disponible redondeando hacia el
 * intervalo más cercano de 30 minutos a partir de la hora actual.
 *
 * @returns {string} Fecha y hora formateadas para el atributo `min` del input datetime.
 */
function siguienteHorarioDisponible() {
  const siguiente = new Date();
  siguiente.setSeconds(0, 0);
  siguiente.setMinutes(Math.ceil((siguiente.getMinutes() + 1) / 30) * 30);
  return fechaHoraLocal(siguiente);
}

/**
 * Mapea/Adapta la entidad de servicio recibida de la API REST al DTO utilizado por la UI.
 * Asigna una imagen por defecto mediante el diccionario si la API no provee una.
 *
 * @param {Object} service - Objeto del servicio en la estructura de la API.
 * @returns {Object} Objeto de servicio adaptado para el componente `Servicios`.
 */
function adaptarServicio(service) {
  return {
    id: service.id,
    nombre: service.name,
    descripcion: service.description,
    precio: Number(service.price),
    duracion: service.durationMinutes,
    imagen: IMAGENES_SERVICIOS[service.name] ?? IMAGENES_SERVICIOS['Fade clásico'],
  };
}

/**
 * Componente Principal `AgendarCita`
 * Orquesta la página landing principal de la barbería "El Caballero", el formulario interactivo
 * de reservas y la alternancia de vista con el panel administrativo para los barberos.
 */
export default function AgendarCita() {
  // Estados de apariencia y navegación
  const [temaOscuro, setTemaOscuro] = useState(true);
  const [vistaPanel, setVistaPanel] = useState(false); // Alterna entre la Landing Page y el Panel Administrativo

  // Estados de datos remotos (Catálogos)
  const [servicioSeleccionado, setServicioSeleccionado] = useState('');
  const [servicios, setServicios] = useState([]);
  const [barberos, setBarberos] = useState([]);
  const [cargandoCatalogo, setCargandoCatalogo] = useState(true);
  const [errorCatalogo, setErrorCatalogo] = useState('');

  // Estados del formulario y reserva
  const [formulario, setFormulario] = useState(FORMULARIO_VACIO);
  const [confirmacion, setConfirmacion] = useState('');
  const [errorReserva, setErrorReserva] = useState('');
  const [reservando, setReservando] = useState(false);
  const [fechaMinima, setFechaMinima] = useState(siguienteHorarioDisponible);

  /**
   * Carga inicial de catálogos (Servicios y Barberos) mediante llamadas HTTP paralelas.
   * Utiliza `AbortController` para cancelar la petición en caso de desmontaje del componente.
   */
  useEffect(() => {
    const controller = new AbortController();
    
    Promise.all([
      apiRequest('/services', { signal: controller.signal }),
      apiRequest('/public/barbers', { signal: controller.signal }),
    ])
      .then(([serviceList, barberList]) => {
        // Filtra solo los servicios activos y aplica el adaptador de datos
        setServicios(serviceList.filter((service) => service.active).map(adaptarServicio));
        setBarberos(barberList);
        setErrorCatalogo('');
      })
      .catch((error) => {
        if (error.name !== 'AbortError') setErrorCatalogo(error.message);
      })
      .finally(() => {
        if (!controller.signal.aborted) setCargandoCatalogo(false);
      });

    return () => controller.abort();
  }, []);

  /**
   * Escucha la selección rápida de un servicio desde las tarjetas del catálogo
   * y desplaza suavemente al usuario (*smooth scroll*) hacia el formulario de reserva.
   */
  useEffect(() => {
    if (!servicioSeleccionado) return;
    setFormulario((actual) => ({ ...actual, servicioId: servicioSeleccionado }));
    document.getElementById('agendar')?.scrollIntoView({ behavior: 'smooth' });
  }, [servicioSeleccionado]);

  /**
   * Manejador genérico para el cambio de valores en las entradas del formulario.
   */
  function actualizar(evento) {
    setFormulario((actual) => ({ ...actual, [evento.target.name]: evento.target.value }));
    setConfirmacion('');
    setErrorReserva('');
  }

  /**
   * Procesa y valida el envío del formulario de reserva hacia el servidor.
   */
  async function reservar(evento) {
    evento.preventDefault();
    
    const service = servicios.find((item) => item.id === Number(formulario.servicioId));
    const barber = barberos.find((item) => item.id === Number(formulario.barberoId));
    const start = new Date(formulario.fechaHora);

    // Validaciones de integridad previa al envío
    if (!service || !barber || Number.isNaN(start.getTime())) {
      setErrorReserva('Selecciona un servicio, barbero y horario válidos.');
      return;
    }
    if (start <= new Date()) {
      setErrorReserva('La fecha de la cita debe ser posterior a la hora actual.');
      return;
    }

    // Descomposición del nombre en Nombre y Apellidos para cumplir con el esquema del backend
    const [firstName, ...lastNameParts] = formulario.cliente.trim().split(/\s+/);
    const [appointmentDate, startTime] = formulario.fechaHora.split('T');

    setReservando(true);
    setErrorReserva('');

    try {
      const appointment = await apiRequest('/public/appointments', {
        method: 'POST',
        body: {
          firstName,
          lastName: lastNameParts.join(' '),
          email: formulario.correo,
          phone: formulario.telefono,
          barberId: barber.id,
          appointmentDate,
          startTime,
          notes: '',
          serviceIds: [service.id],
          paymentMethod: formulario.metodoPago,
        },
      });

      // Limpieza de estados tras confirmación exitosa
      setFormulario(FORMULARIO_VACIO);
      setServicioSeleccionado('');
      setFechaMinima(siguienteHorarioDisponible());
      setConfirmacion(
        `¡Listo, ${appointment.clientName}! Tu cita #${appointment.id} para ${service.nombre} quedó pendiente de confirmación.`
      );
    } catch (error) {
      setErrorReserva(error.message);
    } finally {
      setReservando(false);
    }
  }

  /**
   * Alterna la vista entre la landing page del cliente y el panel interno para barberos.
   */
  function alternarVista() {
    setVistaPanel((actual) => !actual);
    setConfirmacion('');
    setErrorReserva('');
  }

  return (
    <div className={`app-shell ${temaOscuro ? 'theme-dark' : 'theme-light'}`}>
      
      {/* Encabezado del sitio web y navegación principal */}
      <header className="site-header">
        <a className="brand" href="#inicio" onClick={() => setVistaPanel(false)} aria-label="El Caballero, inicio">
          <span className="brand-mark" aria-hidden="true">C.</span>
          <span><strong>EL CABALLERO</strong><small>BARBERÍA · ARMENIA</small></span>
        </a>

        <nav className="main-nav" aria-label="Navegación principal">
          {!vistaPanel && (
            <>
              <a href="#servicios">Servicios</a>
              <a href="#resenas">Reseñas</a>
              <a href="#pagos">Medios de pago</a>
              <a className="nav-book" href="#agendar">
                Reservar <span aria-hidden="true">↗</span>
              </a>
            </>
          )}

          {/* Switch para el modo oscuro / claro */}
          <button 
            className="theme-toggle" 
            type="button" 
            onClick={() => setTemaOscuro((actual) => !actual)} 
            aria-label={temaOscuro ? 'Activar modo claro' : 'Activar modo oscuro'}
          >
            <span aria-hidden="true">{temaOscuro ? '☼' : '☾'}</span> {temaOscuro ? 'Modo claro' : 'Modo oscuro'}
          </button>

          {/* Enlace para conmutar la vista del Panel de Staff */}
          <button className="staff-link" type="button" onClick={alternarVista}>
            {vistaPanel ? '← Volver al sitio' : 'Panel barbero'}
          </button>
        </nav>
      </header>

      {/* Renderizado condicional: Panel Administrativo vs Landing Page Comercial */}
      {vistaPanel ? (
        <PanelBarbero />
      ) : (
        <>
          {/* Banner de alertas globales para errores del catálogo o de reservas */}
          {(errorCatalogo || errorReserva) && (
            <div className="connection-notice" role="alert">
              <span>{errorReserva || errorCatalogo}</span>
              <button 
                type="button" 
                aria-label="Cerrar aviso" 
                onClick={() => { setErrorReserva(''); setErrorCatalogo(''); }}
              >
                ×
              </button>
            </div>
          )}

          {/* Hero / Presentación de la barbería */}
          <section className="hero" id="inicio">
            <div className="hero-image" aria-hidden="true" />
            <div className="hero-content">
              <span className="eyebrow"><span className="eyebrow-dot" /> DESDE 2024 · ARMENIA, QUINDÍO</span>
              <h1>El arte de un<br />buen <span className="serif-accent">corte.</span></h1>
              <p>Buena conversación, un corte preciso y ese momento para ti que siempre hace falta.</p>
              <a className="button button-primary" href="#agendar">Encuentra tu momento <span aria-hidden="true">↗</span></a>
              <div className="hero-note"><span>01</span> Cuidamos cada detalle.</div>
            </div>
            <div className="hero-side-label">EL ESTILO SE SIENTE</div>
            <div className="hero-caption">CLÁSICOS DE SIEMPRE. ACTITUD DE HOY.</div>
          </section>

          {/* Estado de carga de catálogos y renderizado de componentes presentacionales */}
          {cargandoCatalogo ? (
            <p className="catalog-state" role="status">Cargando servicios y barberos…</p>
          ) : (
            <Servicios servicios={servicios} alReservar={setServicioSeleccionado} />
          )}

          <Reseña />
          <MedioPago />

          {/* Sección y formulario de agendamiento de citas */}
          <section className="section booking-section" id="agendar">
            <div className="container booking-layout">
              <div className="booking-copy">
                <span className="eyebrow">Tu próxima visita empieza aquí</span>
                <h2>Un buen corte<br /><span className="serif-accent">te queda cerca.</span></h2>
                <p>Elige tu servicio, tu barbero y el horario que mejor te venga. Te esperamos.</p>
                <div className="booking-detail"><span>⌖</span> Armenia, Quindío · Colombia</div>
                <div className="booking-detail">
                  <span>◷</span> Lun–vie · 8:00 a. m.–8:00 p. m. · Sáb · 8:00 a. m.–6:00 p. m. · Dom · 10:00 a. m.–4:00 p. m.
                </div>
              </div>

              <form className="booking-form" onSubmit={reservar}>
                <div className="form-heading">
                  <span className="eyebrow">Reserva en un momento</span>
                  <h3>Agenda tu cita</h3>
                </div>

                <label>Tu nombre
                  <input 
                    name="cliente" 
                    autoComplete="name" 
                    placeholder="¿Cómo te llamas?" 
                    value={formulario.cliente} 
                    onChange={actualizar} 
                    required 
                    maxLength={80} 
                  />
                </label>

                <div className="form-row">
                  <label>Teléfono
                    <input 
                      name="telefono" 
                      type="tel" 
                      autoComplete="tel" 
                      placeholder="+57 300 000 0000" 
                      value={formulario.telefono} 
                      onChange={actualizar} 
                      required 
                      maxLength={25} 
                    />
                  </label>

                  <label>Correo electrónico
                    <input 
                      name="correo" 
                      type="email" 
                      autoComplete="email" 
                      placeholder="tu@correo.com" 
                      value={formulario.correo} 
                      onChange={actualizar} 
                      required 
                      maxLength={190} 
                    />
                  </label>
                </div>

                <label>Elige tu servicio
                  <select 
                    name="servicioId" 
                    value={formulario.servicioId} 
                    onChange={actualizar} 
                    required 
                    disabled={cargandoCatalogo || servicios.length === 0}
                  >
                    <option value="">Selecciona un servicio</option>
                    {servicios.map((service) => (
                      <option key={service.id} value={service.id}>
                        {service.nombre} · {COP.format(service.precio)}
                      </option>
                    ))}
                  </select>
                </label>

                <div className="form-row">
                  <label>Tu barbero
                    <select 
                      name="barberoId" 
                      value={formulario.barberoId} 
                      onChange={actualizar} 
                      required 
                      disabled={cargandoCatalogo || barberos.length === 0}
                    >
                      <option value="">Elige tu barbero</option>
                      {barberos.map((barber) => (
                        <option key={barber.id} value={barber.id}>
                          {barber.firstName} {barber.lastName}
                        </option>
                      ))}
                    </select>
                  </label>

                  <label>Fecha y hora
                    <input 
                      name="fechaHora" 
                      type="datetime-local" 
                      min={fechaMinima} 
                      step={1800} // Incrementos de 30 minutos (1800 segundos)
                      value={formulario.fechaHora} 
                      onChange={actualizar} 
                      required 
                    />
                  </label>
                </div>

                {!cargandoCatalogo && barberos.length === 0 && (
                  <p className="form-footnote">No hay barberos activos disponibles para reservar en este momento.</p>
                )}

                {/* Selección del método de pago */}
                <fieldset className="payment-options">
                  <legend>¿Cómo prefieres pagar?</legend>
                  {METODOS_PAGO.map((method) => (
                    <label 
                      className={`payment-option ${formulario.metodoPago === method.id ? 'selected' : ''}`} 
                      key={method.id}
                    >
                      <input 
                        type="radio" 
                        name="metodoPago" 
                        value={method.id} 
                        checked={formulario.metodoPago === method.id} 
                        onChange={actualizar} 
                        required 
                      />
                      {method.nombre}
                    </label>
                  ))}
                </fieldset>

                <button 
                  className="button button-primary form-submit" 
                  type="submit" 
                  disabled={reservando || cargandoCatalogo || servicios.length === 0 || barberos.length === 0}
                >
                  {reservando ? 'Guardando…' : 'Confirmar mi cita'} <span aria-hidden="true">↗</span>
                </button>

                {confirmacion && <p className="form-success" role="status">{confirmacion}</p>}
                <p className="form-footnote">La reserva se guarda en el servidor con estado pendiente de confirmación.</p>
              </form>
            </div>
          </section>

          {/* Pie de página de la aplicación */}
          <footer className="site-footer">
            <a className="brand" href="#inicio">
              <span className="brand-mark" aria-hidden="true">C.</span>
              <span><strong>EL CABALLERO</strong><small>BARBERÍA · ARMENIA</small></span>
            </a>
            <span>CALLE 93 · ARMENIA, QUINDÍO</span>
            <span>© 2026 · HECHO CON OFICIO</span>
          </footer>
        </>
      )}
    </div>
  );
}