import { METODOS_PAGO } from '../data.js';

/**
 * Componente `MedioPago`
 * Renderiza la sección informativa sobre los métodos de pago aceptados en la barbería,
 * consumiendo las opciones estáticas centralizadas en el archivo de datos.
 */
export default function MedioPago() {
  return (
    <section className="section section-alt" id="pagos">
      <div className="container payment-layout">
        
        {/* Encabezado y copy de la sección */}
        <div className="section-heading">
          <span className="eyebrow">Simple y sin sorpresas</span>
          <h2>
            Paga como<br />
            <span className="serif-accent">te quede mejor.</span>
          </h2>
          <p>Reserva en línea y elige tu forma favorita de pagar cuando nos visites.</p>
        </div>

        {/* Listado de métodos de pago disponibles */}
        <div className="payment-list">
          {METODOS_PAGO.map((metodo) => (
            <article className="payment-row" key={metodo.id}>
              {/* Icono representativo del método de pago (oculto a lectores de pantalla por decorativo) */}
              <span className="payment-icon" aria-hidden="true">
                {metodo.icono}
              </span>

              {/* Información comercial: Nombre y detalle operativo */}
              <span>
                <strong>{metodo.nombre}</strong>
                <small>{metodo.detalle}</small>
              </span>

              {/* Marca de verificación visual de disponibilidad (oculta a lectores de pantalla) */}
              <span className="payment-check" aria-hidden="true">✓</span>
            </article>
          ))}
        </div>

      </div>
    </section>
  );
}