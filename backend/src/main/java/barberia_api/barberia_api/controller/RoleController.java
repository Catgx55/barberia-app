package barberia_api.barberia_api.controller;

import barberia_api.barberia_api.dto.ApiDtos.RoleRequest;
import barberia_api.barberia_api.dto.ApiDtos.RoleResponse;
import barberia_api.barberia_api.service.RoleService;
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
 * Controlador REST para la Gestión de Roles (`RoleController`)
 * Expone los endpoints bajo la ruta base `/api/roles` para administrar los diferentes roles
 * de seguridad y permisos que pueden ser asignados a los usuarios en el sistema (por ejemplo, `ADMIN`, `BARBER`).
 */
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roles;

    /**
     * Inyección de dependencias por constructor del servicio de lógica de negocio de roles.
     * 
     * @param roles - Servicio encargado de procesar las operaciones CRUD sobre los roles.
     */
    public RoleController(RoleService roles) {
        this.roles = roles;
    }

    /**
     * Endpoint GET para listar todos los roles registrados en el sistema.
     * 
     * @return Una lista de DTOs (`RoleResponse`) con la información de los roles.
     */
    @GetMapping
    public List<RoleResponse> findAll() {
        return roles.findAll();
    }

    /**
     * Endpoint GET para consultar un rol específico mediante su ID único.
     * 
     * @param id - ID del rol a consultar.
     * @return El DTO con la información del rol solicitado.
     */
    @GetMapping("/{id}")
    public RoleResponse findById(@PathVariable Long id) {
        return roles.findById(id);
    }

    /**
     * Endpoint POST para la creación de un nuevo rol en el sistema.
     * 
     * @param request - DTO con el nombre validado del nuevo rol.
     * @return Una respuesta HTTP 201 Created con la URI del recurso creado y sus datos.
     */
    @PostMapping
    public ResponseEntity<RoleResponse> create(@Valid @RequestBody RoleRequest request) {
        RoleResponse created = roles.create(request);
        return ResponseEntity.created(URI.create("/api/roles/" + created.id())).body(created);
    }

    /**
     * Endpoint PUT para actualizar de manera completa un rol existente.
     * 
     * @param id - ID del rol a modificar.
     * @param request - DTO con los nuevos datos actualizados.
     * @return El DTO del rol actualizado.
     */
    @PutMapping("/{id}")
    public RoleResponse update(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        return roles.update(id, request);
    }

    /**
     * Endpoint PATCH para actualizaciones parciales de un rol existente.
     * 
     * @param id - ID del rol a modificar.
     * @param request - DTO con los campos parciales a actualizar.
     * @return El DTO del rol actualizado.
     */
    @PatchMapping("/{id}")
    public RoleResponse patch(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        return roles.update(id, request);
    }

    /**
     * Endpoint DELETE para eliminar un rol del sistema.
     * 
     * @param id - ID del rol a eliminar.
     * @return Una respuesta HTTP 204 No Content indicando éxito sin cuerpo de respuesta.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roles.delete(id);
        return ResponseEntity.noContent().build();
    }
}