package barberia_api.barberia_api.repository;

import barberia_api.barberia_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad `User` (`UserRepository`)
 * Proporciona operaciones CRUD estándar heredadas de `JpaRepository` y métodos de consulta
 * derivados optimizados para la gestión de barberos y administradores en la base de datos.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Verifica si ya existe un usuario registrado con el correo electrónico especificado,
     * ignorando diferencias entre mayúsculas y minúsculas.
     * 
     * @param email - Correo electrónico a validar.
     * @return `true` si el correo ya está registrado en el sistema, de lo contrario `false`.
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Verifica si existe otro usuario con el mismo correo electrónico (ignorando mayúsculas/minúsculas),
     * excluyendo al usuario con el ID proporcionado.
     * 
     * @param email - Correo electrónico a validar.
     * @param id - ID del usuario actual que se está actualizando (para evitar conflictos consigo mismo).
     * @return `true` si el correo ya pertenece a otro registro diferente, de lo contrario `false`.
     */
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    /**
     * Busca y recupera un usuario a partir de su correo electrónico, ignorando mayúsculas/minúsculas.
     * Envuelve el resultado en un `Optional` para manejar de forma segura la ausencia del registro.
     * 
     * @param email - Correo electrónico del usuario a buscar.
     * @return Un `Optional` que contiene la entidad `User` si es encontrada, o vacío si no existe.
     */
    Optional<User> findByEmailIgnoreCase(String email);
}