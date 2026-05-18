# Invasion V2

Backend di un gioco di strategia spaziale costruito con Spring Boot, Spring Data JPA e MySQL.

## Stato del progetto

Il progetto è ancora in sviluppo. La struttura principale è già stata realizzata, ma mancano ancora diverse parti del gioco, della logica di business e alcuni endpoint da completare.

## Tecnologie

- Java 21
- Spring Boot 4
- Spring Web
- Spring Data JPA
- Spring WebSocket
- Maven
- MySQL

## Requisiti

- Java 21
- Maven
- MySQL su `localhost:3306`
- Database `invasion2-db`

## Configurazione locale

Il progetto utilizza un profilo locale in `src/main/resources/application-local.properties`.

Questo profilo punta a:

- URL: `jdbc:mysql://localhost:3306/invasion2-db`
- utente: `root`
- driver: `com.mysql.cj.jdbc.Driver`

Se vuoi utilizzare un altro database o credenziali differenti, modifica questo file prima di avviare l’applicazione.

## Avvio dell’applicazione

Con il profilo locale:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Endpoint attuali

### Creazione del leader iniziale e del suo pianeta

`POST /api/invasion2`

Restituisce il pianeta iniziale creato per un nuovo `Leader`.

### Colonizzazione di un pianeta con un leader esistente

`POST /api/invasion2/coordinate/{galaxy}/{solarSystem}/{position}/{leaderId}`

Programma la colonizzazione differita di un pianeta nelle coordinate indicate utilizzando un `Leader` già esistente.

## Note di sviluppo

- La logica di colonizzazione è ancora in evoluzione.
- Alcune parti del gioco non sono ancora complete.
- Alcune entità e servizi sono ancora in fase di refactoring mentre il modello viene completato.

## Struttura generale

- `PlanetController` espone gli endpoint principali di colonizzazione.
- `PlanetService` orchestra la creazione iniziale e la colonizzazione differita.
- `PlanetColonizationTxService` contiene la logica transazionale della colonizzazione.
- `ResearchServiceImpl` gestisce il miglioramento delle ricerche e il consumo delle risorse.

## Build

```bash
./mvnw test
```