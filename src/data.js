/**
 * Archivo de Constantes y Utilidades Globales (`data.js`)
 * Centraliza datos estáticos, configuraciones de UI y funciones de formato compartidas
 * en toda la aplicación de la barbería.
 */

/**
 * Diccionario de URLs de imágenes optimizadas para cada tipo de servicio.
 * Utiliza URLs de Unsplash con parámetros de control de tamaño, calidad y formato automático.
 * Las claves corresponden exactamente al nombre del servicio.
 */
export const IMAGENES_SERVICIOS = {
  'Fade clásico': 'https://images.unsplash.com/photo-1599351431202-1e0f0137899a?w=800&h=520&fit=crop&auto=format',
  'Corte tradicional': 'https://images.unsplash.com/photo-1568339434343-2a640a1a9946?w=800&h=520&fit=crop&auto=format',
  'Mullet moderno': 'https://images.unsplash.com/photo-1647140655214-e4a2d914971f?w=800&h=520&fit=crop&auto=format',
  'Degradado + diseño': 'https://images.unsplash.com/photo-1630827020718-3433092696e7?w=800&h=520&fit=crop&auto=format',
  'Afeitado con navaja': 'https://images.unsplash.com/photo-1621645582931-d1d3e6564943?w=800&h=520&fit=crop&auto=format',
  'Corte + perfilado': 'https://images.unsplash.com/photo-1629189784191-9afdcbcb0398?w=800&h=520&fit=crop&auto=format',
};

/**
 * Lista de métodos de pago aceptados en la barbería.
 * Cada método cuenta con un identificador único, nombre comercial, símbolo/icono y detalles de uso.
 */
export const METODOS_PAGO = [
  { id: 'NEQUI', nombre: 'Nequi', icono: '◉', detalle: 'Transferencia o código QR' },
  { id: 'DAVIPLATA', nombre: 'Daviplata', icono: '◈', detalle: 'Transferencia o código QR' },
  { id: 'EFECTIVO', nombre: 'Efectivo', icono: '＄', detalle: 'Paga en el local al finalizar' },
];

/**
 * Formateador internacionalizado para moneda colombiana (COP - Pesos Colombianos).
 * Configurado para usar el locale 'es-CO' sin decimales (maximumFractionDigits: 0),
 * ideal para mostrar precios limpios en la interfaz de usuario.
 */
export const COP = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
});