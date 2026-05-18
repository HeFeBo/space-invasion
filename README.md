# Invasion V2

Backend de un juego de estrategia espacial construido con Spring Boot, Spring Data JPA y MySQL.

## Estado del proyecto

El proyecto sigue en desarrollo. La base principal ya está montada, pero todavía faltan varias partes del juego, lógica de negocio y endpoints por completar.

## Tecnologías

- Java 21
- Spring Boot 4
- Spring Web
- Spring Data JPA
- Spring WebSocket
- Maven
- MySQL

## Requisitos

- Java 21
- Maven
- MySQL en `localhost:3306`
- Base de datos `invasion2-db`

## Configuración local

El proyecto tiene un perfil local en `src/main/resources/application-local.properties`.

Ese perfil apunta a:

- URL: `jdbc:mysql://localhost:3306/invasion2-db`
- usuario: `root`
- driver: `com.mysql.cj.jdbc.Driver`

Si quieres usar otra base o credenciales, ajusta ese archivo antes de arrancar la app.

## Ejecutar la aplicación

Con el perfil local:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Endpoints actuales

### Crear el líder inicial y su planeta

`POST /api/invasion2`

Devuelve el planeta inicial creado para un nuevo `Leader`.

### Colonizar un planeta con líder existente

`POST /api/invasion2/coordinate/{galaxy}/{solarSystem}/{position}/{leaderId}`

Programa la colonización diferida de un planeta en las coordenadas indicadas usando un `Leader` ya existente.

## Notas de desarrollo

- La lógica de colonización todavía está en evolución.
- Hay partes del juego que aún no están terminadas.
- Algunas entidades y servicios siguen en refactor mientras se completa el modelo.

## Estructura general

- `PlanetController` expone los endpoints principales de colonización.
- `PlanetService` orquesta la creación inicial y la colonización diferida.
- `PlanetColonizationTxService` contiene la lógica transaccional de colonización.
- `ResearchServiceImpl` maneja la subida de investigaciones y consumo de recursos.

## Build

```bash
./mvnw test
```

