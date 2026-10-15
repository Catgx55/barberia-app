package barberia_api.barberia_api.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA `Appointment`
 * Representa la tabla principal `appointments` en la base de datos relacional. 
 * Almacena toda la información crítica de una reserva de la barbería: el cliente que asiste, 
 * el barbero asignado, el estado de la cita, las marcas de tiempo (fecha, hora de inicio y fin), 
 * el monto total, notas, método de pago y la colección de detalles (servicios contratados).
 */
@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "details") // Excluye la colección del toString para evitar recursividad infinita o logs pesados
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Cliente que solicita la cita. Relación de muchos a uno con la entidad `User` (`client_id`).
     * Configurada con carga perezosa (`FetchType.LAZY`) para optimizar recursos.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    /**
     * Barbero profesional asignado para atender el servicio. Relación con la entidad `Barber` (`barber_id`).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "barber_id", nullable = false)
    private Barber barber;

    /**
     * Estado operativo actual de la cita (ej. PENDIENTE, CONFIRMADA, COMPLETADA).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "status_id", nullable = false)
    private AppointmentStatus status;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * Monto total monetario de la cita. 
     * Se configura con precisión monetaria estricta en base de datos (`precision = 12, scale = 2`)
     * para evitar errores de redondeo de punto flotante. Inicializado por defecto en cero.
     */
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(length = 1000)
    private String notes;

    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Relación de uno a muchos con los detalles de la cita (`AppointmentDetail`).
     * 
     * - `CascadeType.ALL`: Propaga operaciones de persistencia, actualización y borrado en cascada.
     * - `orphanRemoval = true`: Si un detalle es removido de la lista, Hibernate lo elimina automáticamente de la base de datos.
     */
    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<AppointmentDetail> details = new ArrayList<>();

    /**
     * Método auxiliar de conveniencia (*Helper method*) para sincronizar la relación bidireccional 
     * al añadir un servicio al detalle de la cita.
     * 
     * @param detail - El objeto `AppointmentDetail` a incorporar.
     */
    public void addDetail(AppointmentDetail detail) {
        details.add(detail);
        detail.setAppointment(this);
    }

    /**
     * Método auxiliar de conveniencia para remover un servicio del detalle de la cita 
     * y romper la relación bidireccional correctamente.
     * 
     * @param detail - El objeto `AppointmentDetail` a retirar.
     */
    public void removeDetail(AppointmentDetail detail) {
        details.remove(detail);
        detail.setAppointment(null);
    }

    @PrePersist
    void setCreatedAt() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void setUpdatedAt() {
        updatedAt = LocalDateTime.now();
    }
}