package barberia_api.barberia_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Configuración Global de CORS (Cross-Origin Resource Sharing) (`CorsConfig`)
 * Define las reglas de acceso cruzado entre el servidor backend de Spring Boot
 * y el cliente frontend (aplicación de React / Vite), permitiendo la comunicación segura
 * de las solicitudes HTTP desde orígenes específicos.
 */
@Configuration
public class CorsConfig {

    private final List<String> allowedOrigins;

    public CorsConfig(@Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * Registra un bean de tipo `WebMvcConfigurer` para personalizar las reglas CORS de la API.
     * 
     * @return Una instancia anónima de `WebMvcConfigurer` con las políticas CORS configuradas.
     */
    @Bean
    WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // Aplica la política a todos los endpoints bajo la ruta base /api/
                        .allowedOrigins(allowedOrigins.toArray(String[]::new))
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // Métodos HTTP autorizados
                        .allowedHeaders("Content-Type", "Authorization") // Cabeceras permitidas (clave para enviar HTTP Basic Auth)
                        .maxAge(3600); // Tiempo en segundos que el navegador puede cachear la respuesta de pre-verificación (Pre-flight cache: 1 hora)
            }
        };
    }
}