import { COP } from '../data.js';

/**
 * Componente `Servicios`
 * Renderiza el catálogo o parrilla de servicios disponibles en la barbería.
 *
 * @param {Object} props
 * @param {Array} props.servicios - Arreglo de objetos con la información de los servicios (id, nombre, descripcion, duracion, precio, imagen).
 * @param {Function} props.alReservar - Función de callback que recibe el ID del servicio seleccionado para iniciar el flujo de reserva.
 */
export default function Servicios({ servicios, alReservar }) {
  return (
    <section className="section section-alt" id="servicios">
      <div className="container">
        {/* Encabezado de la sección */}
        <div className="section-heading">
          <span className="eyebrow">El ritual, a tu manera</span>
          <h2>Servicios pensados para ti</h2>
          <p>Cortes con intención, buenos productos y el tiempo que mereces.</p>
        </div>

        {/* Renderizado condicional: valida si la lista de servicios está vacía */}
        {servicios.length === 0 ? (
          <p className="catalog-state">No hay servicios activos disponibles por ahora.</p>
        ) : (
          /* Grid/Parrilla que contiene las tarjetas de cada servicio */
          <div className="services-grid">
            {servicios.map((servicio, indice) => (
              <article className="service-card" key={servicio.id}>
                
                {/* Marco de la imagen con índice numérico dinámico */}
                <div className="service-image-frame">
                  <img
                    className="service-image"
                    src={servicio.imagen}
                    alt={`${servicio.nombre}: estilo de corte en barbería`}
                    loading="lazy"      /* Carga diferida para optimizar el rendimiento y consumo de red */
                    decoding="async"    /* Decodificación asíncrona de la imagen para no bloquear el hilo principal */
                  />
                  {/* Muestra un número formateado (01, 02, 03...) basado en la posición en el array */}
                  <span className="service-number">0{indice + 1}</span>
                </div>

                {/* Detalles y metadatos del servicio */}
                <div className="service-content">
                  <h3>{servicio.nombre}</h3>
                  <p>{servicio.descripcion}</p>
                  
                  <div className="service-meta">
                    <span>{servicio.duracion} min</span>
                    {/* Formateo de moneda utilizando la utilidad internacionalizada COP */}
                    <strong>{COP.format(servicio.precio)}</strong>
                  </div>

                  {/* Botón de acción para seleccionar el servicio */}
                  <button 
                    className="text-button" 
                    type="button" 
                    onClick={() => alReservar(servicio.id)}
                  >
                    Elegir servicio <span aria-hidden="true">↗</span>
                  </button>
                </div>

              </article>
            ))}
          </div>
        )}
      </div>
    </section>
  );
}