package barberia_api.barberia_api.controller;

import barberia_api.barberia_api.dto.ApiDtos.UserRequest;
import barberia_api.barberia_api.dto.ApiDtos.UserResponse;
import barberia_api.barberia_api.service.UserService;
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
 * Controlador REST para la Gestión de Usuarios (`UserController`)
 * Expone los endpoints bajo la ruta base `/api/users` para administrar el registro, 
 * actualización y control de las cuentas de usuario con privilegios de staff 
 * (tanto administradores como barberos) en el sistema.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService users;

    /**
     * Inyección de dependencias por constructor del servicio de lógica de negocio de usuarios.
     * 
     * @param users - Servicio encargado de procesar las operaciones CRUD sobre los usuarios.
     */
    public UserController(UserController users) {
        this.users = users;
    }

    /**
     * Endpoint GET para listar todos los usuarios registrados en el sistema.
     * 
     * @return Una lista de DTOs (`UserResponse`) con la información de los usuarios (excluyendo contraseñas).
     */
    @GetMapping
    public List<UserResponse> findAll() {
        return users.findAll();
    }

    /**
     * Endpoint GET para consultar los datos de un usuario específico mediante su ID único.
     * 
     * @param id - ID del usuario a consultar.
     * @return El DTO con la información del usuario solicitado.
     */
    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id) {
        return users.findById(id);
    }

    /**
     * Endpoint POST para el registro de un nuevo usuario en el sistema.
     * 
     * @param request - DTO con los datos validados del usuario (`firstName`, `lastName`, `email`, `phone`, `password`, `roleId`).
     * @return Una respuesta HTTP 201 Created con la URI del recurso creado y sus datos.
     */
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        UserResponse created = users.create(request);
        return ResponseEntity.created(URI.create("/api/users/" + created.id())).body(created);
    }

    /**
     * Endpoint PUT para actualizar de manera completa la información de un usuario existente.
     * 
     * @param id - ID del usuario a modificar.
     * @param request - DTO con los nuevos datos actualizados.
     * @return El DTO del usuario actualizado.
     */
    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return users.update(id, request);
    }

    /**
     * Endpoint PATCH para realizar actualizaciones parciales en la información de un usuario.
     * 
     * @param id - ID del usuario a modificar.
     * @param request - DTO con los campos parciales a actualizar.
     * @return El DTO del usuario actualizado.
     */
    @PatchMapping("/{id}")
    public UserResponse patch(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return users.update(id, request);
    }

    /**
     * Endpoint DELETE para eliminar una cuenta de usuario del sistema.
     * 
     * @param id - ID del usuario a eliminar.
     * @return Una respuesta HTTP 204 No Content indicando éxito sin cuerpo de respuesta.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        users.delete(id);
        return ResponseEntity.noContent().build();
    }
}