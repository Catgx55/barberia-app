package barberia_api.barberia_api.exception;

/**
 * Excepción personalizada para Recursos No Encontrados (`ResourceNotFoundException`)
 * Extiende de `RuntimeException` para representar situaciones en las que un recurso solicitado 
 * (por ID) no existe en la base de datos (ej. un usuario, una cita, un servicio o un barbero ausente).
 * Al ser interceptada por el manejador global de errores, genera automáticamente un mensaje 
 * descriptivo estandarizado y se traduce en una respuesta HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructor que recibe el nombre descriptivo del recurso y el ID buscado,
     * construyendo un mensaje de error claro y unificado.
     * 
     * @param resource - Nombre legible de la entidad (ej. "Usuario", "Cita", "Barbero", "Servicio").
     * @param id       - Identificador único numérico que no pudo ser encontrado.
     */
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " con id " + id + " no existe.");
    }
}