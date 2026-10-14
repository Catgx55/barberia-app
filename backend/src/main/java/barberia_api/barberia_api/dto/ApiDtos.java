package barberia_api.barberia_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Contenedor Central de Objetos de Transferencia de Datos (DTOs - Data Transfer Objects) (`ApiDtos`)
 * Agrupa todos los registros (*Records*) utilizados para la comunicación entre la API REST (backend)
 * y los clientes (frontend), tanto para las peticiones entrantes como para las respuestas salientes.
 * 
 * Nota: El uso de respuestas explícitas previene filtrar datos sensibles (como contraseñas) 
 * y evita problemas de recursividad o inicialización perezosa de Hibernate.
 */
public final class ApiDtos {

    // Constructor privado para evitar la instanciación de esta clase contenedora utilitaria
    private ApiDtos() {
    }

    // =========================================================================
    // ROLES
    // =========================================================================

    public record RoleRequest(
            @NotBlank @Size(max = 60) String name
    ) {
    }

    public record RoleResponse(
            Long id, 
            String name
    ) {
    }

    // =========================================================================
    // SERVICIOS
    // =========================================================================

    public record ServiceRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 500) String description,
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal price,
            @NotNull @Positive Integer durationMinutes,
            @NotNull Boolean active
    ) {
    }

    public record ServiceResponse(
            Long id,
            String name,
            String description,
            BigDecimal price,
            Integer durationMinutes,
            boolean active,
            LocalDateTime createdAt
    ) {
    }

    // =========================================================================
    // ESTADOS DE CITAS
    // =========================================================================

    public record AppointmentStatusRequest(
            @NotBlank @Size(max = 60) String name
    ) {
    }

    public record AppointmentStatusResponse(
            Long id, 
            String name
    ) {
    }

    // =========================================================================
    // USUARIOS (ADMINISTRACIÓN / STAFF)
    // =========================================================================

    public record UserRequest(
            @NotBlank @Size(max = 80) String firstName,
            @NotBlank @Size(max = 100) String lastName,
            @NotBlank @Email @Size(max = 190) String email,
            @NotBlank @Pattern(regexp = "^[+0-9() .-]{7,25}$") String phone,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotNull @Positive Long roleId
    ) {
    }

    public record UserResponse(
            Long id,
            String firstName,
            String lastName,
            String email,
            String phone,
            Long roleId,
            String roleName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    // =========================================================================
    // BARBEROS
    // =========================================================================

    public record BarberRequest(
            @NotNull @Positive Long userId,
            @NotBlank @Size(max = 150) String specialty,
            @NotNull Boolean active
    ) {
    }

    public record BarberResponse(
            Long id,
            Long userId,
            String firstName,
            String lastName,
            String email,
            String specialty,
            boolean active,
            LocalDateTime createdAt
    ) {
    }

    // =========================================================================
    // CITAS Y RESERVAS
    // =========================================================================

    /**
     * DTO para la creación o actualización interna de citas (desde el panel de administración).
     */
    public record AppointmentRequest(
            @NotNull @Positive Long clientId,
            @NotNull @Positive Long barberId,
            @NotNull @Positive Long statusId,
            @NotNull LocalDate appointmentDate,
            @NotNull LocalTime startTime,
            @Size(max = 1000) String notes,
            @NotEmpty List<@NotNull @Positive Long> serviceIds,
            @Pattern(regexp = "NEQUI|DAVIPLATA|EFECTIVO") String paymentMethod
    ) {
    }

    /**
     * DTO para la creación pública de citas (consumido directamente por la Landing Page de clientes).
     * Incluye los datos del cliente para registrarlo o vincularlo automáticamente sin requerir sesión previa.
     */
    public record PublicAppointmentRequest(
            @NotBlank @Size(max = 80) String firstName,
            @Size(max = 100) String lastName,
            @NotBlank @Email @Size(max = 190) String email,
            @NotBlank @Pattern(regexp = "^[+0-9() .-]{7,25}$") String phone,
            @NotNull @Positive Long barberId,
            @NotNull LocalDate appointmentDate,
            @NotNull LocalTime startTime,
            @Size(max = 1000) String notes,
            @NotEmpty List<@NotNull @Positive Long> serviceIds,
            @NotBlank @Pattern(regexp = "NEQUI|DAVIPLATA|EFECTIVO") String paymentMethod
    ) {
    }

    /**
     * DTO resumido para listar opciones de barberos disponibles en selectores públicos de la UI.
     */
    public record BarberOptionResponse(
            Long id, 
            String firstName, 
            String lastName, 
            String specialty
    ) {
    }

    /**
     * DTO específico para la actualización parcial del estado de una cita.
     */
    public record AppointmentStatusUpdateRequest(
            @NotNull @Positive Long statusId
    ) {
    }

    /**
     * DTO que representa el detalle individual de un servicio dentro de una cita agendada.
     */
    public record AppointmentDetailResponse(
            Long id,
            Long serviceId,
            String serviceName,
            BigDecimal price
    ) {
    }

    /**
     * DTO de respuesta completa para una cita, combinando datos del cliente, barbero,
     * montos totales, métodos de pago y la lista de servicios asociados.
     */
    public record AppointmentResponse(
            Long id,
            Long clientId,
            String clientName,
            String clientPhone,
            Long barberId,
            String barberName,
            Long statusId,
            String statusName,
            LocalDate appointmentDate,
            LocalTime startTime,
            LocalTime endTime,
            BigDecimal totalAmount,
            String notes,
            String paymentMethod,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<AppointmentDetailResponse> details
    ) {
    }

    public record AppointmentDetailRequest(
            @NotNull @Positive Long appointmentId,
            @NotNull @Positive Long serviceId
    ) {
    }
}