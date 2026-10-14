package barberia_api.barberia_api.repository;

import barberia_api.barberia_api.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad `Role` (`RoleRepository`)
 * Proporciona métodos de consulta optimizados y operaciones CRUD heredadas de `JpaRepository`
 * para la gestión y validación de los roles de usuario en la base de datos de la barbería.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Verifica si ya existe un rol registrado con el nombre especificado,
     * ignorando diferencias entre mayúsculas y minúsculas.
     * 
     * @param name - Nombre del rol a buscar (ej. "admin", "barber").
     * @return `true` si el rol ya existe en el sistema, de lo contrario `false`.
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Busca y recupera un rol a partir de su nombre, ignorando mayúsculas y minúsculas.
     * Envuelve el resultado en un `Optional` para manejar de manera segura la ausencia del registro.
     * 
     * @param name - Nombre del rol a consultar.
     * @return Un `Optional` que contiene la entidad `Role` si es encontrada, o vacío si no existe.
     */
    Optional<Role> findByNameIgnoreCase(String name);
}
