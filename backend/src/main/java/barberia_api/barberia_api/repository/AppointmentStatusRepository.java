package barberia_api.barberia_api.repository;

import barberia_api.barberia_api.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para los Estados de Citas (`AppointmentStatusRepository`)
 * Gestiona la persistencia, operaciones CRUD y métodos de consulta especializados 
 * sobre la entidad de catálogo maestro `AppointmentStatus`.
 */
public interface AppointmentStatusRepository extends JpaRepository<AppointmentStatus, Long> {

    /**
     * Verifica si ya existe un estado de cita registrado con un nombre específico,
     * realizando una búsqueda insensible a mayúsculas y minúsculas.
     * 
     * @param name - Nombre del estado a evaluar (ej. PENDIENTE, COMPLETADA).
     * @return `true` si el nombre ya se encuentra ocupado; de lo contrario, `false`.
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Busca y retorna un estado de cita a partir de su nombre,
     * realizando una comparación insensible a mayúsculas y minúsculas.
     * Utilizado frecuentemente por servicios como `PublicBookingService` para localizar 
     * automáticamente el estado inicial predeterminado (ej. "PENDIENTE").
     * 
     * @param name - Nombre del estado buscado.
     * @return Un `Optional` que contiene el estado si es encontrado.
     */
    Optional<AppointmentStatus> findByNameIgnoreCase(String name);
}