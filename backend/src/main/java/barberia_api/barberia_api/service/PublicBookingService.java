package barberia_api.barberia_api.service;

import barberia_api.barberia_api.dto.ApiDtos.AppointmentRequest;
import barberia_api.barberia_api.dto.ApiDtos.AppointmentResponse;
import barberia_api.barberia_api.dto.ApiDtos.BarberOptionResponse;
import barberia_api.barberia_api.dto.ApiDtos.PublicAppointmentRequest;
import barberia_api.barberia_api.entity.AppointmentStatus;
import barberia_api.barberia_api.entity.Barber;
import barberia_api.barberia_api.entity.Role;
import barberia_api.barberia_api.entity.User;
import barberia_api.barberia_api.exception.ConflictException;
import barberia_api.barberia_api.repository.AppointmentStatusRepository;
import barberia_api.barberia_api.repository.BarberRepository;
import barberia_api.barberia_api.repository.RoleRepository;
import barberia_api.barberia_api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Servicio de Lógica de Negocio para Reservas Públicas (`PublicBookingService`)
 * Gestiona el flujo de agendamiento abierto desde la Landing Page del cliente final (sin requerir
 * autenticación previa obligatoria). Permite listar barberos activos disponibles y procesa la reserva 
 * creando de forma transparente cuentas de usuario de tipo `CLIENT` bajo demanda si el correo es nuevo.
 */
@Service
@Transactional
public class PublicBookingService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final AppointmentStatusRepository statuses;
    private final BarberRepository barbers;
    private final PasswordEncoder passwordEncoder;
    private final AppointmentService appointments;

    /**
     * Inyección de dependencias por constructor de los repositorios y servicios requeridos.
     */
    public PublicBookingService(
            UserRepository users,
            RoleRepository roles,
            AppointmentStatusRepository statuses,
            BarberRepository barbers,
            PasswordEncoder passwordEncoder,
            AppointmentService appointments
    ) {
        this.users = users;
        this.roles = roles;
        this.statuses = statuses;
        this.barbers = barbers;
        this.passwordEncoder = passwordEncoder;
        this.appointments = appointments;
    }

    /**
     * Consulta y retorna la lista de barberos activos con sus respectivas cuentas de usuario,
     * transformándolos a opciones simplificadas para alimentar el selector gráfico de la interfaz web.
     */
    @Transactional(readOnly = true)
    public List<BarberOptionResponse> findAvailableBarbers() {
        return barbers.findAllActiveWithUsers().stream()
                .map(PublicBookingService::toBarberOption)
                .toList();
    }

    /**
     * Procesa una solicitud de reserva pública realizada por un cliente desde la web.
     * Si el correo electrónico ingresado no existe en el sistema, registra automáticamente al cliente
     * con rol `CLIENT`. Luego, asigna por defecto el estado inicial "PENDIENTE" y delega 
     * la creación formal de la cita al servicio principal (`AppointmentService`).
     */
    public AppointmentResponse create(PublicAppointmentRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        
        // Busca si el cliente ya existe por correo; de lo contrario, lo crea al vuelo (*Just-In-Time Provisioning*)
        User client = users.findByEmailIgnoreCase(email)
                .orElseGet(() -> createClient(request, email));

        // Valida que el usuario encontrado/creado posea estrictamente el rol de cliente
        if (!"CLIENT".equalsIgnoreCase(client.getRole().getName())) {
            throw new ConflictException("No se pudo asociar ese correo a una reserva.");
        }

        // Obtiene el estado inicial predeterminado para nuevas reservas públicas
        AppointmentStatus pending = statuses.findByNameIgnoreCase("PENDIENTE")
                .orElseThrow(() -> new IllegalStateException("No existe el estado inicial PENDIENTE."));

        // Reutiliza la lógica robusta de creación de citas del servicio principal
        return appointments.create(new AppointmentRequest(
                client.getId(),
                request.barberId(),
                pending.getId(),
                request.appointmentDate(),
                request.startTime(),
                request.notes(),
                request.serviceIds(),
                request.paymentMethod()
        ));
    }

    /**
     * Método auxiliar privado para registrar de manera transparente a un nuevo cliente 
     * que agende su primera cita en la barbería. Asigna un rol `CLIENT` y genera un password aleatorio seguro 
     * (ya que el cliente no inició sesión con credenciales previas).
     */
    private User createClient(PublicAppointmentRequest request, String email) {
        Role clientRole = roles.findByNameIgnoreCase("CLIENT")
                .orElseThrow(() -> new IllegalStateException("No existe el rol inicial CLIENT."));

        User client = new User();
        client.setFirstName(request.firstName().trim());
        client.setLastName(request.lastName() == null ? "" : request.lastName().trim());
        client.setEmail(email);
        client.setPhone(request.phone().trim());
        client.setRole(clientRole);
        // Genera un UUID aleatorio cifrado como contraseña temporal para cumplir las restricciones de seguridad Not-Null/BCrypt
        client.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        return users.save(client);
    }

    /**
     * Mapea una entidad `Barber` a su DTO optimizado `BarberOptionResponse` para la vista pública.
     */
    private static BarberOptionResponse toBarberOption(Barber barber) {
        return new BarberOptionResponse(
                barber.getId(),
                barber.getUser().getFirstName(),
                barber.getUser().getLastName(),
                barber.getSpecialty()
        );
    }
}