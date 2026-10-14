package barberia_api.barberia_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entidad JPA `AppointmentDetail`
 * Representa la tabla intermedia `appointment_details` en la base de datos relacional.
 * Almacena los servicios individuales que conforman una cita (relación muchos a uno con `Appointment`
 * y muchos a uno con `ServiceEntity`), junto con el precio histórico fijado al momento de la reserva.
 */
@Entity
@Table(name = "appointment_details",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_appointment_details_service",
                columnNames = {"appointment_id", "service_id"}))
@Getter
@Setter
@NoArgsConstructor
public class AppointmentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Cita a la cual pertenece este detalle de servicio. Relación de muchos a uno (`appointment_id`)
     * configurada con carga perezosa (`FetchType.LAZY`).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    /**
     * Servicio comercial seleccionado (ej. corte de cabello, arreglo de barba). Relación de muchos a uno (`service_id`).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    /**
     * Precio monetario del servicio congelado al momento de crear la reserva. 
     * Configurado con precisión estricta (`precision = 12, scale = 2`) para evitar discrepancias financieras 
     * si el precio general del catálogo de servicios cambia en el futuro.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
}