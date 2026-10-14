package barberia_api.barberia_api.service;

import barberia_api.barberia_api.dto.ApiDtos.AppointmentDetailRequest;
import barberia_api.barberia_api.dto.ApiDtos.AppointmentDetailResponse;
import barberia_api.barberia_api.entity.Appointment;
import barberia_api.barberia_api.entity.AppointmentDetail;
import barberia_api.barberia_api.entity.Barber;
import barberia_api.barberia_api.entity.ServiceEntity;
import barberia_api.barberia_api.exception.BadRequestException;
import barberia_api.barberia_api.exception.ConflictException;
import barberia_api.barberia_api.exception.EntityLookup;
import barberia_api.barberia_api.exception.ResourceNotFoundException;
import barberia_api.barberia_api.repository.AppointmentDetailRepository;
import barberia_api.barberia_api.repository.AppointmentRepository;
import barberia_api.barberia_api.repository.BarberRepository;
import barberia_api.barberia_api.repository.ServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Servicio de Lógica de Negocio para los Detalles de Citas (`AppointmentDetailService`)
 * Administra las operaciones atómicas sobre los servicios individuales asociados a una reserva,
 * garantizando la validación de servicios activos, la prevención de duplicados dentro de la misma cita,
 * el bloqueo pesimista/optimista de registros para evitar condiciones de carrera, y la recalculación 
 * automática de los tiempos y montos totales de la agenda.
 */
@Service
@Transactional
public class AppointmentDetailService {

    private final AppointmentDetailRepository details;
    private final AppointmentRepository appointments;
    private final BarberRepository barbers;
    private final ServiceRepository services;
    private final AppointmentService appointmentService;

    /**
     * Inyección de dependencias por constructor de los repositorios y servicios relacionados.
     */
    public AppointmentDetailService(
            AppointmentDetailRepository details,
            AppointmentRepository appointments,
            BarberRepository barbers,
            ServiceRepository services,
            AppointmentService appointmentService
    ) {
        this.details = details;
        this.appointments = appointments;
        this.barbers = barbers;
        this.services = services;
        this.appointmentService = appointmentService;
    }

    @Transactional(readOnly = true)
    public List<AppointmentDetailResponse> findAll() {
        return details.findAll().stream().map(AppointmentService::toDetailResponse).toList();
    }

    @Transactional(readOnly = true)
    public AppointmentDetailResponse findById(Long id) {
        return AppointmentService.toDetailResponse(EntityLookup.detail(details, id));
    }

    /**
     * Agrega un nuevo servicio como detalle a una cita existente.
     * Bloquea la cita y verifica que el servicio esté activo y no se encuentre repetido en la reserva.
     */
    public AppointmentDetailResponse create(AppointmentDetailRequest request) {
        Appointment appointment = lockAppointment(request.appointmentId());
        ServiceEntity service = activeService(request.serviceId());
        ensureServiceNotAlreadyAdded(appointment, service.getId(), null);
        
        // Delega la creación y el recálculo financiero al servicio principal de citas
        AppointmentDetail detail = appointmentService.createDetail(appointment, service);
        appointments.save(appointment);
        return AppointmentService.toDetailResponse(detail);
    }

    /**
     * Actualiza el servicio asociado a un detalle de cita existente o lo reasigna a una cita diferente.
     */
    public AppointmentDetailResponse update(Long id, AppointmentDetailRequest request) {
        AppointmentDetail detail = EntityLookup.detail(details, id);
        Appointment previousAppointment = detail.getAppointment();
        
        // Determina si el detalle se mantiene en la misma cita o se mueve a una nueva
        Appointment targetAppointment = request.appointmentId().equals(previousAppointment.getId())
                ? previousAppointment
                : lockAppointment(request.appointmentId());

        if (targetAppointment == previousAppointment) {
            barbers.findByIdForUpdate(targetAppointment.getBarber().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Barbero", targetAppointment.getBarber().getId()));
        }

        ServiceEntity service = activeService(request.serviceId());
        ensureServiceNotAlreadyAdded(targetAppointment, service.getId(),
                targetAppointment == previousAppointment ? detail.getId() : null);

        detail.setService(service);
        detail.setPrice(service.getPrice());

        // Si cambió de cita, remueve de la anterior y añade a la nueva, recalculando ambas agendas
        if (targetAppointment != previousAppointment) {
            previousAppointment.removeDetail(detail);
            targetAppointment.addDetail(detail);
            appointmentService.recalculateDetails(previousAppointment);
            appointmentService.recalculateDetails(targetAppointment);
        } else {
            appointmentService.recalculateDetails(targetAppointment);
        }
        
        appointments.save(previousAppointment);
        if (targetAppointment != previousAppointment) {
            appointments.save(targetAppointment);
        }
        return AppointmentService.toDetailResponse(detail);
    }

    /**
     * Elimina un servicio específico de una cita y actualiza el monto y duración total de la reserva.
     */
    public void delete(Long id) {
        AppointmentDetail detail = EntityLookup.detail(details, id);
        Appointment appointment = detail.getAppointment();
        
        barbers.findByIdForUpdate(appointment.getBarber().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Barbero", appointment.getBarber().getId()));
                        
        appointment.removeDetail(detail);
        appointmentService.recalculateDetails(appointment);
        appointments.save(appointment);
    }

    /**
     * Bloquea la cita y su barbero correspondiente para prevenir modificaciones concurrentes.
     */
    private Appointment lockAppointment(Long id) {
        Appointment appointment = EntityLookup.appointment(appointments, id);
        Barber barber = EntityLookup.barberForUpdate(barbers, appointment.getBarber().getId());
        appointment.setBarber(barber);
        return appointment;
    }

    /**
     * Valida que el servicio exista y se encuentre activo antes de asociarlo.
     */
    private ServiceEntity activeService(Long id) {
        ServiceEntity service = EntityLookup.service(services, id);
        if (!service.isActive()) {
            throw new BadRequestException("No se pueden agregar servicios inactivos a una cita.");
        }
        return service;
    }

    /**
     * Asegura que un mismo servicio no sea agregado dos veces a la misma cita (evita duplicados).
     */
    private static void ensureServiceNotAlreadyAdded(Appointment appointment, Long serviceId, Long excludedDetailId) {
        boolean alreadyAdded = appointment.getDetails().stream()
                .anyMatch(detail -> !Objects.equals(detail.getId(), excludedDetailId)
                        && detail.getService().getId().equals(serviceId));
        if (alreadyAdded) {
            throw new ConflictException("Ese servicio ya está agregado a la cita.");
        }
    }
}