package barberia_api.barberia_api.repository;

import barberia_api.barberia_api.entity.AppointmentDetail;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA para los Detalles de Citas (`AppointmentDetailRepository`)
 * Extiende `JpaRepository` para proveer operaciones CRUD completas y capacidades de paginación
 * sobre la entidad intermedia `AppointmentDetail`, la cual vincula las reservas con los servicios contratados.
 */
public interface AppointmentDetailRepository extends JpaRepository<AppointmentDetail, Long> {
}