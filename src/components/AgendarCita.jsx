import { useEffect, useState } from 'react';
import { apiRequest } from '../api.js';
import MedioPago from './MedioPago.jsx';
import PanelBarbero from './PanelBarbero.jsx';
import Reseña from './Resena.jsx';
import Servicios from './Servicios.jsx';
import { COP, IMAGENES_SERVICIOS, METODOS_PAGO } from '../data.js';

/**
 * Estado inicial limpio del formulario de reserva.
 */
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
 * Convierte un objeto Date a una cadena ISO recortada para campos `datetime-local` (YYYY-MM-THH:mm),
 * ajustando manualmente la descompensación de la zona horaria local (`getTimezoneOffset`).
 * 
 * @param {Date} fecha - Objeto Date a formatear.
 * @returns {string} Cadena formateada para inputs de fecha y hora locales.
 */
function fechaHoraLocal(fecha) {
  const desplazamiento = fecha.getTimezoneOffset() * 60_000;
  return new Date(fecha.getTime() - desplazamiento).toISOString().slice(0, 16);
}

/**
 * Calcula la siguiente franja horaria disponible redondeando hacia arriba en intervalos de 30 minutos.
 * 
 * @returns {string} Fecha y hora formateada para la restricción `min` del input `datetime-local`.
 */
function siguienteHorarioDisponible() {
  const siguiente = new Date();
  siguiente.setSeconds(0, 0);
  siguiente.setMinutes(Math.ceil((siguiente.getMinutes() + 1) / 30) * 30);
  return fechaHoraLocal(siguiente);
}

/**
 * Adaptador de Datos (*Adapter Pattern*): Mapea la respuesta del DTO del backend (`ServiceResponse`)
 * al formato esperado por la interfaz visual del cliente, asignando imágenes estáticas del catálogo.
 * 
 * @param {Object} service - DTO de servicio proveniente de la API REST.
 * @returns {Object} Servicio adaptado para el frontend.
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
 * Actúa como la vista contenedor raíz (*Landing Page*) de Barbería "El Caballero".
 * Gestiona el tema visual (oscuro/claro), el consumo asíncrono del catálogo público,
 * la conmutación entre la landing page y el panel del barbero, y el agendamiento directo de citas.
 */
