package barberia_api.barberia_api.controller;

import barberia_api.barberia_api.dto.ApiDtos.AppointmentDetailRequest;
import barberia_api.barberia_api.dto.ApiDtos.AppointmentDetailResponse;
import barberia_api.barberia_api.service.AppointmentDetailService;
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
 * Controlador REST para la Gestión de Detalles de Citas (`AppointmentDetailController`)
 * Expone los endpoints bajo la ruta base `/api/appointment-details` para administrar
 * la relación de los servicios individuales asociados a cada reserva (por ejemplo, cuando 
 * una cita incluye múltiples servicios como corte de cabello y afeitado con navaja).
 */
@RestController
@RequestMapping("/api/appointment-details")
public class AppointmentDetailController {

    private final AppointmentDetailService details;

    /**
     * Inyección de dependencias por constructor del servicio de lógica de negocio de detalles de citas.
     * 
     * @param details - Servicio encargado de procesar las operaciones sobre los detalles de las reservas.
     */
    public AppointmentDetailController(AppointmentDetailService details) {
        this.details = details;
    }

    /**
     * Endpoint GET para listar todos los detalles de citas registrados en el sistema.
     * 
     * @return Una lista de DTOs (`AppointmentDetailResponse`) con la información de los detalles.
     */
    @GetMapping
    public List<AppointmentDetailResponse> findAll() {
        return details.findAll();
    }

    /**
     * Endpoint GET para consultar un detalle de cita específico mediante su ID único.
     * 
     * @param id - ID del detalle de cita a consultar.
     * @return El DTO con la información del detalle solicitado.
     */
    @GetMapping("/{id}")
    public AppointmentDetailResponse findById(@PathVariable Long id) {
        return details.findById(id);
    }

    /**
     * Endpoint POST para la creación de un nuevo detalle de cita.
     * 
     * @param request - DTO con los datos validados que relacionan una cita con un servicio.
     * @return Una respuesta HTTP 201 Created con la URI del recurso creado y sus datos.
     */
    @PostMapping
    public ResponseEntity<AppointmentDetailResponse> create(@Valid @RequestBody AppointmentDetailRequest request) {
        AppointmentDetailResponse created = details.create(request);
        return ResponseEntity.created(URI.create("/api/appointment-details/" + created.id())).body(created);
    }

    /**
     * Endpoint PUT para actualizar de manera completa un detalle de cita existente.
     * 
     * @param id - ID del detalle a modificar.
     * @param request - DTO con los nuevos datos actualizados.
     * @return El DTO del detalle actualizado.
     */
    @PutMapping("/{id}")
    public AppointmentDetailResponse update(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentDetailRequest request
    ) {
        return details.update(id, request);
    }

    /**
     * Endpoint PATCH para actualizaciones parciales de un detalle de cita.
     * 
     * @param id - ID del detalle a modificar.
     * @param request - DTO con los campos a actualizar.
     * @return El DTO del detalle actualizado.
     */
    @PatchMapping("/{id}")
    public AppointmentDetailResponse patch(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentDetailRequest request
    ) {
        return details.update(id, request);
    }

    /**
     * Endpoint DELETE para eliminar un detalle de cita del sistema.
     * 
     * @param id - ID del detalle a eliminar.
     * @return Una respuesta HTTP 204 No Content indicando éxito sin cuerpo de respuesta.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        details.delete(id);
        return ResponseEntity.noContent().build();
    }
}
