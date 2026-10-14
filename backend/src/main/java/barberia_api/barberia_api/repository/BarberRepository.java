package barberia_api.barberia_api.repository;

import barberia_api.barberia_api.entity.Barber;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para los Barberos (`BarberRepository`)
 * Gestiona la persistencia y consultas avanzadas sobre la entidad `Barber`.
 * Incorpora bloqueo pesimista para concurrencia crítica, consultas optimizadas con `JOIN FETCH`
 * para prevenir el problema N+1, y métodos de validación de unicidad por usuario.
 */
public interface BarberRepository extends JpaRepository<Barber, Long> {

    /**
     * Verifica si ya existe un perfil de barbero asociado a una cuenta de usuario específica.
     * 
     * @param userId - ID del usuario.
     * @return `true` si el usuario ya cuenta con un perfil profesional; de lo contrario, `false`.
     */
    boolean existsByUserId(Long userId);

    /**
     * Verifica si existe otro perfil de barbero vinculado a un usuario, excluyendo un ID específico 
     * (útil durante operaciones de actualización).
     * 
     * @param userId - ID del usuario a evaluar.
     * @param id     - ID del perfil de barbero a excluir de la validación.
     * @return `true` si hay conflicto de duplicidad; de lo contrario, `false`.
     */
    boolean existsByUserIdAndIdNot(Long userId, Long id);

    /**
     * Busca un barbero aplicando un bloqueo pesimista de escritura (`Pessimistic Write Lock`).
     * Esta consulta es utilizada por el `AppointmentDetailService` para asegurar que ningún otro hilo 
     * modifique simultáneamente los registros del barbero durante operaciones críticas de agenda.
     * 
     * @param id - ID del barbero.
     * @return Un `Optional` con la entidad bloqueada.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Barber b where b.id = :id")
    Optional<Barber> findByIdForUpdate(@Param("id") Long id);

    /**
     * Busca un barbero por su ID cargando de forma ansiosa la entidad de usuario asociada (`JOIN FETCH`),
     * evitando consultas adicionales innecesarias.
     * 
     * @param id - ID del barbero.
     * @return Un `Optional` con el barbero y su usuario integrado.
     */
    @Query("select b from Barber b join fetch b.user where b.id = :id")
    Optional<Barber> findWithUserById(@Param("id") Long id);

    /**
     * Consulta todos los barberos del sistema cargando preventivamente los datos de sus usuarios asociados.
     * 
     * @return Lista completa de barberos con sus perfiles de usuario.
     */
    @Query("select b from Barber b join fetch b.user")
    java.util.List<Barber> findAllWithUsers();

    /**
     * Consulta todos los barberos activos en el sistema que posean estrictamente el rol de barbero,
     * optimizando la consulta para alimentar selectores públicos (como el de la Landing Page).
     * 
     * @return Lista de barberos activos con sus usuarios asociados ordenados por ID.
     */
    @Query("""
            select b from Barber b join fetch b.user
            where b.active = true and upper(b.user.role.name) = 'BARBER'
            order by b.id
            """)
    java.util.List<Barber> findAllActiveWithUsers();

    /**
     * Busca un perfil de barbero a partir del correo electrónico de su cuenta de usuario,
     * realizando una comparación insensible a mayúsculas/minúsculas.
     * Utilizado para autenticar y validar permisos de staff sobre las citas.
     * 
     * @param email - Correo electrónico del usuario.
     * @return Un `Optional` con el barbero correspondiente.
     */
    @Query("select b from Barber b join fetch b.user where lower(b.user.email) = lower(:email)")
    Optional<Barber> findByUserEmailIgnoreCase(@Param("email") String email);
}