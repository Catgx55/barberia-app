package barberia_api.barberia_api.config;

import barberia_api.barberia_api.entity.Role;
import barberia_api.barberia_api.entity.User;
import barberia_api.barberia_api.repository.RoleRepository;
import barberia_api.barberia_api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Inicializador de Administrador Bootstrap (`BootstrapAdminInitializer`)
 * Implementa `ApplicationRunner` para ejecutarse automáticamente al arrancar la aplicación
 * de Spring Boot. Su propósito es garantizar la existencia de un usuario Administrador inicial
 * en la base de datos usando credenciales externas.
 */
@Component
public class BootstrapAdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdminInitializer.class);

    private final RoleRepository roles;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    /**
     * Inyección de dependencias por constructor y configuración de credenciales 
     * mediante variables de entorno (`APP_ADMIN_EMAIL` y `APP_ADMIN_PASSWORD`).
     */
    public BootstrapAdminInitializer(
            RoleRepository roles,
            UserRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${APP_ADMIN_EMAIL:}") String email,
            @Value("${APP_ADMIN_PASSWORD:}") String password
    ) {
        this.roles = roles;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    /**
     * Método ejecutado automáticamente al iniciar Spring Boot.
     * 
     * @param args - Argumentos de la aplicación.
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // No crear credenciales compartidas: el administrador bootstrap es opcional.
        if (email.isBlank() && password.isBlank()) {
            log.warn("No se creó el administrador inicial: configura APP_ADMIN_EMAIL y APP_ADMIN_PASSWORD si necesitas acceso administrativo.");
            return;
        }

        // Validación estricta de robustez para las credenciales de administrador provistas
        if (email.isBlank() || password.isBlank()
                || password.getBytes(StandardCharsets.UTF_8).length < 12
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException(
                    "Configura APP_ADMIN_EMAIL y una APP_ADMIN_PASSWORD de 12 a 72 bytes UTF-8 para crear el administrador inicial."
            );
        }

        // Normaliza el correo electrónico a minúsculas y sin espacios
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        // Busca o crea automáticamente el rol "ADMIN" en la base de datos si no existe
        Role adminRole = roles.findByNameIgnoreCase("ADMIN").orElseGet(() -> {
            Role role = new Role();
            role.setName("ADMIN");
            return roles.save(role);
        });

        // Verifica si el correo ya se encuentra registrado en el sistema
        var existing = users.findByEmailIgnoreCase(normalizedEmail);
        if (existing.isPresent()) {
            // Si el correo existe pero pertenece a un rol diferente, lanza una excepción de seguridad
            if (!existing.get().getRole().getName().equalsIgnoreCase("ADMIN")) {
                throw new IllegalStateException(
                        "APP_ADMIN_EMAIL ya pertenece a una cuenta que no es ADMIN."
                );
            }
            // Si ya existe y es ADMIN, termina la ejecución sin hacer duplicados
            return;
        }

        // Crea y guarda la entidad del usuario Administrador inicial en la base de datos
        User admin = new User();
        admin.setFirstName("Administrador");
        admin.setLastName("Barbería");
        admin.setEmail(normalizedEmail);
        admin.setPhone("0000000000");
        // Cifra la contraseña utilizando el codificador seguro (BCrypt por defecto en Spring Security)
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(adminRole);
        
        users.save(admin);
    }
}