package barberia_api.barberia_api.config;

import barberia_api.barberia_api.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de Autenticación y Carga de Detalles de Usuario (`BarberUserDetailsService`)
 * Implementa la interfaz estándar `UserDetailsService` de Spring Security para conectar
 * el mecanismo de autenticación HTTP Basic con la base de datos relacional de la barbería.
 */
@Service
public class BarberUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    /**
     * Inyección de dependencias por constructor del repositorio de usuarios.
     * 
     * @param users - Repositorio JPA para consultar la entidad de usuarios.
     */
    public BarberUserDetailsService(UserRepository users) {
        this.users = users;
    }

    /**
     * Busca y carga los datos de un usuario a partir de su nombre de usuario (en esta aplicación, su correo electrónico).
     * Es invocado automáticamente por Spring Security durante el proceso de autenticación.
     *
     * @param email - Correo electrónico ingresado por el usuario en las credenciales HTTP Basic.
     * @return Una instancia de `UserDetails` con las credenciales y roles del usuario para la sesión.
     * @throws UsernameNotFoundException si el correo no existe en la base de datos.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Busca el usuario ignorando mayúsculas/minúsculas y eliminando espacios en blanco accidentales
        var user = users.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas."));

        // Mapea la entidad de base de datos al objeto UserDetails estándar que requiere Spring Security
        return User.withUsername(user.getEmail())
                .password(user.getPassword()) // Contraseña cifrada almacenada en la base de datos (ej. BCrypt)
                .roles(user.getRole().getName()) // Asigna el rol asociado (ej. ADMIN o BARBER)
                .build();
    }
}