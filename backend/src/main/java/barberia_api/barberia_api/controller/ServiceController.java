package barberia_api.barberia_api.controller;

import barberia_api.barberia_api.dto.ApiDtos.ServiceRequest;
import barberia_api.barberia_api.dto.ApiDtos.ServiceResponse;
import barberia_api.barberia_api.service.ServiceCatalogService;
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
 * Controlador REST para la Gestión del Catálogo de Servicios (`ServiceController`)
 * Expone los endpoints bajo la ruta base `/api/services` para administrar los servicios
 * ofrecidos por la barbería (nombres, descripciones, precios, duración y estado activo/inactivo).
 */
@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceCatalogService services;

    /**
     * Inyección de dependencias por constructor del servicio de lógica de negocio del catálogo de servicios.
     * 
     * @param services - Servicio encargado de procesar las operaciones sobre los servicios.
     */
    public ServiceController(ServiceCatalogService services) {
        this.services = services;
    }

    /**
     * Endpoint GET para listar todos los servicios registrados en el sistema.
     * Es consumido tanto por el panel de administración como por el componente `Servicios` 
     * en la Landing Page del cliente (para mostrar la parrilla interactiva que revisamos al inicio).
     * 
     * @return Una lista de DTOs (`ServiceResponse`) con la información de los servicios.
     */
    @GetMapping
    public List<ServiceResponse> findAll() {
        return services.findActive();
    }

    @GetMapping("/admin")
    public List<ServiceResponse> findAllForAdmin() {
        return services.findAll();
    }

    /**
     * Endpoint GET para consultar un servicio específico mediante su ID único.
     * 
     * @param id - ID del servicio a consultar.
     * @return El DTO con la información del servicio solicitado.
     */
    @GetMapping("/{id}")
    public ServiceResponse findById(@PathVariable Long id) {
        return services.findById(id);
    }

    /**
     * Endpoint POST para la creación de un nuevo servicio en el catálogo.
     * 
     * @param request - DTO con los datos validados del servicio (`name`, `description`, `price`, `durationMinutes`, `active`).
     * @return Una respuesta HTTP 201 Created con la URI del recurso creado y sus datos.
     */
    @PostMapping
    public ResponseEntity<ServiceResponse> create(@Valid @RequestBody ServiceRequest request) {
        ServiceResponse created = services.create(request);
        return ResponseEntity.created(URI.create("/api/services/" + created.id())).body(created);
    }

    /**
     * Endpoint PUT para actualizar de manera completa un servicio existente.
     * 
     * @param id - ID del servicio a modificar.
     * @param request - DTO con los nuevos datos actualizados.
     * @return El DTO del servicio actualizado.
     */
    @PutMapping("/{id}")
    public ServiceResponse update(@PathVariable Long id, @Valid @RequestBody ServiceRequest request) {
        return services.update(id, request);
    }

    /**
     * Endpoint PATCH para realizar actualizaciones parciales en un servicio existente.
     * 
     * @param id - ID del servicio a modificar.
     * @param request - DTO con los campos parciales a actualizar.
     * @return El DTO del servicio actualizado.
     */
    @PatchMapping("/{id}")
    public ServiceResponse patch(@PathVariable Long id, @Valid @RequestBody ServiceRequest request) {
        return services.update(id, request);
    }

    /**
     * Endpoint DELETE para eliminar un servicio del catálogo del sistema.
     * 
     * @param id - ID del servicio a eliminar.
     * @return Una respuesta HTTP 204 No Content indicando éxito sin cuerpo de respuesta.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        services.delete(id);
        return ResponseEntity.noContent().build();
    }
}