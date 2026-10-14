package barberia_api.barberia_api.controller;

import barberia_api.barberia_api.dto.ApiDtos.BarberRequest;
import barberia_api.barberia_api.dto.ApiDtos.BarberResponse;
import barberia_api.barberia_api.service.BarberService;
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
 * Controlador REST para la Gestión de Barberos (`BarberController`)
 * Expone los endpoints de la API bajo la ruta base `/api/barbers` para administrar
 * la información profesional de los barberos (especialidades, estado de actividad 
 * y vinculación con las cuentas de usuario del sistema).
 */
@RestController
@RequestMapping("/api/barbers")
public class BarberController {

    private final BarberService barbers;

    /**
     * Inyección de dependencias por constructor del servicio de lógica de negocio de barberos.
     * 
     * @param barbers - Servicio encargado de procesar las operaciones sobre los barberos.
     */
    public BarberController(BarberService barbers) {
        this.barbers = barbers;
    }

    /**
     * Endpoint GET para listar todos los barberos registrados en el sistema.
     * 
     * @return Una lista de DTOs (`BarberResponse`) con la información detallada de cada barbero.
     */
    @GetMapping
    public List<BarberResponse> findAll() {
        return barbers.findAll();
    }

    /**
     * Endpoint GET para consultar los datos de un barbero específico mediante su ID único.
     * 
     * @param id - ID del barbero a consultar.
     * @return El DTO con la información del barbero solicitado.
     */
    @GetMapping("/{id}")
    public BarberResponse findById(@PathVariable Long id) {
        return barbers.findById(id);
    }

    /**
     * Endpoint POST para registrar un nuevo barbero en el sistema, vinculándolo a una cuenta de usuario existente 
     * y asignándole su especialidad y estado.
     * 
     * @param request - DTO con los datos validados del barbero (`userId`, `specialty`, `active`).
     * @return Una respuesta HTTP 201 Created con la URI del recurso creado y sus datos.
     */
    @PostMapping
    public ResponseEntity<BarberResponse> create(@Valid @RequestBody BarberRequest request) {
        BarberResponse created = barbers.create(request);
        return ResponseEntity.created(URI.create("/api/barbers/" + created.id())).body(created);
    }

    /**
     * Endpoint PUT para actualizar de forma completa la información de un barbero existente.
     * 
     * @param id - ID del barbero a modificar.
     * @param request - DTO con los nuevos datos actualizados.
     * @return El DTO del barbero actualizado.
     */
    @PutMapping("/{id}")
    public BarberResponse update(@PathVariable Long id, @Valid @RequestBody BarberRequest request) {
        return barbers.update(id, request);
    }

    /**
     * Endpoint PATCH para realizar actualizaciones parciales en la información de un barbero.
     * 
     * @param id - ID del barbero a modificar.
     * @param request - DTO con los campos parciales a actualizar.
     * @return El DTO del barbero actualizado.
     */
    @PatchMapping("/{id}")
    public BarberResponse patch(@PathVariable Long id, @Valid @RequestBody BarberRequest request) {
        return barbers.update(id, request);
    }

    /**
     * Endpoint DELETE para eliminar el registro de un barbero del sistema.
     * 
     * @param id - ID del barbero a eliminar.
     * @return Una respuesta HTTP 204 No Content indicando éxito sin cuerpo de respuesta.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        barbers.delete(id);
        return ResponseEntity.noContent().build();
    }
}