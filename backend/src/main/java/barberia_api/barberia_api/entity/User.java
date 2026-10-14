package barberia_api.barberia_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad JPA `User`
 * Representa la tabla `users` en la base de datos relacional. Almacena la información
 * de los usuarios del sistema (barberos y administradores), incluyendo sus credenciales,
 * datos de contacto y la relación con su rol asignado.
 */
@Entity
@Table(
    name = "users", 
    uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email")
)
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 190)
    private String email;

    @Column(nullable = false, length = 25)
    private String phone;

    /**
     * Contraseña cifrada del usuario.
     * Anotada con `@JsonIgnore` para garantizar que jamás se exponga o se devuelva 
     * en el cuerpo de las respuestas JSON al consumir los endpoints de la API.
     */
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    /**
     * Rol asociado al usuario (ej. ADMIN o BARBER).
     * Se configura con `FetchType.LAZY` para optimizar el rendimiento, cargando la información
     * del rol únicamente cuando sea explícitamente consultada.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Callback ejecutado automáticamente por JPA justo antes de insertar 
     * un nuevo registro en la base de datos (Persist).
     * Inicializa las marcas de tiempo de creación y última actualización.
     */
    @PrePersist
    void setCreatedAt() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    /**
     * Callback ejecutado automáticamente por JPA justo antes de actualizar 
     * un registro existente en la base de datos.
     * Actualiza la marca de tiempo `updatedAt` con el momento exacto de la modificación.
     */
    @PreUpdate
    void setUpdatedAt() {
        updatedAt = LocalDateTime.now();
    }
}
