# Verificación de la entrega

Ejecutada el 14 de octubre de 2026 en Windows, Java 25.0.4 y Maven 3.9.11.

- Compilación y empaquetado: correctos (`mvn verify`).
- Pruebas de integración: **3 ejecutadas, 0 fallos, 0 errores**.
- Sintaxis JavaScript del frontend y servidor de desarrollo: correcta.
- Backend empaquetado iniciado; GET `/` y `/api/info` disponibles.
- Página integrada (8080) y frontend separado (5173): HTTP 200.
- Preflight HTTP real desde `http://127.0.0.1:5173`: origen permitido en la respuesta.
- Las pruebas cubren también rechazo de un origen externo y validación de parámetros.
- Vista visual automatizada en navegador: no disponible; la herramienta de navegador no pudo iniciar en este entorno. No se afirma haber validado visualmente todos los tamaños de pantalla.

## Medición real de ejemplo

100.000 registros, 10 repeticiones, después del calentamiento:

| Modalidad | Promedio | Mediana | Consultas SQL |
|---|---:|---:|---:|
| Sin caché de aplicación | 5,87438 ms | 5,49625 ms | 10 |
| Acierto de caché | 0,11178 ms | 0,09720 ms | 0 |

Aceleración observada: **52,55×**; reducción del tiempo medido: **98,10 %**. Son resultados de una ejecución y no una garantía. Las muestras completas y la carga inicial están en `benchmark-example.json`. Ejecuta tu propia comparación para la entrega.

## Particularidad del entorno de prueba

El JDK 25 local falló inicialmente al establecer el socket interno que usa el selector de Tomcat (`Unable to establish loopback connection`). El JAR se pudo ejecutar configurando `jdk.net.unixdomain.tmpdir` hacia la carpeta de trabajo existente de esta sesión. No se cambió la lógica de la aplicación para resolverlo. En una instalación habitual usa los comandos del README; si tu entorno presenta el mismo problema, revisa su carpeta temporal de Java o usa JDK 17/21.

Las pruebas utilizan el contexto Spring y la base reales, sin mocks ni retrasos artificiales. Se excluyeron las dependencias Mockito no utilizadas para evitar la necesidad de adjuntar un agente a la JVM local.
