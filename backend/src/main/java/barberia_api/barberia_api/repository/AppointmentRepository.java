package barberia_api.barberia_api.repository;

import barberia_api.barberia_api.entity.Appointment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Repositorio Spring Data JPA para las Citas (`AppointmentRepository`)
 * Gestiona la persistencia y consultas avanzadas sobre la entidad principal `Appointment`.
 * Incorpora estrategias de optimización de consultas con `@EntityGraph` para prevenir 
 * el problema de consultas N+1 y sentencias JPQL personalizadas para la detección de cruces de agenda.
 */
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Verifica si existen citas asociadas a un barbero específico.
     * 
     * @param barberId - ID del barbero a evaluar.
     * @return `true` si al menos una cita está vinculada; de lo contrario, `false`.
     */
    boolean existsByBarberId(Long barberId);

    /**
     * Sobrescribe el método `findAll` para aplicar un grafo de entidades (`@EntityGraph`),
     * cargando de manera ansiosa y en una sola consulta SQL optimizada todas las relaciones clave:
     * cliente, barbero, usuario del barbero, estado, detalles y los servicios asociados a cada detalle.
     */
    @Override
    @EntityGraph(attributePaths = {
            "client", "barber", "barber.user", "status", "details", "details.service"
    })
    java.util.List<Appointment> findAll();

    /**
     * Consulta todas las citas asignadas a un barbero específico ordenadas cronológicamente 
     * por fecha y hora de inicio, optimizando la carga mediante `@EntityGraph`.
     * 
     * @param barberId - ID del barbero.
     * @return Lista de citas ordenadas para el panel del personal (*Staff*).
     */
    @EntityGraph(attributePaths = {
            "client", "barber", "barber.user", "status", "details", "details.service"
    })
    List<Appointment> findAllByBarberIdOrderByAppointmentDateAscStartTimeAsc(Long barberId);

    /**
     * Consulta JPQL personalizada para validar si existe un solapamiento o cruce de horarios 
     * en la agenda de un barbero. 
     * 
     * Excluye automáticamente de la validación a las citas que se encuentren en estado 'CANCELADA' 
     * y permite omitir una cita específica al realizar actualizaciones (`excludeId`).
     * 
     * @param barberId       - ID del barbero evaluado.
     * @param appointmentDate - Fecha de la cita.
     * @param startTime      - Hora de inicio propuesta.
     * @param endTime        - Hora de finalización calculada.
     * @param excludeId      - ID de la cita a excluir de la validación (útil al modificar una cita existente).
     * @return `true` si hay conflicto de horario; `false` si el espacio está disponible.
     */
    @Query("""
            select case when count(a) > 0 then true else false end
            from Appointment a
            where a.barber.id = :barberId
              and a.appointmentDate = :appointmentDate
              and upper(a.status.name) <> 'CANCELADA'
              and a.startTime < :endTime
              and a.endTime > :startTime
              and (:excludeId is null or a.id <> :excludeId)
            """)
    boolean existsOverlappingAppointment(
            @Param("barberId") Long barberId,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );
}