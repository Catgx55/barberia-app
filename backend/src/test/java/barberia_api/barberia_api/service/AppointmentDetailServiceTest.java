package barberia_api.barberia_api.service;

import barberia_api.barberia_api.entity.Appointment;
import barberia_api.barberia_api.entity.AppointmentDetail;
import barberia_api.barberia_api.exception.ConflictException;
import barberia_api.barberia_api.repository.AppointmentDetailRepository;
import barberia_api.barberia_api.repository.AppointmentRepository;
import barberia_api.barberia_api.repository.BarberRepository;
import barberia_api.barberia_api.repository.ServiceRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AppointmentDetailServiceTest {

    @Test
    void cannotDeleteTheOnlyServiceFromAnAppointment() {
        AppointmentDetailRepository details = mock(AppointmentDetailRepository.class);
        AppointmentRepository appointments = mock(AppointmentRepository.class);
        BarberRepository barbers = mock(BarberRepository.class);
        ServiceRepository services = mock(ServiceRepository.class);
        AppointmentDetailService service = new AppointmentDetailService(
                details, appointments, barbers, services, mock(AppointmentService.class));

        Appointment appointment = new Appointment();
        AppointmentDetail detail = new AppointmentDetail();
        appointment.addDetail(detail);
        when(details.findById(1L)).thenReturn(Optional.of(detail));

        assertThrows(ConflictException.class, () -> service.delete(1L));
        verifyNoInteractions(appointments, barbers);
    }
}
