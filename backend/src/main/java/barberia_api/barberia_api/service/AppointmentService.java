package barberia_api.barberia_api.service;

import barberia_api.barberia_api.dto.ApiDtos.AppointmentDetailResponse;
import barberia_api.barberia_api.dto.ApiDtos.AppointmentRequest;
import barberia_api.barberia_api.dto.ApiDtos.AppointmentResponse;
import barberia_api.barberia_api.dto.ApiDtos.AppointmentStatusUpdateRequest;
import barberia_api.barberia_api.entity.Appointment;
import barberia_api.barberia_api.entity.AppointmentDetail;
import barberia_api.barberia_api.entity.AppointmentStatus;
import barberia_api.barberia_api.entity.Barber;
import barberia_api.barberia_api.entity.ServiceEntity;
import barberia_api.barberia_api.entity.User;
import barberia_api.barberia_api.exception.BadRequestException;
import barberia_api.barberia_api.exception.ConflictException;
import barberia_api.barberia_api.exception.EntityLookup;
import org.springframework.security.access.AccessDeniedException;
import barberia_api.barberia_api.repository.AppointmentRepository;
import barberia_api.barberia_api.repository.AppointmentStatusRepository;
import barberia_api.barberia_api.repository.BarberRepository;
import barberia_api.barberia_api.repository.ServiceRepository;
import barberia_api.barberia_api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;

/**
 * Servicio de Lógica de Negocio para Citas (`AppointmentService`)
 * Centraliza las reglas de negocio críticas del sistema de la barbería: validación de horarios comerciales,
 * prevención de cruces de agenda, cálculo dinámico de montos y duraciones de servicios,
 * y control de permisos granulares para el personal.
 */
