/**
 * Módulo de Comunicación con la API REST (`api.js`)
 * Centraliza las peticiones HTTP del frontend hacia el backend de Spring Boot,
 * gestionando la codificación de credenciales para HTTP Basic Auth y el manejo
 * estandarizado de errores y respuestas de la API.
 */

// Prefijo base para todas las rutas de la API REST
const API_ROOT = '/api';

/**
 * Codifica las credenciales del usuario en formato Base64 para el esquema de autenticación HTTP Basic.
 * Utiliza `TextEncoder` para garantizar la correcta codificación de caracteres especiales, tildes y eñes (UTF-8).
 * 
 * @param {string} email - Correo electrónico del usuario.
 * @param {string} password - Contraseña del usuario.
 * @returns {string} Cabecera de autorización formateada ("Basic <base64>").
 */
export function basicAuthorization(email, password) {
  // Limpia espacios en blanco del correo y convierte las cadenas a bytes UTF-8
  const bytes = new TextEncoder().encode(`${email.trim()}:${password}`);
  let binary = '';
  for (const byte of bytes) binary += String.fromCharCode(byte);
  return `Basic ${btoa(binary)}`;
}

/**
 * Realiza peticiones HTTP asíncronas de manera centralizada hacia el backend.
 * 
 * @param {string} path - Ruta o endpoint de la API (ej. '/services', '/appointments').
 * @param {Object} [options] - Opciones de configuración de la petición.
 * @param {string} [options.method='GET'] - Método HTTP (GET, POST, PATCH, etc.).
 * @param {Object} [options.body] - Cuerpo de la petición (se serializa automáticamente a JSON).
 * @param {string} [options.authorization] - Token o cabecera de autorización HTTP Basic.
 * @param {AbortSignal} [options.signal] - Señal de AbortController para cancelar la petición si es necesario.
 * @returns {Promise<any>} Datos JSON devueltos por el servidor o null si la respuesta está vacía.
 * @throws {Error} Lanza un error descriptivo si falla la conexión o si el servidor responde con códigos de error HTTP.
 */
export async function apiRequest(path, { method = 'GET', body, authorization, signal } = {}) {
  const headers = new Headers();
  
  // Configura cabeceras si se envía un cuerpo o credenciales de autorización
  if (body !== undefined) headers.set('Content-Type', 'application/json');
  if (authorization) headers.set('Authorization', authorization);

  let response;
  try {
    // Ejecuta la petición nativa fetch apuntando al prefijo de la API
    response = await fetch(`${API_ROOT}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
      signal,
    });
  } catch (error) {
    // Propaga el error si fue provocado intencionalmente por un AbortController
    if (error.name === 'AbortError') throw error;
    // Captura errores de red (ej. si el servidor backend en Java Spring Boot no está corriendo)
    throw new Error('No se pudo conectar con la API. Comprueba que Spring Boot esté ejecutándose.');
  }

  // Si la respuesta es 204 No Content, retorna null directamente sin procesar cuerpo
  if (response.status === 204) return null;

  // Detecta el tipo de contenido retornado por el servidor
  const contentType = response.headers.get('content-type') ?? '';
  const payload = contentType.includes('application/json')
    ? await response.json()
    : await response.text();

  // Si la respuesta del servidor no es exitosa (códigos 4xx, 5xx)
  if (!response.ok) {
    const message = typeof payload === 'object' && payload !== null
      ? payload.message ?? payload.error ?? 'La solicitud no se pudo completar.'
      : payload || 'La solicitud no se pudo completar.';
      
    // Traduce el error 401 a un mensaje claro para el usuario
    throw new Error(response.status === 401 ? 'Credenciales inválidas o sesión no autorizada.' : message);
  }

  return payload;
}