package barberia_api.barberia_api.service;

import barberia_api.barberia_api.dto.ApiDtos.AppointmentStatusRequest;
import barberia_api.barberia_api.dto.ApiDtos.AppointmentStatusResponse;
import barberia_api.barberia_api.entity.AppointmentStatus;
import barberia_api.barberia_api.exception.ConflictException;
import barberia_api.barberia_api.exception.EntityLookup;
import barberia_api.barberia_api.repository.AppointmentStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de Lógica de Negocio para Estados de Citas (`AppointmentStatusService`)
 * Administra las operaciones CRUD del catálogo maestro de estados de reservas (por ejemplo, PENDIENTE, 
 * CONFIRMADA, COMPLETADA), asegurando la normalización de datos y la validación de unicidad por nombre.
 */
@Service
@Transactional
public class AppointmentStatusService {

    private final AppointmentStatusRepository statuses;

    /**
     * Inyección de dependencias por constructor del repositorio de estados.
     */
    public AppointmentStatusService(AppointmentStatusRepository statuses) {
        this.statuses = statuses;
    }

    /**
     * Obtiene la lista completa de todos los estados de citas registrados en el sistema.
     */
    @Transactional(readOnly = true)
    public List<AppointmentStatusResponse> findAll() {
        return statuses.findAll().stream().map(AppointmentStatusService::toResponse).toList();
    }

    /**
     * Busca y retorna un estado específico de cita mediante su ID único.
     */
    @Transactional(readOnly = true)
    public AppointmentStatusResponse findById(Long id) {
        return toResponse(EntityLookup.status(statuses, id));
    }

    /**
     * Crea un nuevo estado de cita en el sistema tras validar que no exista otro con el mismo nombre.
     */
    public AppointmentStatusResponse create(AppointmentStatusRequest request) {
        String name = request.name().trim();
        if (statuses.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Ya existe un estado con ese nombre.");
        }
        
        AppointmentStatus status = new AppointmentStatus();
        status.setName(name);
        return toResponse(statuses.save(status));
    }

    /**
     * Actualiza el nombre de un estado de cita existente, validando restricciones de unicidad.
     */
    public AppointmentStatusResponse update(Long id, AppointmentStatusRequest request) {
        AppointmentStatus status = EntityLookup.status(statuses, id);
        String name = request.name().trim();
        
        // Verifica si el nombre cambió y si el nuevo nombre ya está ocupado por otro registro
        if (!status.getName().equalsIgnoreCase(name) && statuses.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Ya existe un estado con ese nombre.");
        }
        
        status.setName(name);
        return toResponse(statuses.save(status));
    }

    /**
     * Elimina un estado de cita del sistema.
     */
    public void delete(Long id) {
        statuses.delete(EntityLookup.status(statuses, id));
    }

    /**
     * Mapea una entidad `AppointmentStatus` a su respectivo DTO de respuesta `AppointmentStatusResponse`.
     */
    private static AppointmentStatusResponse toResponse(AppointmentStatus status) {
        return new AppointmentStatusResponse(status.getId(), status.getName());
    }
}