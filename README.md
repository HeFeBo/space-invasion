# Invasion V2

Backend de un juego de estrategia espacial construido con Spring Boot, Spring Data JPA, MySQL y WebSocket.

## Estado actual

El backend ya expone el flujo principal del juego:

- creación del `Leader` inicial y de su primer planeta
- colonización diferida de planetas
- mejora diferida de estructuras
- mejora diferida de investigaciones
- notificaciones en tiempo real por WebSocket

La lógica del juego sigue en evolución, pero la base principal de dominio y API ya está integrada.

## Tecnologías

- Java 21
- Spring Boot 4.0.6
- Spring Web
- Spring Data JPA
- Spring WebSocket
- Spring Validation
- Maven
- MySQL

## Requisitos

- Java 21
- Maven
- MySQL accesible en `localhost:3306`
- Base de datos `invasion2-db`

## Configuración local

El proyecto usa el perfil `local` definido en `src/main/resources/application-local.properties`.

Ese perfil apunta a una instancia MySQL local. Si necesitas otra URL, usuario o contraseña, ajusta ese archivo antes de arrancar la aplicación.

## Arranque

Con el perfil local:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## API HTTP

### Crear el líder inicial y su primer planeta

`POST /api/invasion2`

Crea un nuevo `Leader`, genera un planeta inicial libre y lo asocia al jugador.

Ejemplo:

```bash
curl -X POST http://localhost:8080/api/invasion2
```

### Colonizar un planeta existente

`POST /api/invasion2/coordinate/{galaxy}/{solarSystem}/{position}/{leaderId}`

Programa una colonización diferida para las coordenadas indicadas usando un `Leader` existente.

Ejemplo:

```bash
curl -X POST \
  "http://localhost:8080/api/invasion2/coordinate/1/2/3/1"
```

### Mejorar una estructura

`POST /api/invasion2/struttura/upgrade/{structureId}`

Programa la mejora diferida de una estructura.

Ejemplo:

```bash
curl -X POST \
  "http://localhost:8080/api/invasion2/struttura/upgrade/15"
```

### Mejorar una investigación

`POST /api/invasion2/research/upgrade/{researchId}/{planetId}`

Programa la mejora diferida de una investigación asociada a un planeta.

Ejemplo:

```bash
curl -X POST \
  "http://localhost:8080/api/invasion2/research/upgrade/4/2"
```

## WebSocket

### Endpoint STOMP

`/ws-invasion`

### Broker

- prefijo de aplicación: `/app`
- broker simple: `/topic`

### Topics emitidos

- `/topic/colonization`
- `/topic/struttura/produzione`
- `/topic/planet/research`

Estos topics notifican el resultado de colonizaciones, mejoras de estructuras y mejoras de investigaciones.

### Ejemplo de cliente

Con SockJS/STOMP, conecta al endpoint `/ws-invasion` y suscríbete a alguno de los topics anteriores. Por ejemplo:

- `/topic/colonization`
- `/topic/struttura/produzione`
- `/topic/planet/research`

## Notas de implementación

- Las operaciones de colonización y mejora se ejecutan con retraso programado de 10 segundos.
- La producción de estructuras se gestiona con tareas periódicas en segundo plano.
- El modelo de dominio incluye `Leader`, `Planet`, `Structure`, `Research` y las entidades auxiliares relacionadas con recursos y tipos.
- JPA está configurado para actualizar el esquema automáticamente en desarrollo.

## Build y tests

```bash
./mvnw test
```

## Estructura general

- `controller/` expone la API HTTP.
- `service/` contiene la lógica de negocio y las tareas programadas.
- `configuration/` define el scheduler y el broker WebSocket.
- `model/` contiene las entidades de dominio.
- `repository/` encapsula el acceso a datos.
- `dto/` expone los contratos de respuesta de la API.