export default function AgendarCita() {
  // Estados de interfaz y navegación
  const [temaOscuro, setTemaOscuro] = useState(true);
  const [vistaPanel, setVistaPanel] = useState(false);
  
  // Estados de datos asíncronos
  const [servicios, setServicios] = useState([]);
  const [barberos, setBarberos] = useState([]);
  const [cargandoCatalogo, setCargandoCatalogo] = useState(true);
  const [errorCatalogo, setErrorCatalogo] = useState('');
  
  // Estados de gestión del formulario de reserva
  const [formulario, setFormulario] = useState(FORMULARIO_VACIO);
  const [confirmacion, setConfirmacion] = useState('');
  const [errorReserva, setErrorReserva] = useState('');
  const [reservando, setReservando] = useState(false);
  const [fechaMinima, setFechaMinima] = useState(siguienteHorarioDisponible);

  /**
   * Carga inicial asíncrona mediante `Promise.all` con soporte para cancelación de peticiones (`AbortController`).
   * Consulta en paralelo el catálogo de servicios (`/services`) y los barberos disponibles (`/public/barbers`).
   */
  useEffect(() => {
    const controller = new AbortController();
    Promise.all([
      apiRequest('/services', { signal: controller.signal }),
      apiRequest('/public/barbers', { signal: controller.signal }),
    ])
      .then(([serviceList, barberList]) => {
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
   * Sincroniza la selección directa desde la tarjeta del servicio con el formulario de reserva,
   * desplazando la pantalla suavemente hacia la sección `#agendar`.
   */
  function manejarSeleccionServicio(id) {
    setFormulario((actual) => ({ ...actual, servicioId: id }));
    setConfirmacion('');
    setErrorReserva('');
    document.getElementById('agendar')?.scrollIntoView({ behavior: 'smooth' });
  }

  /**
   * Manejador de cambios genérico para actualizar el estado del formulario de forma inmutable.
   */
  function actualizar(evento) {
    setFormulario((actual) => ({ ...actual, [evento.target.name]: evento.target.value }));
    setConfirmacion('');
    setErrorReserva('');
  }

  /**
   * Envía la reserva pública al backend (`POST /public/appointments`).
   * Valida en cliente el horario y delega al `PublicBookingService` la creación del cliente y de la cita.
   */
  async function reservar(evento) {
    evento.preventDefault();
    const service = servicios.find((item) => item.id === Number(formulario.servicioId));
    const barber = barberos.find((item) => item.id === Number(formulario.barberoId));
    const start = new Date(formulario.fechaHora);

    if (!service || !barber || Number.isNaN(start.getTime())) {
      setErrorReserva('Selecciona un servicio, barbero y horario válidos.');
      return;
    }
    if (start <= new Date()) {
      setErrorReserva('La fecha de la cita debe ser posterior a la hora actual.');
      return;
    }

    // Descompone el nombre completo en nombre y apellido para el backend
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

      // Limpia el formulario y muestra mensaje de éxito con el ID de la cita retornado por el servidor
      setFormulario(FORMULARIO_VACIO);
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
   * Alterna la navegación entre la Landing Page del cliente y el Panel de Administración del Barbero.
   */
  function alternarVista() {
    setVistaPanel((actual) => !actual);
    setConfirmacion('');
    setErrorReserva('');
  }

  return (
    <div className={`app-shell ${temaOscuro ? 'theme-dark' : 'theme-light'}`}>
      <header className="site-header">
        <a className="brand" href="#inicio" onClick={() => setVistaPanel(false)} aria-label="El Caballero, inicio">
          <span className="brand-mark" aria-hidden="true">C.</span>
          <span><strong>EL CABALLERO</strong><small>BARBERÍA · ARMENIA</small></span>
        </a>
        <nav className="main-nav" aria-label="Navegación principal">
          {!vistaPanel && <>
            <a href="#servicios">Servicios</a><a href="#resenas">Reseñas</a><a href="#pagos">Medios de pago</a>
            <a className="nav-book" href="#agendar">Reservar <span aria-hidden="true">↗</span></a>
          </>}
          <button className="theme-toggle" type="button" onClick={() => setTemaOscuro((actual) => !actual)} aria-label={temaOscuro ? 'Activar modo claro' : 'Activar modo oscuro'}>
            <span aria-hidden="true">{temaOscuro ? '☼' : '☾'}</span> {temaOscuro ? 'Modo claro' : 'Modo oscuro'}
          </button>
          <button className="staff-link" type="button" onClick={alternarVista}>{vistaPanel ? '← Volver al sitio' : 'Panel barbero'}</button>
        </nav>
      </header>

      {vistaPanel ? <PanelBarbero /> : (
        <>
          {(errorCatalogo || errorReserva) && (
            <div className="connection-notice" role="alert">
              <span>{errorReserva || errorCatalogo}</span>
              <button type="button" aria-label="Cerrar aviso" onClick={() => { setErrorReserva(''); setErrorCatalogo(''); }}>×</button>
            </div>
          )}
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

          {cargandoCatalogo ? (
            <p className="catalog-state" role="status">Cargando servicios y barberos…</p>
          ) : (
            <Servicios servicios={servicios} alReservar={manejarSeleccionServicio} />
          )}
          <Reseña />
          <MedioPago />

          <section className="section booking-section" id="agendar">
            <div className="container booking-layout">
              <div className="booking-copy">
                <span className="eyebrow">Tu próxima visita empieza aquí</span>
                <h2>Un buen corte<br /><span className="serif-accent">te queda cerca.</span></h2>
                <p>Elige tu servicio, tu barbero y el horario que mejor te venga. Te esperamos.</p>
                <div className="booking-detail"><span>⌖</span> Armenia, Quindío · Colombia</div>
                <div className="booking-detail"><span>◷</span> Lun–vie · 8:00 a. m.–8:00 p. m. · Sáb · 8:00 a. m.–6:00 p. m. · Dom · 10:00 a. m.–4:00 p. m.</div>
              </div>
              <form className="booking-form" onSubmit={reservar}>
                <div className="form-heading"><span className="eyebrow">Reserva en un momento</span><h3>Agenda tu cita</h3></div>
                <label>Tu nombre<input name="cliente" autoComplete="name" placeholder="¿Cómo te llamas?" value={formulario.cliente} onChange={actualizar} required maxLength={80} /></label>
                <div className="form-row">
                  <label>Teléfono<input name="telefono" type="tel" autoComplete="tel" placeholder="+57 300 000 0000" value={formulario.telefono} onChange={actualizar} required maxLength={25} /></label>
                  <label>Correo electrónico<input name="correo" type="email" autoComplete="email" placeholder="tu@correo.com" value={formulario.correo} onChange={actualizar} required maxLength={190} /></label>
                </div>
                <label>Elige tu servicio
                  <select name="servicioId" value={formulario.servicioId} onChange={actualizar} required disabled={cargandoCatalogo || servicios.length === 0}>
                    <option value="">Selecciona un servicio</option>
                    {servicios.map((service) => <option key={service.id} value={service.id}>{service.nombre} · {COP.format(service.precio)}</option>)}
                  </select>
                </label>
                <div className="form-row">
                  <label>Tu barbero
                    <select name="barberoId" value={formulario.barberoId} onChange={actualizar} required disabled={cargandoCatalogo || barberos.length === 0}>
                      <option value="">Elige tu barbero</option>
                      {barberos.map((barber) => <option key={barber.id} value={barber.id}>{barber.firstName} {barber.lastName}</option>)}
                    </select>
                  </label>
                  <label>Fecha y hora<input name="fechaHora" type="datetime-local" min={fechaMinima} step={1800} value={formulario.fechaHora} onChange={actualizar} required /></label>
                </div>
                {!cargandoCatalogo && barberos.length === 0 && (
                  <p className="form-footnote">No hay barberos activos disponibles para reservar en este momento.</p>
                )}
                <fieldset className="payment-options">
                  <legend>¿Cómo prefieres pagar?</legend>
                  {METODOS_PAGO.map((method) => (
                    <label className={`payment-option ${formulario.metodoPago === method.id ? 'selected' : ''}`} key={method.id}>
                      <input type="radio" name="metodoPago" value={method.id} checked={formulario.metodoPago === method.id} onChange={actualizar} required />
                      {method.nombre}
                    </label>
                  ))}
                </fieldset>
                <button className="button button-primary form-submit" type="submit" disabled={reservando || cargandoCatalogo || servicios.length === 0 || barberos.length === 0}>
                  {reservando ? 'Guardando…' : 'Confirmar mi cita'} <span aria-hidden="true">↗</span>
                </button>
                {confirmacion && <p className="form-success" role="status">{confirmacion}</p>}
                <p className="form-footnote">La reserva se guarda en el servidor con estado pendiente de confirmación.</p>
              </form>
            </div>
          </section>

          <footer className="site-footer">
            <a className="brand" href="#inicio"><span className="brand-mark" aria-hidden="true">C.</span><span><strong>EL CABALLERO</strong><small>BARBERÍA · ARMENIA</small></span></a>
            <span>CALLE 93 · ARMENIA, QUINDÍO</span>
            <span>© 2026 · HECHO CON OFICIO</span>
          </footer>
        </>
      )}
    </div>
  );
}