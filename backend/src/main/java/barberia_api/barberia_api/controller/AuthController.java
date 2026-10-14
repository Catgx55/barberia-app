package barberia_api.barberia_api.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la Autenticación y Sesión del Usuario (`AuthController`)
 * Expone endpoints bajo la ruta base `/api/auth` para verificar el estado de la sesión actual
 * y proveer los datos de identidad y roles del usuario autenticado al frontend.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * Endpoint GET para consultar los datos del usuario autenticado actualmente (`/api/auth/me`).
     * Es consumido por el panel del barbero en React inmediatamente después de enviar las credenciales
     * HTTP Basic para confirmar que son válidas y conocer sus roles o permisos.
     * 
     * @param authentication - Objeto de Spring Security que contiene la sesión y autoridades del usuario logueado.
     * @return Un record `AuthenticatedUser` con el correo electrónico y la lista limpia de roles.
     */
    @GetMapping("/me")
    public AuthenticatedUser currentUser(Authentication authentication) {
        // Mapea las autoridades/roles del usuario, eliminando el prefijo interno "ROLE_" 
        // para estandarizar el formato que consume el cliente frontend (ej. "ADMIN", "BARBER").
        List<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                .toList();
                
        return new AuthenticatedUser(authentication.getName(), roles);
    }

    /**
     * Record DTO interno que define la estructura de la respuesta de identidad del usuario actual.
     */
    public record AuthenticatedUser(String email, List<String> roles) {
    }
}