package barberia_api.barberia_api.service;

import barberia_api.barberia_api.dto.ApiDtos.RoleRequest;
import barberia_api.barberia_api.dto.ApiDtos.RoleResponse;
import barberia_api.barberia_api.entity.Role;
import barberia_api.barberia_api.exception.ConflictException;
import barberia_api.barberia_api.exception.EntityLookup;
import barberia_api.barberia_api.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de Lógica de Negocio para Roles (`RoleService`)
 * Administra las operaciones CRUD del catálogo maestro de roles de usuario en el sistema 
 * (tales como `ADMIN`, `BARBER`, y `CLIENT`), garantizando la normalización de las entradas 
 * y la validación estricta de unicidad por nombre para evitar duplicados.
 */
@Service
@Transactional
public class RoleService {

    private final RoleRepository roles;

    /**
     * Inyección de dependencias por constructor del repositorio de roles.
     */
    public RoleService(RoleRepository roles) {
        this.roles = roles;
    }

    /**
     * Obtiene la lista completa de todos los roles registrados en el sistema.
     */
    @Transactional(readOnly = true)
    public List<RoleResponse> findAll() {
        return roles.findAll().stream().map(RoleService::toResponse).toList();
    }

    /**
     * Busca y retorna un rol específico mediante su ID único.
     */
    @Transactional(readOnly = true)
    public RoleResponse findById(Long id) {
        return toResponse(EntityLookup.role(roles, id));
    }

    /**
     * Crea un nuevo rol en el sistema tras normalizar el texto y verificar que no exista 
     * otro rol registrado con el mismo nombre (insensible a mayúsculas/minúsculas).
     */
    public RoleResponse create(RoleRequest request) {
        String name = normalize(request.name());
        if (roles.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Ya existe un rol con ese nombre.");
        }
        
        Role role = new Role();
        role.setName(name);
        return toResponse(roles.save(role));
    }

    /**
     * Actualiza el nombre de un rol existente, validando restricciones de unicidad 
     * únicamente si el nombre fue modificado respecto al valor actual.
     */
    public RoleResponse update(Long id, RoleRequest request) {
        Role role = EntityLookup.role(roles, id);
        String name = normalize(request.name());
        
        if (!role.getName().equalsIgnoreCase(name) && roles.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Ya existe un rol con ese nombre.");
        }
        
        role.setName(name);
        return toResponse(roles.save(role));
    }

    /**
     * Elimina un rol del sistema mediante su ID.
     */
    public void delete(Long id) {
        roles.delete(EntityLookup.role(roles, id));
    }

    /**
     * Normaliza la cadena de texto del nombre del rol eliminando espacios en blanco innecesarios.
     */
    private static String normalize(String name) {
        return name.trim();
    }

    /**
     * Mapea una entidad `Role` a su respectivo DTO de respuesta `RoleResponse`.
     */
    private static RoleResponse toResponse(Role role) {
        return new RoleResponse(role.getId(), role.getName());
    }
}