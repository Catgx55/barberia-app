package barberia_api.barberia_api.service;

import barberia_api.barberia_api.dto.ApiDtos.UserRequest;
import barberia_api.barberia_api.dto.ApiDtos.UserResponse;
import barberia_api.barberia_api.entity.Role;
import barberia_api.barberia_api.entity.User;
import barberia_api.barberia_api.exception.ConflictException;
import barberia_api.barberia_api.exception.EntityLookup;
import barberia_api.barberia_api.repository.RoleRepository;
import barberia_api.barberia_api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/**
 * Servicio de Lógica de Negocio para Usuarios (`UserService`)
 * Centraliza la administración de cuentas de usuario del sistema (personal y clientes). 
 * Implementa cifrado seguro de contraseñas mediante BCrypt, validaciones robustas de límites 
 * de longitud en UTF-8 para contraseñas, normalización de correos electrónicos y prevención 
 * estricta de duplicados por email.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;

    /**
     * Inyección de dependencias por constructor de los repositorios y el codificador de contraseñas.
     */
    public UserService(UserRepository users, RoleRepository roles, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return users.findAll().stream().map(UserService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return toResponse(EntityLookup.user(users, id));
    }

    /**
     * Registra un nuevo usuario en el sistema tras validar las restricciones de la contraseña,
     * normalizar el correo electrónico y asegurar que no exista otra cuenta registrada con el mismo email.
     */
    public UserResponse create(UserRequest request) {
        validatePassword(request.password());
        String email = normalizeEmail(request.email());
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Ya existe un usuario con ese correo electrónico.");
        }

        User user = new User();
        apply(user, request, EntityLookup.role(roles, request.roleId()));
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        return toResponse(users.save(user));
    }

    /**
     * Actualiza la información de un usuario existente (datos personales, rol y credenciales),
     * validando la unicidad del correo electrónico en caso de que este haya sido modificado.
     */
    public UserResponse update(Long id, UserRequest request) {
        validatePassword(request.password());
        User user = EntityLookup.user(users, id);
        String email = normalizeEmail(request.email());
        
        // Verifica si el correo cambió y si el nuevo email ya está ocupado por otro usuario
        if (!user.getEmail().equalsIgnoreCase(email) && users.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ConflictException("Ya existe un usuario con ese correo electrónico.");
        }

        apply(user, request, EntityLookup.role(roles, request.roleId()));
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        return toResponse(users.save(user));
    }

    public void delete(Long id) {
        users.delete(EntityLookup.user(users, id));
    }

    /**
     * Método auxiliar privado (*Helper*) para aplicar los datos de la solicitud a la entidad usuario.
     */
    private void apply(User user, UserRequest request, Role role) {
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(normalizeEmail(request.email()));
        user.setPhone(request.phone().trim());
        user.setRole(role);
    }

    /**
     * Normaliza el correo electrónico convirtiéndolo a minúsculas y eliminando espacios sobrantes.
     */
    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Valida de manera estricta que la contraseña no supere los 72 bytes en codificación UTF-8.
     * Esta validación es crítica debido a que el algoritmo de cifrado BCrypt trunca internamente 
     * cualquier contraseña que exceda los 72 bytes, previniendo vulnerabilidades y comportamientos inesperados.
     */
    private static void validatePassword(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new com.elcaballero.barberia.exception.BadRequestException(
                    "La contraseña no puede superar los 72 bytes en UTF-8.");
        }
    }

    /**
     * Mapea una entidad `User` a su respectivo DTO de respuesta `UserResponse` (excluyendo hashes de contraseñas).
     */
    private static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().getId(),
                user.getRole().getName(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}