package barberia_api.barberia_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.nio.charset.StandardCharsets;

/**
 * Configuración de Seguridad de la API (`ApiSecurityConfig`)
 * Define las reglas de control de acceso, políticas de sesiones sin estado (Stateless),
 * esquemas de autenticación HTTP Basic y manejo personalizado de errores de autorización (401/403)
 * para la aplicación de la barbería.
 */
@Configuration
public class ApiSecurityConfig {

    /**
     * Configura y registra el filtro de cadena de seguridad (SecurityFilterChain) principal.
     * 
     * @param http - Objeto HttpSecurity provisto por Spring Security para construir la seguridad web.
     * @return El bean de la cadena de filtros configurado.
     * @throws Exception si ocurre algún error en la construcción de la seguridad.
     */
    @Bean
    SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http
                // Habilita la configuración de CORS utilizando los valores por defecto del proyecto
                .cors(Customizer.withDefaults())
                
                // Deshabilita CSRF ya que la API es RESTful y Stateless (no utiliza cookies de sesión tradicionales)
                .csrf(csrf -> csrf.disable())
                
                // Configura la política de sesiones como STATELESS (Sin estado): el servidor no guarda sesiones HTTP
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Configuración de Autenticación HTTP Basic
                .httpBasic(basic -> basic
                        // Personaliza la respuesta HTTP cuando el usuario no está autenticado (Error 401 Unauthorized)
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(401);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write(
                                    "{\"status\":401,\"error\":\"No autenticado\",\"message\":\"Inicia sesión para continuar.\"}"
                            );
                        })
                        // Configura el procesador de objetos para forzar el juego de caracteres UTF-8 en las credenciales Basic Auth
                        .withObjectPostProcessor(new ObjectPostProcessor<BasicAuthenticationFilter>() {
                            @Override
                            public <O extends BasicAuthenticationFilter> O postProcess(O filter) {
                                filter.setCredentialsCharset(StandardCharsets.UTF_8.name());
                                return filter;
                            }
                        }))
                
                // Configuración de manejo de excepciones y accesos denegados (Error 403 Forbidden)
                .exceptionHandling(errors -> errors.accessDeniedHandler((request, response, exception) -> {
                    response.setStatus(403);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(
                            "{\"status\":403,\"error\":\"Acceso denegado\",\"message\":\"No tienes permisos para esta operación.\"}"
                    );
                }))
                
                // Reglas de autorización basadas en rutas (Endpoints) y roles de usuario
                .authorizeHttpRequests(auth -> auth
                        // 1. Permite solicitudes preliminares CORS (OPTIONS) globalmente para evitar bloqueos del navegador
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        
                        // 2. Endpoints públicos consumidos por la Landing Page de clientes
                        .requestMatchers(HttpMethod.GET, "/api/services").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/public/barbers").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/public/appointments").permitAll()
                        
                        // 3. Endpoints exclusivos para el rol ADMINISTRADOR
                        .requestMatchers("/api/roles/**", "/api/users/**", "/api/appointment-details/**")
                        .hasRole("ADMIN")
                        
                        // 4. Endpoints compartidos para el equipo (ADMIN y BARBER)
                        .requestMatchers(HttpMethod.GET, "/api/auth/me")
                        .hasAnyRole("ADMIN", "BARBER")
                        
                        .requestMatchers(HttpMethod.GET, "/api/appointment-statuses/**")
                        .hasAnyRole("ADMIN", "BARBER")
                        .requestMatchers("/api/appointment-statuses/**").hasRole("ADMIN") // Modificaciones de estado exclusivas de Admin
                        
                        .requestMatchers(HttpMethod.PATCH, "/api/appointments/*/status")
                        .hasAnyRole("ADMIN", "BARBER") // Los barberos pueden actualizar el estado de las citas en su panel
                        
                        .requestMatchers(HttpMethod.GET, "/api/appointments/**")
                        .hasAnyRole("ADMIN", "BARBER")
                        .requestMatchers("/api/appointments/**").hasRole("ADMIN")
                        
                        .requestMatchers("/api/services/**", "/api/barbers/**").hasRole("ADMIN")
                        
                        // 5. Cualquier otra solicitud HTTP no especificada requiere obligatoriamente autenticación
                        .anyRequest().authenticated());

        return http.build();
    }
}