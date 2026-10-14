package barberia_api.barberia_api.service;

import barberia_api.barberia_api.dto.ApiDtos.BarberRequest;
import barberia_api.barberia_api.dto.ApiDtos.BarberResponse;
import barberia_api.barberia_api.entity.Barber;
import barberia_api.barberia_api.exception.BadRequestException;
import barberia_api.barberia_api.exception.ConflictException;
import barberia_api.barberia_api.exception.EntityLookup;
import barberia_api.barberia_api.repository.BarberRepository;
import barberia_api.barberia_api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de Lógica de Negocio para Barberos (`BarberService`)
 * Administra el perfil profesional de los barberos en el sistema de la barbería. 
 * Controla la vinculación obligatoria con cuentas de usuario que posean el rol `BARBER`, 
 * previene la duplicidad de perfiles por usuario, e implementa un borrado lógico (*soft delete*) 
 * para preservar el historial de citas vinculadas.
 */
@Service
@Transactional
public class BarberService {

    private final BarberRepository barbers;
    private final UserRepository users;

    /**
     * Inyección de dependencias por constructor de los repositorios de barberos y usuarios.
     */
    public BarberService(BarberRepository barbers, UserRepository users) {
        this.barbers = barbers;
        this.users = users;
    }

    /**
     * Obtiene la lista completa de todos los perfiles de barberos junto con la información 
     * de sus cuentas de usuario asociadas, optimizando consultas mediante joins específicos.
     */
    @Transactional(readOnly = true)
    public List<BarberResponse> findAll() {
        return barbers.findAllWithUsers().stream().map(BarberService::toResponse).toList();
    }

    /**
     * Busca y retorna el perfil de un barbero específico mediante su ID único,
     * cargando de forma ansiosa/eficiente los datos de su usuario correspondiente.
     */
    @Transactional(readOnly = true)
    public BarberResponse findById(Long id) {
        Barber barber = barbers.findWithUserById(id)
                .orElseThrow(() -> new barberia_api.barberia_api.exception.ResourceNotFoundException("Barbero", id));
        return toResponse(barber);
    }

    /**
     * Crea un nuevo perfil profesional de barbero. 
     * Valida que el usuario exista, que no posea ya otro perfil de barbero registrado, 
     * y que tenga estrictamente el rol `BARBER`.
     */
    public BarberResponse create(BarberRequest request) {
        if (barbers.existsByUserId(request.userId())) {
            throw new ConflictException("Ese usuario ya tiene un perfil de barbero.");
        }
        Barber barber = new Barber();
        barber.setUser(barberUser(request.userId()));
        apply(barber, request);
        return toResponse(barbers.save(barber));
    }

    /**
     * Actualiza la información del perfil de un barbero (especialidad y estado de actividad),
     * validando restricciones de unicidad si el usuario asociado es modificado.
     */
    public BarberResponse update(Long id, BarberRequest request) {
        Barber barber = EntityLookup.barber(barbers, id);
        if (!barber.getUser().getId().equals(request.userId())
                && barbers.existsByUserIdAndIdNot(request.userId(), id)) {
            throw new ConflictException("Ese usuario ya tiene un perfil de barbero.");
        }
        barber.setUser(barberUser(request.userId()));
        apply(barber, request);
        return toResponse(barbers.save(barber));
    }

    /**
     * Realiza un borrado lógico (*Soft Delete*) del perfil del barbero, cambiando su estado a inactivo (`active = false`).
     * Esta estrategia es crucial en sistemas transaccionales para evitar romper las llaves foráneas 
     * y preservar el historial de citas y reportes financieros pasados en los que haya participado.
     */
    public void delete(Long id) {
        Barber barber = EntityLookup.barber(barbers, id);
        barber.setActive(false);
        barbers.save(barber);
    }

    /**
     * Aplica los valores de la solicitud al objeto entidad, normalizando la especialidad.
     */
    private static void apply(Barber barber, BarberRequest request) {
        barber.setSpecialty(request.specialty().trim());
        barber.setActive(request.active());
    }

    /**
     * Valida la existencia del usuario y asegura que su rol sea explícitamente `BARBER`.
     */
    private barberia_api.barberia_api.entity.User barberUser(Long userId) {
        var user = EntityLookup.user(users, userId);
        if (!"BARBER".equalsIgnoreCase(user.getRole().getName())) {
            throw new BadRequestException("El usuario debe tener el rol BARBER para crear un perfil de barbero.");
        }
        return user;
    }

    /**
     * Mapea una entidad `Barber` a su respectivo DTO de respuesta `BarberResponse`.
     */
    private static BarberResponse toResponse(Barber barber) {
        return new BarberResponse(
                barber.getId(),
                barber.getUser().getId(),
                barber.getUser().getFirstName(),
                barber.getUser().getLastName(),
                barber.getUser().getEmail(),
                barber.getSpecialty(),
                barber.isActive(),
                barber.getCreatedAt()
        );
    }
}