package barberia_api.barberia_api.controller;

import barberia_api.barberia_api.dto.ApiDtos.AppointmentStatusRequest;
import barberia_api.barberia_api.dto.ApiDtos.AppointmentStatusResponse;
import barberia_api.barberia_api.service.AppointmentStatusService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para la Gestión de Estados de Citas (`AppointmentStatusController`)
 * Expone los endpoints bajo la ruta base `/api/appointment-statuses` para administrar
 * los diferentes estados posibles que puede tener una reserva (por ejemplo: PENDIENTE, 
 * CONFIRMADA, COMPLETADA, CANCELADA).
 */
@RestController
@RequestMapping("/api/appointment-statuses")
public class AppointmentStatusController {

    private final AppointmentStatusService statuses;

    /**
     * Inyección de dependencias por constructor del servicio de lógica de negocio de estados.
     * 
     * @param statuses - Servicio encargado de procesar las operaciones sobre los estados de las citas.
     */
    public AppointmentStatusController(AppointmentStatusService statuses) {
        this.statuses = statuses;
    }

    /**
     * Endpoint GET para listar todos los estados de citas disponibles en el sistema.
     * Este catálogo es consumido tanto por el panel administrativo como por el panel del barbero 
     * (para poblar los selectores desplegables que vimos en el frontend).
     * 
     * @return Una lista de DTOs (`AppointmentStatusResponse`) con la información de los estados.
     */
    @GetMapping
    public List<AppointmentStatusResponse> findAll() {
        return statuses.findAll();
    }

    /**
     * Endpoint GET para consultar un estado de cita específico mediante su ID único.
     * 
     * @param id - ID del estado a consultar.
     * @return El DTO con la información del estado solicitado.
     */
    @GetMapping("/{id}")
    public AppointmentStatusResponse findById(@PathVariable Long id) {
        return statuses.findById(id);
    }

    /**
     * Endpoint POST para la creación de un nuevo estado de cita.
     * 
     * @param request - DTO con el nombre validado del nuevo estado.
     * @return Una respuesta HTTP 201 Created con la URI del recurso creado y sus datos.
     */
    @PostMapping
    public ResponseEntity<AppointmentStatusResponse> create(@Valid @RequestBody AppointmentStatusRequest request) {
        AppointmentStatusResponse created = statuses.create(request);
        return ResponseEntity.created(URI.create("/api/appointment-statuses/" + created.id())).body(created);
    }

    /**
     * Endpoint PUT para actualizar de manera completa un estado de cita existente.
     * 
     * @param id - ID del estado a modificar.
     * @param request - DTO con los nuevos datos actualizados.
     * @return El DTO del estado actualizado.
     */
    @PutMapping("/{id}")
    public AppointmentStatusResponse update(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentStatusRequest request
    ) {
        return statuses.update(id, request);
    }

    /**
     * Endpoint PATCH para actualizaciones parciales de un estado de cita.
     * 
     * @param id - ID del estado a modificar.
     * @param request - DTO con los campos a actualizar.
     * @return El DTO del estado actualizado.
     */
    @PatchMapping("/{id}")
    public AppointmentStatusResponse patch(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentStatusRequest request
    ) {
        return statuses.update(id, request);
    }

    /**
     * Endpoint DELETE para eliminar un estado de cita del sistema.
     * 
     * @param id - ID del estado a eliminar.
     * @return Una respuesta HTTP 204 No Content indicando éxito sin cuerpo de respuesta.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        statuses.delete(id);
        return ResponseEntity.noContent().build();
    }
}