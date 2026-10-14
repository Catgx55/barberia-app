package barberia_api.barberia_api.exception;

/**
 * Excepción personalizada para Solicitudes Incorrectas (`BadRequestException`)
 * Extiende de `RuntimeException` para representar errores de validación de negocio 
 * o peticiones inválidas enviadas por el cliente. Al ser una excepción no verificada (*unchecked*), 
 * permite que el flujo transaccional se interrumpa limpiamente y sea interceptada 
 * por el manejador global de errores para retornar un código de estado HTTP 400 Bad Request.
 */
public class BadRequestException extends RuntimeException {

    /**
     * Constructor que recibe un mensaje descriptivo del error de validación.
     * 
     * @param message - Mensaje detallado que explica la causa del fallo (ej. restricciones de horario,
     *                  servicios inactivos, longitudes excedidas).
     */
    public BadRequestException(String message) {
        super(message);
    }
}