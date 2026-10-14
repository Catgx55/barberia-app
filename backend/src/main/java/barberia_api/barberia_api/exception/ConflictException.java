package barberia_api.barberia_api.exception;

/**
 * Excepción personalizada para Conflictos de Datos y Negocio (`ConflictException`)
 * Extiende de `RuntimeException` para representar situaciones en las que una operación del cliente 
 * entra en conflicto con el estado actual de la base de datos (por ejemplo, cruces de horarios en agendas, 
 * intentos de registrar correos electrónicos duplicados, nombres de servicios o roles ya existentes).
 * Al ser capturada por el manejador global de errores, se traduce automáticamente 
 * en una respuesta HTTP 409 Conflict.
 */
public class ConflictException extends RuntimeException {

    /**
     * Constructor que recibe un mensaje descriptivo del conflicto.
     * 
     * @param message - Mensaje detallado que explica la naturaleza del conflicto (ej. "El barbero ya tiene una cita en ese horario", 
     *                  "Ya existe un usuario con ese correo electrónico").
     */
    public ConflictException(String message) {
        super(message);
    }
}