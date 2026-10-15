# Demostración de CORS

El frontend de desarrollo se ejecuta en `http://127.0.0.1:5173` y consume el backend en `http://localhost:8080`. Como sus orígenes son distintos, el backend autoriza la comunicación mediante CORS.

## Ejecutar la demostración

1. Inicia el backend desde la carpeta del proyecto:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

2. En otra terminal, inicia el frontend:

   ```sh
   node frontend-dev.mjs
   ```

3. Abre `http://127.0.0.1:5173` y pulsa **Comparar tiempos**.
4. En las herramientas de desarrollo del navegador, abre **Red** y revisa el encabezado `Access-Control-Allow-Origin` de la respuesta: debe mostrar `http://127.0.0.1:5173`.

## Configuración del backend

En `application.properties`:

```properties
app.cors.origins=http://localhost:5173,http://127.0.0.1:5173
```

En `WebConfig.java`:

```java
registry.addMapping("/api/**")
    .allowedOrigins(origins)
    .allowedMethods("GET", "POST", "DELETE", "OPTIONS")
    .allowedHeaders("Content-Type")
    .maxAge(3600);
```

La configuración permite los dos orígenes locales y contempla las solicitudes previas `OPTIONS` del navegador. No es necesario desactivar su seguridad ni utilizar `no-cors`.

## Comprobar el preflight

```sh
curl -i -X OPTIONS http://localhost:8080/api/benchmark -H "Origin: http://127.0.0.1:5173" -H "Access-Control-Request-Method: POST"
```

La respuesta debe incluir:

```http
Access-Control-Allow-Origin: http://127.0.0.1:5173
```

Al abrir la aplicación integrada en `http://localhost:8080`, frontend y backend comparten origen y no necesitan CORS.
