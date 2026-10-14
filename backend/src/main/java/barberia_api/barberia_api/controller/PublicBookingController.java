package barberia_api.barberia_api.controller;

import barberia_api.barberia_api.dto.ApiDtos.AppointmentResponse;
import barberia_api.barberia_api.dto.ApiDtos.BarberOptionResponse;
import barberia_api.barberia_api.dto.ApiDtos.PublicAppointmentRequest;
import barberia_api.barberia_api.service.PublicBookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST Público para el Agendamiento de Clientes (`PublicBookingController`)
 * Expone endpoints bajo la ruta base `/api/public` diseñados específicamente para ser consumidos
 * por la Landing Page de la barbería. Permite a los usuarios visitantes consultar los barberos 
 * disponibles y agendar citas de manera libre **sin requerir autenticación previa** (sin HTTP Basic).
 */
@RestController
@RequestMapping("/api/public")
public class PublicBookingController {

    private final PublicBookingService bookings;

    /**
     * Inyección de dependencias por constructor del servicio de reservas públicas.
     * 
     * @param bookings - Servicio encargado de procesar la lógica de negocio para las reservas de clientes externos.
     */
    public PublicBookingController(PublicBookingService bookings) {
        this.bookings = bookings;
    }

    /**
     * Endpoint GET para listar los barberos activos y disponibles (`/api/public/barbers`).
     * Es consumido directamente por el selector desplegable de barberos en el formulario 
     * de la landing page de React que analizamos anteriormente.
     * 
     * @return Una lista optimizada de DTOs (`BarberOptionResponse`) con la información básica de los barberos.
     */
    @GetMapping("/barbers")
    public List<BarberOptionResponse> findAvailableBarbers() {
        return bookings.findAvailableBarbers();
    }

    /**
     * Endpoint POST para la creación pública de citas (`/api/public/appointments`).
     * Permite a cualquier cliente visitante agendar su cita ingresando su nombre, teléfono, 
     * correo y seleccionando su servicio y barbero favorito.
     * 
     * @param request - DTO validado (`PublicAppointmentRequest`) con toda la información de la reserva del cliente.
     * @return Una respuesta HTTP 201 Created con la URI del recurso creado en el panel interno y los datos de la cita.
     */
    @PostMapping("/appointments")
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody PublicAppointmentRequest request) {
        AppointmentResponse created = bookings.create(request);
        // Retorna la URI apuntando al recurso interno correspondiente en /api/appointments/{id}
        return ResponseEntity.created(URI.create("/api/appointments/" + created.id())).body(created);
    }
}