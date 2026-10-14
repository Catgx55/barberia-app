package barberia_api.barberia_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad JPA `Barber`
 * Representa la tabla `barbers` en la base de datos relacional. 
 * Almacena el perfil profesional específico de los barberos del establecimiento, manteniendo 
 * una relación uno a uno estrictamente vinculada con la cuenta de usuario general (`User`), 
 * su especialidad técnica, su estado operativo y la marca de tiempo de creación.
 */
@Entity
@Table(name = "barbers", uniqueConstraints = @UniqueConstraint(name = "uk_barbers_user", columnNames = "user_id"))
@Getter
@Setter
@NoArgsConstructor
public class Barber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Cuenta de usuario asociada al barbero. Relación uno a uno (`@OneToOne`) con la entidad `User` (`user_id`).
     * Configurada con carga perezosa (`FetchType.LAZY`) para evitar consultas innecesarias, 
     * y con obligatoriedad estricta (`optional = false`, `nullable = false`).
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String specialty;

    /**
     * Indicador booleano que define si el perfil del barbero se encuentra activo (`true`) 
     * o desactivado mediante borrado lógico (`false`), preservando el historial de citas pasadas.
     */
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Método de ciclo de vida JPA ejecutado automáticamente antes de insertar el registro en la base de datos,
     * estableciendo la marca de tiempo inmutable de creación (`createdAt`).
     */
    @PrePersist
    void setCreatedAt() {
        createdAt = LocalDateTime.now();
    }
}