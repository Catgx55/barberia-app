package barberia_api.barberia_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración del Codificador de Contraseñas (`PasswordConfig`)
 * Define el bean encargado del cifrado unidireccional y la verificación segura
 * de las contraseñas de los usuarios en el sistema de la barbería.
 */
@Configuration
public class PasswordConfig {

    /**
     * Registra un bean de tipo `PasswordEncoder` utilizando el algoritmo BCrypt.
     * 
     * @return Una instancia de `BCryptPasswordEncoder` para encriptar contraseñas de forma robusta.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        // BCrypt genera automáticamente un "salt" (sal) aleatorio y aplica un factor de costo
        // computacional, protegiendo las contraseñas contra ataques de diccionario y fuerza bruta.
        return new BCryptPasswordEncoder();
    }
}
