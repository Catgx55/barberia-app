package barberia_api.barberia_api.exception;

import barberia_api.barberia_api.entity.Appointment;
import barberia_api.barberia_api.entity.AppointmentDetail;
import barberia_api.barberia_api.entity.AppointmentStatus;
import barberia_api.barberia_api.entity.Barber;
import barberia_api.barberia_api.entity.Role;
import barberia_api.barberia_api.entity.ServiceEntity;
import barberia_api.barberia_api.entity.User;
import barberia_api.barberia_api.repository.AppointmentDetailRepository;
import barberia_api.barberia_api.repository.AppointmentRepository;
import barberia_api.barberia_api.repository.AppointmentStatusRepository;
import barberia_api.barberia_api.repository.BarberRepository;
import barberia_api.barberia_api.repository.RoleRepository;
import barberia_api.barberia_api.repository.ServiceRepository;
import barberia_api.barberia_api.repository.UserRepository;

/**
 * Utilidad Centralizada de Búsqueda de Entidades (`EntityLookup`)
 * Clase de utilidad final (*Utility class*) que centraliza los métodos de recuperación segura 
 * de registros mediante sus repositorios correspondientes. Si la entidad buscada no existe en la base de datos, 
 * lanza automáticamente una excepción de tipo `ResourceNotFoundException`, garantizando una respuesta 
 * HTTP 404 Not Found limpia y consistente en toda la aplicación.
 */
public final class EntityLookup {

    /**
     * Constructor privado para prevenir la instanciación de esta clase de utilidad.
     */
    private EntityLookup() {
    }

    /**
     * Busca y retorna un rol por su ID, o lanza una excepción si no es encontrado.
     */
    public static Role role(RoleRepository repository, Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Rol", id));
    }

    /**
     * Busca y retorna un servicio del catálogo por su ID, o lanza una excepción si no es encontrado.
     */
    public static ServiceEntity service(ServiceRepository repository, Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Servicio", id));
    }

    /**
     * Busca y retorna un estado de cita por su ID, o lanza una excepción si no es encontrado.
     */
    public static AppointmentStatus status(AppointmentStatusRepository repository, Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Estado de cita", id));
    }

    /**
     * Busca y retorna un usuario por su ID, o lanza una excepción si no es encontrado.
     */
    public static User user(UserRepository repository, Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    /**
     * Busca y retorna un perfil de barbero por su ID, o lanza una excepción si no es encontrado.
     */
    public static Barber barber(BarberRepository repository, Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Barbero", id));
    }

    /**
     * Busca y retorna un perfil de barbero aplicando bloqueo pesimista de escritura para actualizaciones críticas,
     * o lanza una excepción si no es encontrado.
     */
    public static Barber barberForUpdate(BarberRepository repository, Long id) {
        return repository.findByIdForUpdate(id).orElseThrow(() -> new ResourceNotFoundException("Barbero", id));
    }

    /**
     * Busca y retorna una cita por su ID, o lanza una excepción si no es encontrada.
     */
    public static Appointment appointment(AppointmentRepository repository, Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cita", id));
    }

    /**
     * Busca y retorna un detalle de cita por su ID, o lanza una excepción si no es encontrado.
     */
    public static AppointmentDetail detail(AppointmentDetailRepository repository, Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Detalle de cita", id));
    }
}