package barberia_api.barberia_api.controller;

import barberia_api.barberia_api.dto.ApiDtos.AppointmentRequest;
import barberia_api.barberia_api.dto.ApiDtos.AppointmentResponse;
import barberia_api.barberia_api.dto.ApiDtos.AppointmentStatusUpdateRequest;
import barberia_api.barberia_api.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
 * Controlador REST para la Gestión de Citas (`AppointmentController`)
 * Expone los endpoints de la API bajo la ruta base `/api/appointments` para que el personal 
 * de la barbería (Administradores y Barberos) pueda consultar, crear, actualizar, modificar 
 * el estado y eliminar citas en el servidor.
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointments;

    /**
     * Inyección de dependencias por constructor del servicio de lógica de negocio de citas.
     * 
     * @param appointments - Servicio encargado de procesar las operaciones de citas.
     */
    public AppointmentController(AppointmentService appointments) {
        this.appointments = appointments;
    }

    /**
     * Endpoint GET para listar todas las citas accesibles según el rol del usuario autenticado.
     * Si es Administrador, verá todas; si es Barbero, visualizará únicamente las suyas.
     * 
     * @param authentication - Objeto de seguridad que contiene la sesión del usuario autenticado.
     * @return Una lista de DTOs (`AppointmentResponse`) con la información de las citas.
     */
    @GetMapping
    public List<AppointmentResponse> findAll(Authentication authentication) {
        return appointments.findAllForStaff(authentication.getName(), isAdmin(authentication));
    }

    /**
     * Endpoint GET para consultar los detalles de una cita específica por su ID.
     * Valida permisos para asegurar que el barbero solo acceda a sus propias citas.
     * 
     * @param id - ID único de la cita.
     * @param authentication - Sesión del usuario autenticado.
     * @return El DTO de la cita solicitada.
     */
    @GetMapping("/{id}")
    public AppointmentResponse findById(@PathVariable Long id, Authentication authentication) {
        return appointments.findByIdForStaff(id, authentication.getName(), isAdmin(authentication));
    }

    /**
     * Endpoint POST para la creación de una nueva cita.
     * 
     * @param request - DTO con los datos validados de la reserva.
     * @return Una respuesta HTTP 201 Created con la URI del nuevo recurso creado y sus datos.
     */
    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse created = appointments.create(request);
        return ResponseEntity.created(URI.create("/api/appointments/" + created.id())).body(created);
    }

    /**
     * Endpoint PUT para actualizar de forma completa una cita existente.
     * 
     * @param id - ID de la cita a modificar.
     * @param request - DTO con los nuevos datos actualizados.
     * @return El DTO de la cita actualizada.
     */
    @PutMapping("/{id}")
    public AppointmentResponse update(@PathVariable Long id, @Valid @RequestBody AppointmentRequest request) {
        return appointments.update(id, request);
    }

    /**
     * Endpoint PATCH para actualizaciones parciales de una cita.
     * 
     * @param id - ID de la cita a modificar.
     * @param request - DTO con los campos parciales a actualizar.
     * @return El DTO de la cita actualizada.
     */
    @PatchMapping("/{id}")
    public AppointmentResponse patch(@PathVariable Long id, @Valid @RequestBody AppointmentRequest request) {
        return appointments.update(id, request);
    }

    /**
     * Endpoint PATCH específico para actualizar exclusivamente el estado de una cita 
     * (por ejemplo, pasar de pendiente a confirmada o completada).
     * Hace match directo con la interacción que revisamos en el panel de React de los barberos.
     * 
     * @param id - ID de la cita cuyo estado se va a modificar.
     * @param request - DTO que contiene el nuevo `statusId`.
     * @param authentication - Sesión del usuario autenticado.
     * @return El DTO de la cita con el estado actualizado.
     */
    @PatchMapping("/{id}/status")
    public AppointmentResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentStatusUpdateRequest request,
            Authentication authentication
    ) {
        return appointments.updateStatus(id, request, authentication.getName(), isAdmin(authentication));
    }

    /**
     * Endpoint DELETE para eliminar una cita del sistema.
     * 
     * @param id - ID de la cita a eliminar.
     * @return Una respuesta HTTP 204 No Content indicando éxito sin cuerpo de respuesta.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        appointments.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Método auxiliar privado para verificar si el usuario autenticado posee el rol de Administrador.
     * Revisa las autoridades/roles asociados al token de seguridad.
     * 
     * @param authentication - Objeto de autenticación actual.
     * @return `true` si el usuario es ROLE_ADMIN, de lo contrario `false`.
     */
    private static boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
