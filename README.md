# El Caballero — Barbería

Aplicación local para gestionar reservas de una barbería. El frontend usa React y Vite; el backend es una API REST Spring Boot que utiliza MySQL.

## Requisitos

- Node.js y npm
- Java 21
- Maven
- MySQL local

## Puesta en marcha local

1. Crea la base de datos y un usuario MySQL con permisos sobre ella. Por ejemplo, desde MySQL como administrador:

   ```sql
   CREATE DATABASE barberia_el_caballero_db
     CHARACTER SET utf8mb4
     COLLATE utf8mb4_unicode_ci;
   CREATE USER 'barberia_user'@'localhost' IDENTIFIED BY 'elige-una-clave-local';
   GRANT ALL PRIVILEGES ON barberia_el_caballero_db.* TO 'barberia_user'@'localhost';
   ```

   Si ya tienes la base o el usuario, no vuelvas a ejecutar estas sentencias; configura las variables para que coincidan con tu instalación.

2. En la terminal donde iniciarás el backend, configura las variables de conexión:

   ```bash
   export DB_URL='jdbc:mysql://localhost:3306/barberia_el_caballero_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
   export DB_USERNAME='barberia_user'
   export DB_PASSWORD='la-clave-que-elegiste'
   ```

   No guardes contraseñas reales en el repositorio ni compartas capturas o logs que las contengan.

3. Opcionalmente, configura un administrador inicial. Si se omiten ambas variables, la API inicia sin crear una cuenta de administrador:

   ```bash
   export APP_ADMIN_EMAIL='admin@localhost'
   export APP_ADMIN_PASSWORD='elige-una-clave-local-de-al-menos-12-caracteres'
   ```

   La contraseña debe tener entre 12 y 72 bytes UTF-8. Usa una dirección de correo y una contraseña propias; no copies literalmente los valores de ejemplo.

4. Inicia el backend en esa misma terminal:

   ```bash
   cd backend
   mvn spring-boot:run
   ```

5. En otra terminal, instala las dependencias del frontend (solo la primera vez) y arranca Vite:

   ```bash
   npm install
   npm run dev
   ```

6. Abre la dirección que indique Vite, normalmente `http://localhost:5173`.

Por defecto, la API escucha únicamente en `127.0.0.1:8080` y CORS solo permite el frontend local (`localhost` y `127.0.0.1` en el puerto 5173). El frontend se conecta a la API mediante el proxy de Vite. No expongas la API a la red local o a Internet sin revisar autenticación, HTTPS y protección contra abuso. Para desarrollo local se puede cambiar la lista de orígenes con `CORS_ALLOWED_ORIGINS`, separados por comas.

## Base de datos y migraciones

Flyway crea el esquema y los datos iniciales al iniciar por primera vez una base vacía. Hibernate valida el esquema, pero no lo modifica automáticamente. Los cambios de estructura futuros deben agregarse como nuevas migraciones SQL versionadas en `backend/src/main/resources/db/migration`; no edites una migración que ya se haya aplicado en una base de datos.

Para bases existentes, Flyway está configurado para registrar la estructura como versión inicial y no ejecutar sobre ella la migración `V1`. Antes del primer arranque, haz una copia de seguridad y comprueba que esa base ya tenga el esquema y los datos iniciales esperados (roles y estados); Hibernate fallará al iniciar si el esquema no coincide con las entidades.

## Pruebas y calidad

```bash
npm run lint
npm run build
```

Las pruebas del backend usan una base H2 en memoria y no requieren conectarse al MySQL local:

```bash
cd backend
mvn test
```
