# 🚀 Invasion V2

Backend di un gioco di strategia spaziale costruito con Spring Boot, Spring Data JPA, MySQL e WebSocket 🌌🛠️

## 📌 Stato attuale

Il backend espone già il flusso principale del gioco:

- 🌍 creazione del `Leader` iniziale e del suo primo pianeta
- 🛰️ colonizzazione differita dei pianeti
- 🏗️ miglioramento differito delle strutture
- 🔬 miglioramento differito delle ricerche
- ⚡ notifiche in tempo reale tramite WebSocket

La logica del gioco è ancora in evoluzione, ma la base principale di dominio e API è già integrata.

## 🧰 Tecnologie

- Java 21
- Spring Boot 4.0.6
- Spring Web
- Spring Data JPA
- Spring WebSocket
- Spring Validation
- Maven
- MySQL

## 📦 Requisiti

- Java 21
- Maven
- MySQL accessibile su `localhost:3306`
- Database `invasion2-db`

## 🛠️ Configurazione locale

Il progetto utilizza il profilo `local` definito in:

```
src/main/resources/application-local.properties
```

Questo profilo punta a un'istanza MySQL locale. Se hai bisogno di un'altra URL, utente o password, modifica quel file prima di avviare l'applicazione.

## ▶️ Avvio

Con il profilo `local`:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## 🌐 API HTTP

### 🧑‍🚀 Creare il leader iniziale e il suo primo pianeta

`POST /api/invasion2`

Crea un nuovo `Leader`, genera un pianeta iniziale libero e lo associa al giocatore.

Esempio:

```bash
curl -X POST http://localhost:8080/api/invasion2
```

### 🪐 Colonizzare un pianeta esistente

`POST /api/invasion2/coordinate/{galaxy}/{solarSystem}/{position}/{leaderId}`

Programma una colonizzazione differita usando un `Leader` esistente.

Esempio:

```bash
curl -X POST \
  "http://localhost:8080/api/invasion2/coordinate/1/2/3/1"
```

### 🏗️ Migliorare una struttura

`POST /api/invasion2/struttura/upgrade/{structureId}`

Esempio:

```bash
curl -X POST \
  "http://localhost:8080/api/invasion2/struttura/upgrade/15"
```

### 🔬 Migliorare una ricerca

`POST /api/invasion2/research/upgrade/{researchId}/{planetId}`

Esempio:

```bash
curl -X POST \
  "http://localhost:8080/api/invasion2/research/upgrade/4/2"
```

## 📡 WebSocket

**Endpoint STOMP**
`/ws-invasion`

**Broker**

- prefisso applicazione: `/app`
- broker semplice: `/topic`

**Topics emessi**

- `/topic/colonization`
- `/topic/struttura/produzione`
- `/topic/planet/research`

### 🧪 Esempio di client

Con SockJS/STOMP, connettiti a `/ws-invasion` e iscriviti a uno dei topics:

- `/topic/colonization`
- `/topic/struttura/produzione`
- `/topic/planet/research`

## 📝 Note di implementazione

- ⏱️ operazioni di colonizzazione e miglioramento con ritardo programmato di 10 secondi
- 🔄 produzione delle strutture gestita con task periodici
- 🧩 modello di dominio con `Leader`, `Planet`, `Structure`, `Research` e relative entità ausiliarie
- 🛠️ JPA configurato per aggiornare automaticamente lo schema in sviluppo

## 🧪 Build e test

```bash
./mvnw test
```

## 📁 Struttura generale

- `controller/` — espone la API HTTP
- `service/` — logica di business e task programmati
- `configuration/` — scheduler e broker WebSocket
- `model/` — entità di dominio
- `repository/` — accesso ai dati
- `dto/` — contratti di risposta della API