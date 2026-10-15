# El Caballero — Barbería

Aplicación local para gestionar reservas de una barbería. El frontend usa React y Vite; el backend es una API REST Spring Boot que utiliza MySQL.

## Requisitos

- Node.js y npm
- Java 21
- Maven
- MySQL local

## Configuración local

1. Crea en MySQL la base de datos `barberia_el_caballero_db` y una cuenta con permisos sobre ella.
2. Configura las variables de entorno del backend en la terminal donde lo iniciarás:

   ```bash
   export DB_URL='jdbc:mysql://localhost:3306/barberia_el_caballero_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
   export DB_USERNAME='barberia_user'
   export DB_PASSWORD='tu-clave-local-de-mysql'
   export APP_ADMIN_EMAIL='admin@localhost'
   export APP_ADMIN_PASSWORD='reemplaza-por-una-clave-local-de-al-menos-12-caracteres'
   ```

   No guardes contraseñas reales en el repositorio. La contraseña inicial del administrador debe tener entre 12 y 72 bytes UTF-8. Si ambas variables `APP_ADMIN_EMAIL` y `APP_ADMIN_PASSWORD` se omiten, el backend inicia sin crear un administrador bootstrap y lo indica en el log.

3. Inicia el backend:

   ```bash
   cd backend
   mvn spring-boot:run
   ```

4. En otra terminal, instala las dependencias del frontend la primera vez y arranca Vite:

   ```bash
   npm install
   npm run dev
   ```

5. Abre la dirección local que indique Vite, normalmente `http://localhost:5173`.

Por defecto, la API escucha únicamente en `127.0.0.1:8080` y CORS solo permite el frontend local (`localhost` y `127.0.0.1` en el puerto 5173). No expongas el servidor a la red local o a Internet sin revisar autenticación, HTTPS y protección contra abuso. Para cambiar los orígenes CORS locales se puede definir `CORS_ALLOWED_ORIGINS` como una lista separada por comas.

## Base de datos y migraciones

Flyway crea el esquema y los datos iniciales al iniciar por primera vez una base vacía. Hibernate valida el esquema, pero no lo modifica automáticamente. Los cambios de estructura futuros deben agregarse como nuevas migraciones SQL versionadas en `backend/src/main/resources/db/migration`.

Para bases existentes, Flyway está configurado para registrar la estructura existente como versión inicial y no ejecutar sobre ella la migración inicial. Haz una copia de seguridad antes del primer arranque con esta configuración; Hibernate fallará al iniciar si el esquema existente no coincide con las entidades.

## Pruebas y calidad

```bash
npm run lint
npm run build
cd backend
mvn test
```

Las pruebas del backend usan una base H2 en memoria y no requieren conectarse al MySQL local.
