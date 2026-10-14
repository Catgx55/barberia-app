package barberia_api.barberia_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA `AppointmentStatus`
 * Representa la tabla de catálogo maestro `appointment_statuses` en la base de datos relacional.
 * Almacena los diferentes estados operativos posibles en los que puede encontrarse una reserva 
 * (por ejemplo: PENDIENTE, CONFIRMADA, COMPLETADA, CANCELADA), garantizando unicidad en los nombres 
 * a nivel de infraestructura.
 */
@Entity
@Table(name = "appointment_statuses",
        uniqueConstraints = @UniqueConstraint(name = "uk_appointment_statuses_name", columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
public class AppointmentStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre descriptivo del estado de la cita. 
     * Configurado con una longitud máxima de 60 caracteres y una restricción de unicidad 
     * a nivel de base de datos (`UniqueConstraint`) para prevenir nombres duplicados.
     */
    @Column(nullable = false, length = 60)
    private String name;
}