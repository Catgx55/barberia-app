package barberia_api.barberia_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad JPA `ServiceEntity`
 * Representa la tabla `services` en la base de datos relacional. 
 * Almacena el catálogo maestro de servicios ofrecidos por la barbería (cortes, arreglos de barba, 
 * tratamientos, etc.), incluyendo su nombre comercial único, descripción, precio con precisión monetaria, 
 * duración estimada en minutos, estado operativo y marca de tiempo de creación.
 */
@Entity
@Table(name = "services", uniqueConstraints = @UniqueConstraint(name = "uk_services_name", columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre comercial del servicio. Configurado con una restricción de unicidad a nivel de base de datos 
     * (`UniqueConstraint`) y una longitud máxima de 120 caracteres.
     */
    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    /**
     * Precio oficial del servicio. Almacenado con precisión monetaria estricta (`precision = 12, scale = 2`) 
     * mediante la clase `BigDecimal` para evitar errores de redondeo asociados a tipos de punto flotante.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /**
     * Duración estimada del servicio expresada en minutos (ej. 30, 45, 60 minutos), 
     * utilizada por el `AppointmentService` para calcular la hora de finalización (`endTime`) de la cita.
     */
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    /**
     * Indicador booleano que determina si el servicio se encuentra activo (`true`) para la reserva pública 
     * o desactivado mediante borrado lógico (`false`), preservando el historial financiero de citas pasadas.
     */
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Método de ciclo de vida JPA ejecutado de forma automática antes de persistir la entidad,
     * asignando la marca de tiempo de creación (`createdAt`).
     */
    @PrePersist
    void setCreatedAt() {
        createdAt = LocalDateTime.now();
    }
}