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
 * Entidad JPA `Role`
 * Representa la tabla `roles` en la base de datos relacional. Define los diferentes
 * niveles de autorización y privilegios que pueden tener los usuarios dentro del sistema
 * de la barbería (por ejemplo, `ADMIN` o `BARBER`).
 */
@Entity
@Table(
    name = "roles", 
    uniqueConstraints = {@UniqueConstraint(name = "uk_roles_name", columnNames = "name")}
)
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre descriptivo del rol (ej. ADMIN, BARBER).
     * Se configura con restricción de unicidad a nivel de base de datos para evitar duplicados.
     */
    @Column(nullable = false, length = 60)
    private String name;
}
