package barberia_api.barberia_api.service;

import barberia_api.barberia_api.dto.ApiDtos.ServiceRequest;
import barberia_api.barberia_api.dto.ApiDtos.ServiceResponse;
import barberia_api.barberia_api.entity.ServiceEntity;
import barberia_api.barberia_api.exception.ConflictException;
import barberia_api.barberia_api.exception.EntityLookup;
import barberia_api.barberia_api.repository.ServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de Lógica de Negocio para el Catálogo de Servicios (`ServiceCatalogService`)
 * Administra las operaciones CRUD del catálogo comercial de servicios de la barbería 
 * (cortes, barbería, barba, tratamientos, precios, duraciones en minutos). 
 * Implementa validaciones de unicidad por nombre e incorpora borrado lógico (*soft delete*) 
 * para salvaguardar el historial contable y las citas pasadas vinculadas.
 */
@Service
@Transactional
public class ServiceCatalogService {

    private final ServiceRepository services;

    /**
     * Inyección de dependencias por constructor del repositorio de servicios.
     */
    public ServiceCatalogService(ServiceRepository services) {
        this.services = services;
    }

    /**
     * Obtiene la lista completa de todos los servicios registrados en el sistema 
     * (tanto activos como inactivos).
     */
    @Transactional(readOnly = true)
    public List<ServiceResponse> findAll() {
        return services.findAll().stream().map(ServiceCatalogService::toResponse).toList();
    }

    /**
     * Busca y retorna un servicio específico mediante su ID único.
     */
    @Transactional(readOnly = true)
    public ServiceResponse findById(Long id) {
        return toResponse(EntityLookup.service(services, id));
    }

    /**
     * Crea un nuevo servicio en el catálogo tras normalizar su nombre y validar 
     * que no exista otro servicio registrado con el mismo nombre (insensible a mayúsculas/minúsculas).
     */
    public ServiceResponse create(ServiceRequest request) {
        String name = normalize(request.name());
        if (services.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Ya existe un servicio con ese nombre.");
        }
        
        ServiceEntity service = new ServiceEntity();
        apply(service, request);
        return toResponse(services.save(service));
    }

    /**
     * Actualiza la información de un servicio existente (nombre, descripción, precio, duración y estado),
     * validando restricciones de unicidad en caso de que el nombre comercial sea modificado.
     */
    public ServiceResponse update(Long id, ServiceRequest request) {
        ServiceEntity service = EntityLookup.service(services, id);
        String name = normalize(request.name());
        
        if (!service.getName().equalsIgnoreCase(name) && services.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Ya existe un servicio con ese nombre.");
        }
        
        apply(service, request);
        return toResponse(services.save(service));
    }

    /**
     * Realiza un borrado lógico (*Soft Delete*) del servicio cambiando su estado a inactivo (`active = false`).
     * Esta estrategia previene romper las llaves foráneas en los detalles históricos de citas anteriores 
     * donde el servicio formaba parte de la reserva del cliente.
     */
    public void delete(Long id) {
        ServiceEntity service = EntityLookup.service(services, id);
        service.setActive(false);
        services.save(service);
    }

    /**
     * Método auxiliar privado (*Helper*) para aplicar los valores de la solicitud a la entidad,
     * centralizando la asignación de atributos y manteniendo el principio DRY (*Don't Repeat Yourself*).
     */
    private static void apply(ServiceEntity service, ServiceRequest request) {
        service.setName(normalize(request.name()));
        service.setDescription(request.description().trim());
        service.setPrice(request.price());
        service.setDurationMinutes(request.durationMinutes());
        service.setActive(request.active());
    }

    /**
     * Normaliza la cadena de texto eliminando espacios en blanco sobrantes.
     */
    private static String normalize(String name) {
        return name.trim();
    }

    /**
     * Mapea una entidad `ServiceEntity` a su respectivo DTO de respuesta `ServiceResponse`.
     */
    private static ServiceResponse toResponse(ServiceEntity service) {
        return new ServiceResponse(
                service.getId(),
                service.getName(),
                service.getDescription(),
                service.getPrice(),
                service.getDurationMinutes(),
                service.isActive(),
                service.getCreatedAt()
        );
    }
}