@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointments;
    private final UserRepository users;
    private final BarberRepository barbers;
    private final AppointmentStatusRepository statuses;
    private final ServiceRepository services;

    /**
     * Inyección de dependencias por constructor de los repositorios necesarios.
     */
    public AppointmentService(
            AppointmentRepository appointments,
            UserRepository users,
            BarberRepository barbers,
            AppointmentStatusRepository statuses,
            ServiceRepository services
    ) {
        this.appointments = appointments;
        this.users = users;
        this.barbers = barbers;
        this.statuses = statuses;
        this.services = services;
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findAll() {
        return appointments.findAll().stream().map(AppointmentService::toResponse).toList();
    }

    /**
     * Lista las citas aplicando control de acceso basado en el rol del usuario (Staff).
     * Si es Administrador, retorna todas las citas; si es Barbero, filtra estrictamente
     * por las citas asignadas a su perfil profesional.
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> findAllForStaff(String email, boolean admin) {
        if (admin) return findAll();
        Barber barber = findBarberForStaff(email);
        return appointments.findAllByBarberIdOrderByAppointmentDateAscStartTimeAsc(barber.getId())
                .stream()
                .map(AppointmentService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponse findById(Long id) {
        return toResponse(EntityLookup.appointment(appointments, id));
    }

    @Transactional(readOnly = true)
    public AppointmentResponse findByIdForStaff(Long id, String email, boolean admin) {
        Appointment appointment = EntityLookup.appointment(appointments, id);
        ensureStaffOwnsAppointment(appointment, email, admin);
        return toResponse(appointment);
    }

    /**
     * Crea una nueva cita tras validar restricciones temporales, estado del barbero,
     * integridad de los servicios seleccionados y disponibilidad en el calendario.
     */
    public AppointmentResponse create(AppointmentRequest request) {
        validateAppointmentTime(request.appointmentDate(), request.startTime());
        Barber barber = EntityLookup.barberForUpdate(barbers, request.barberId());
        if (!barber.isActive()) {
            throw new BadRequestException("No se pueden agendar citas con un barbero inactivo.");
        }

        User client = EntityLookup.user(users, request.clientId());
        AppointmentStatus status = EntityLookup.status(statuses, request.statusId());
        
        Appointment appointment = new Appointment();
        appointment.setClient(client);
        appointment.setBarber(barber);
        appointment.setStatus(status);
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setStartTime(request.startTime());
        appointment.setNotes(normalizeNotes(request.notes()));
        appointment.setPaymentMethod(request.paymentMethod());
        updateDetails(appointment, request.serviceIds());

        assertAvailable(appointment, null);
        return toResponse(appointments.save(appointment));
    }

    public AppointmentResponse update(Long id, AppointmentRequest request) {
        validateAppointmentTime(request.appointmentDate(), request.startTime());
        Appointment appointment = EntityLookup.appointment(appointments, id);
        Barber barber = EntityLookup.barberForUpdate(barbers, request.barberId());
        if (!barber.isActive()) {
            throw new BadRequestException("No se pueden agendar citas con un barbero inactivo.");
        }

        appointment.setClient(EntityLookup.user(users, request.clientId()));
        appointment.setBarber(barber);
        appointment.setStatus(EntityLookup.status(statuses, request.statusId()));
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setStartTime(request.startTime());
        appointment.setNotes(normalizeNotes(request.notes()));
        appointment.setPaymentMethod(request.paymentMethod());
        updateDetails(appointment, request.serviceIds());

        assertAvailable(appointment, id);
        return toResponse(appointments.save(appointment));
    }

    public AppointmentResponse updateStatus(
            Long id,
            AppointmentStatusUpdateRequest request,
            String email,
            boolean admin
    ) {
        Appointment appointment = EntityLookup.appointment(appointments, id);
        ensureStaffOwnsAppointment(appointment, email, admin);
        appointment.setStatus(EntityLookup.status(statuses, request.statusId()));
        return toResponse(appointments.save(appointment));
    }

    public void delete(Long id) {
        appointments.delete(EntityLookup.appointment(appointments, id));
    }

    /**
     * Recalcula de forma dinámica el monto total y la duración estimada de la cita
     * sumando los precios y tiempos de cada servicio incluido. Valida también los horarios comerciales.
     */
    void recalculateDetails(Appointment appointment) {
        BigDecimal total = BigDecimal.ZERO;
        long duration = 0;
        for (AppointmentDetail detail : appointment.getDetails()) {
            total = total.add(detail.getPrice());
            duration += detail.getService().getDurationMinutes();
        }

        if (duration == 0) {
            throw new BadRequestException("La cita debe incluir al menos un servicio.");
        }
        if (duration >= 24 * 60) {
            throw new BadRequestException("La duración total de los servicios no puede exceder un día.");
        }

        LocalTime endTime = appointment.getStartTime().plusMinutes(duration);
        validateBusinessHours(appointment.getAppointmentDate(), appointment.getStartTime(), endTime);
        if (!endTime.equals(appointment.getEndTime())) {
            appointment.setEndTime(endTime);
        }
        appointment.setTotalAmount(total);
        assertAvailable(appointment, appointment.getId());
    }

    AppointmentDetail createDetail(Appointment appointment, ServiceEntity service) {
        AppointmentDetail detail = new AppointmentDetail();
        detail.setService(service);
        detail.setPrice(service.getPrice());
        appointment.addDetail(detail);
        recalculateDetails(appointment);
        return detail;
    }

    static AppointmentDetailResponse toDetailResponse(AppointmentDetail detail) {
        return new AppointmentDetailResponse(
                detail.getId(),
                detail.getService().getId(),
                detail.getService().getName(),
                detail.getPrice()
        );
    }

    private void updateDetails(Appointment appointment, List<Long> serviceIds) {
        if (serviceIds.isEmpty()) {
            throw new BadRequestException("La cita debe incluir al menos un servicio.");
        }
        if (new HashSet<>(serviceIds).size() != serviceIds.size()) {
            throw new BadRequestException("No se puede repetir un servicio dentro de la misma cita.");
        }

        appointment.getDetails().clear();
        for (Long serviceId : serviceIds) {
            ServiceEntity service = EntityLookup.service(services, serviceId);
            if (!service.isActive()) {
                throw new BadRequestException("El servicio " + service.getName() + " está inactivo.");
            }

            AppointmentDetail detail = new AppointmentDetail();
            detail.setService(service);
            detail.setPrice(service.getPrice());
            appointment.addDetail(detail);
        }
        recalculateDetails(appointment);
    }

    /**
     * Verifica que no existan cruces o solapamientos de horario con otras citas del mismo barbero.
     */
    private void assertAvailable(Appointment appointment, Long excludeId) {
        if (!appointment.getEndTime().isAfter(appointment.getStartTime())) {
            throw new BadRequestException("La cita debe finalizar el mismo día en que empieza.");
        }
        if (appointments.existsOverlappingAppointment(
                appointment.getBarber().getId(),
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                excludeId
        )) {
            throw new ConflictException("El barbero ya tiene una cita en ese horario.");
        }
    }

    /**
     * Valida que la fecha y hora de la reserva se encuentren en el futuro,
     * utilizando la zona horaria oficial configurada de Colombia (`America/Bogota`).
     */
    private static void validateAppointmentTime(LocalDate date, LocalTime time) {
        if (!LocalDateTime.of(date, time).isAfter(LocalDateTime.now(ZoneId.of("America/Bogota")))) {
            throw new BadRequestException("La fecha y hora de la cita deben ser futuras.");
        }
    }

    /**
     * Valida que la cita se encuentre estrictamente dentro de los horarios de atención de la barbería:
     * - Lunes a Viernes: 8:00 a. m. a 8:00 p. m.
     * - Sábados: 8:00 a. m. a 6:00 p. m.
     * - Domingos: 10:00 a. m. a 4:00 p. m.
     */
    private static void validateBusinessHours(LocalDate date, LocalTime start, LocalTime end) {
        DayOfWeek day = date.getDayOfWeek();
        LocalTime opening = day == DayOfWeek.SUNDAY ? LocalTime.of(10, 0) : LocalTime.of(8, 0);
        LocalTime closing = switch (day) {
            case SATURDAY -> LocalTime.of(18, 0);
            case SUNDAY -> LocalTime.of(16, 0);
            default -> LocalTime.of(20, 0);
        };

        if (start.isBefore(opening) || end.isAfter(closing)) {
            throw new BadRequestException(
                    "El horario es de lunes a viernes de 8:00 a 20:00, sábados de 8:00 a 18:00 "
                            + "y domingos de 10:00 a 16:00."
            );
        }
    }

    private static String normalizeNotes(String notes) {
        return notes == null || notes.isBlank() ? null : notes.trim();
    }

    private Barber findBarberForStaff(String email) {
        return barbers.findByUserEmailIgnoreCase(email)
                .orElseThrow(() -> new AccessDeniedException("La cuenta no tiene un perfil de barbero."));
    }

    private void ensureStaffOwnsAppointment(Appointment appointment, String email, boolean admin) {
        if (!admin && !appointment.getBarber().getId().equals(findBarberForStaff(email).getId())) {
            throw new AccessDeniedException("No puedes consultar ni modificar citas de otro barbero.");
        }
    }

    static AppointmentResponse toResponse(Appointment appointment) {
        List<AppointmentDetailResponse> details = appointment.getDetails().stream()
                .map(AppointmentService::toDetailResponse)
                .toList();
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getClient().getId(),
                (appointment.getClient().getFirstName() + " " + appointment.getClient().getLastName()).trim(),
                appointment.getClient().getPhone(),
                appointment.getBarber().getId(),
                appointment.getBarber().getUser().getFirstName() + " "
                        + appointment.getBarber().getUser().getLastName().trim(),
                appointment.getStatus().getId(),
                appointment.getStatus().getName(),
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getTotalAmount(),
                appointment.getNotes(),
                appointment.getPaymentMethod(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt(),
                details
        );
    }
}