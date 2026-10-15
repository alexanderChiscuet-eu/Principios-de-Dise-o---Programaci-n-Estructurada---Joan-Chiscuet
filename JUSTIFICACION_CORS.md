# Justificación de CORS en Cache Lab

**Asignatura:** ISWZ2202 — Diseño y Arquitectura de Software  
**Actividad:** Principios de Diseño — Programación Estructurada  
**Proyecto:** aplicación Java y Spring Boot con H2, Caffeine y frontend web

## Repositorio público

[Principios de Diseño y Programación Estructurada — Joan Chiscuet](https://github.com/alexanderChiscuet-eu/Principios-de-Dise-o---Programaci-n-Estructurada---Joan-Chiscuet)

El repositorio contiene el backend, el frontend, las pruebas, los datos de prueba y las instrucciones para ejecutar la aplicación.

## Propósito

La aplicación compara el tiempo de lectura de 100.000 productos desde la base de datos y desde una caché de aplicación. El frontend consume los endpoints del backend para iniciar la comparación, mostrar los tiempos, consultar una muestra de productos y vaciar la caché.

## Por qué se necesita CORS en desarrollo

El navegador identifica un origen mediante el protocolo, el host y el puerto. En la demostración de desarrollo, el frontend se sirve desde `http://127.0.0.1:5173` y consume el backend en `http://localhost:8080`. Son orígenes diferentes. También son distintos `http://localhost:5173` y `http://localhost:8080`, aunque compartan el host, porque cambia el puerto.

La política de mismo origen del navegador restringe la lectura de respuestas de otros orígenes por JavaScript. CORS, cuyo nombre significa Cross-Origin Resource Sharing, permite que el servidor autorice explícitamente esa comunicación mediante encabezados HTTP. Por eso la solución se configura en el backend y no desactivando la seguridad del navegador.

## Configuración implementada

La clase `WebConfig` implementa `WebMvcConfigurer` y define las reglas para `/api/**`. Los orígenes autorizados se leen desde `app.cors.origins` en `application.properties`:

```properties
app.cors.origins=http://localhost:5173,http://127.0.0.1:5173
```

```java
registry.addMapping("/api/**")
    .allowedOrigins(origins)
    .allowedMethods("GET", "POST", "DELETE", "OPTIONS")
    .allowedHeaders("Content-Type")
    .maxAge(3600);
```

Se autoriza `GET` para consultar información y productos, `POST` para ejecutar la comparación y `DELETE` para vaciar la caché. `OPTIONS` contempla las solicitudes previas de comprobación del navegador, llamadas preflight. Por ejemplo, una operación DELETE desde otro origen requiere esa comprobación previa; no todas las llamadas GET o POST la necesitan.

`Content-Type` permite declarar el tipo de contenido cuando una solicitud lo utiliza. `maxAge(3600)` permite reutilizar durante una hora una autorización preflight, sujeta a los límites del navegador; este valor no es la duración de la caché Caffeine, que es de diez minutos.

La configuración usa una lista explícita de orígenes en lugar de permitir cualquiera. No habilita credenciales entre orígenes porque esta demostración no utiliza cookies de sesión ni autenticación que lo requiera. `localhost` y `127.0.0.1` se declaran por separado porque el navegador los considera hosts distintos.

## Cómo comprobarlo

1. Iniciar el backend con `.\mvnw.cmd spring-boot:run`.
2. En otra terminal, ejecutar `node frontend-dev.mjs`.
3. Abrir `http://127.0.0.1:5173` y pulsar Comparar tiempos.
4. Revisar en las herramientas de red del navegador el encabezado `Access-Control-Allow-Origin` de las respuestas.

También se puede comprobar la solicitud previa desde una terminal:

```sh
curl -i -X OPTIONS http://localhost:8080/api/benchmark -H "Origin: http://127.0.0.1:5173" -H "Access-Control-Request-Method: POST"
```

La respuesta debe autorizar el origen `http://127.0.0.1:5173`. Las pruebas automatizadas verifican preflight aceptado para los dos orígenes locales y rechazado para un origen externo no autorizado. La comprobación HTTP real de esta entrega también recibió el encabezado de autorización correspondiente.

## Alcance y conclusión

Al abrir la versión integrada en `http://localhost:8080`, el frontend y el backend comparten origen y no requieren CORS. La configuración se utiliza para trabajar con el frontend separado durante el desarrollo.

CORS permite al navegador acceder a las respuestas desde los orígenes autorizados; no reemplaza la autenticación ni protege por sí mismo el API frente a clientes ajenos al navegador. Tampoco publica la aplicación: la visibilidad pública de un repositorio de GitHub permite consultar el código, pero no convierte un servidor localhost en un sitio accesible por Internet.

La solución conserva la política de seguridad del navegador y autoriza expresamente la comunicación necesaria para este laboratorio. Para desplegar el frontend en otro dominio, se deberá añadir su origen exacto a la configuración del backend.

## Referencia

[Spring Framework — CORS en Spring MVC](https://docs.spring.io/spring-framework/reference/web/webmvc-cors.html).
