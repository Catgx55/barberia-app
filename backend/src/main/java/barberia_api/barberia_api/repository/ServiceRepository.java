package barberia_api.barberia_api.repository;

import barberia_api.barberia_api.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA para el Catálogo de Servicios (`ServiceRepository`)
 * Gestiona la persistencia, operaciones CRUD y validaciones de unicidad 
 * sobre la entidad comercial `ServiceEntity`.
 */
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {

    /**
     * Verifica si ya existe un servicio registrado con un nombre específico,
     * realizando una búsqueda insensible a mayúsculas y minúsculas.
     * 
     * @param name - Nombre comercial del servicio a evaluar.
     * @return `true` si el nombre ya se encuentra ocupado; de lo contrario, `false`.
     */
    boolean existsByNameIgnoreCase(String name);
}