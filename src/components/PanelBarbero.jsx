import { useEffect, useState } from 'react';
import { apiRequest, basicAuthorization } from '../api.js';
import { COP } from '../data.js';

/**
 * Componente `PanelBarbero`
 * Gestiona el panel de administración/staff para la barbería. Permite autenticación
 * mediante credenciales HTTP Basic, visualización de la agenda de citas en tiempo real,
 * y actualización dinámica del estado de cada reserva.
 */
export default function PanelBarbero() {
  // Estados de autenticación y credenciales
  const [credentials, setCredentials] = useState(null); // Almacena el token de Basic Auth en base64
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [user, setUser] = useState(null); // Datos del usuario autenticado (ej. rol, email)

  // Estados de datos de la aplicación
  const [appointments, setAppointments] = useState([]); // Listado de citas del servidor
  const [statuses, setStatuses] = useState([]); // Catálogo de estados posibles para las citas

  // Estados de control de interfaz (UX)
  const [loading, setLoading] = useState(true);
  const [updatingId, setUpdatingId] = useState(null); // ID de la cita que se está actualizando actualmente
  const [error, setError] = useState('');

  /**
   * Efecto secundario (Side Effect) para la carga de datos protegidos.
   * Se ejecuta automáticamente cada vez que cambian las credenciales (al iniciar o cerrar sesión).
   * Utiliza `AbortController` para cancelar solicitudes HTTP pendientes si el componente se desmonta.
   */
  useEffect(() => {
    if (!credentials) return undefined;

    const controller = new AbortController();

    // Carga paralela de citas y estados mediante Promise.all para optimizar tiempos de respuesta
    Promise.all([
      apiRequest('/appointments', { authorization: credentials, signal: controller.signal }),
      apiRequest('/appointment-statuses', { authorization: credentials, signal: controller.signal }),
    ])
      .then(([appointmentList, statusList]) => {
        setAppointments(appointmentList);
        setStatuses(statusList);
        setError('');
      })
      .catch((requestError) => {
        // Ignora los errores provocados intencionalmente por la cancelación del AbortController
        if (requestError.name !== 'AbortError') {
          setError(requestError.message);
          // Si el servidor rechaza las credenciales por expiración o invalidación, limpia la sesión
          if (requestError.message.startsWith('Credenciales')) {
            setCredentials(null);
            setUser(null);
          }
        }
      })
      .finally(() => setLoading(false));

    // Función de limpieza que cancela la petición fetch si el componente se desmonta
    return () => controller.abort();
  }, [credentials]);

  /**
   * Maneja el envío del formulario de inicio de sesión.
   * Genera las cabeceras de autorización básica y valida contra el endpoint de sesión (/auth/me).
   */
  async function iniciarSesion(event) {
    event.preventDefault();
    setLoading(true);
    setError('');

    const authorization = basicAuthorization(email, password);

    try {
      const authenticatedUser = await apiRequest('/auth/me', { authorization });
      setCredentials(authorization);
      setUser(authenticatedUser);
      setPassword(''); // Limpia el campo de contraseña por seguridad en el estado local
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setLoading(false);
    }
  }

  /**
   * Actualiza el estado de una cita específica en el servidor y refleja el cambio de forma optimista/reactiva.
   * 
   * @param {Object} appointment - Objeto de la cita a modificar.
   * @param {string|number} statusId - Nuevo ID de estado seleccionado.
   */
  async function actualizarEstado(appointment, statusId) {
    setUpdatingId(appointment.id);
    setError('');

    try {
      // Envía la petición PATCH con el nuevo estado al backend
      const updatedAppointment = await apiRequest(`/appointments/${appointment.id}/status`, {
        method: 'PATCH',
        body: { statusId: Number(statusId) },
        authorization: credentials,
      });

      // Actualiza localmente el array de citas reemplazando únicamente la cita modificada
      setAppointments((current) => current.map((item) => (
        item.id === updatedAppointment.id ? updatedAppointment : item
      )));
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setUpdatingId(null);
    }
  }

  /**
   * Limpia las credenciales y el estado global del panel, cerrando la sesión actual.
   */
  function cerrarSesion() {
    setCredentials(null);
    setUser(null);
    setAppointments([]);
    setStatuses([]);
    setError('');
  }

  return (
    <section className="staff-page">
      <div className="container staff-container">
        <span className="eyebrow">Herramientas del equipo</span>

        {/* Renderizado condicional: Formulario de inicio de sesión vs. Panel de gestión */}
        {!credentials ? (
          <div className="staff-login">
            <div className="staff-heading">
              <div>
                <h1>Panel del barbero</h1>
                <p>Inicia sesión con una cuenta activa de barbero o administrador.</p>
              </div>
            </div>

            {/* Banner de error global */}
            {error && <p className="error-banner" role="alert">{error}</p>}

            <form className="booking-form" onSubmit={iniciarSesion}>
              <label>Correo electrónico
                <input
                  type="email"
                  autoComplete="username"
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  required
                />
              </label>
              
              <label>Contraseña
                <input
                  type="password"
                  autoComplete="current-password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  required
                />
              </label>

              <button className="button button-primary form-submit" type="submit" disabled={loading}>
                {loading ? 'Verificando…' : 'Iniciar sesión'}
              </button>
            </form>
          </div>
        ) : (
          <>
            {/* Cabecera del panel de control autenticado */}
            <div className="staff-heading">
              <div>
                <h1>Panel del barbero</h1>
                <p>Reservas guardadas en el servidor · Sesión: {user?.email}</p>
              </div>
              <div className="staff-panel-actions">
                <span className="staff-count">{appointments.length}<small> reservas</small></span>
                <button className="staff-link" type="button" onClick={cerrarSesion}>Cerrar sesión</button>
              </div>
            </div>

            {error && <p className="error-banner" role="alert">{error}</p>}

            {/* Estados de carga, lista vacía o renderizado de la tabla de reservas */}
            {loading ? (
              <p className="staff-empty" role="status">Cargando agenda…</p>
            ) : appointments.length === 0 ? (
              <p className="staff-empty">Todavía no hay citas registradas.</p>
            ) : (
              <div className="appointments-table">
                <div className="appointment-header">
                  <span>Cliente</span>
                  <span>Servicio y barbero</span>
                  <span>Fecha y hora</span>
                  <span>Pago</span>
                  <span>Estado</span>
                </div>

                {appointments.map((appointment) => (
                  <article className="appointment-row" key={appointment.id}>
                    {/* Información del cliente */}
                    <div className="client-cell">
                      <strong>{appointment.clientName}</strong>
                      <small>{appointment.clientPhone} · #{appointment.id}</small>
                    </div>

                    {/* Detalles de servicios, barbero asignado y precio formateado */}
                    <div>
                      <strong>{appointment.details.map((detail) => detail.serviceName).join(', ')}</strong>
                      <small>{appointment.barberName} · {COP.format(Number(appointment.totalAmount))}</small>
                    </div>

                    {/* Fecha y hora formateadas al estándar local colombiano (es-CO) */}
                    <span>
                      {new Date(`${appointment.appointmentDate}T${appointment.startTime}`).toLocaleString('es-CO', { 
                        dateStyle: 'medium', 
                        timeStyle: 'short' 
                      })}
                    </span>

                    <span>{appointment.paymentMethod || 'No indicado'}</span>

                    {/* Selector de estado interactivo */}
                    <select
                      aria-label={`Estado de la cita de ${appointment.clientName}`}
                      value={appointment.statusId}
                      disabled={updatingId === appointment.id} // Deshabilita el select de esta fila mientras se procesa el parche HTTP
                      onChange={(event) => actualizarEstado(appointment, event.target.value)}
                    >
                      {statuses.map((status) => (
                        <option key={status.id} value={status.id}>
                          {status.name.replace('_', ' ')}
                        </option>
                      ))}
                    </select>
                  </article>
                ))}
              </div>
            )}
          </>
        )}
      </div>
    </section>
  );
}