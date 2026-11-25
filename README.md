# Politest

A multilingual (Catalan, Spanish, English) political compass test. Users answer a short questionnaire and receive their position on eight political axes, together with an affinity score against political parties and historical or contemporary figures.

The project is built as a small system of four Spring Boot applications communicating over REST and Kafka and is used as a practical exercise in clean architecture, domain-driven design and continuous integration.

> **Disclaimer.** The positions assigned to parties and figures are provisional, educational estimates. They are not authoritative statements about any organisation or person.

## System overview

```mermaid
flowchart LR
    user([Browser]) --> web

    subgraph web_ctx [Test and results]
        web["<b>politest</b><br/>web application<br/>:8080"]
        webdb[("MySQL :3306")]
        web --- webdb
    end

    subgraph party_ctx [Party catalogue]
        party["<b>party-service</b><br/>REST API<br/>:8090"]
        partydb[("MySQL :3307")]
        party --- partydb
    end

    subgraph figure_ctx [Figure catalogue]
        figure["<b>figure-service</b><br/>REST API<br/>:8091"]
        figuredb[("MySQL :3308")]
        figure --- figuredb
    end

    subgraph stats_ctx [Statistics]
        stats["<b>stats-service</b><br/>REST API + Kafka consumer<br/>:8092"]
        statsdb[("MySQL :3309")]
        stats --- statsdb
    end

    kafka{{"<b>Kafka</b><br/>topic test.completed<br/>:9092"}}

    web -- "HTTP / JSON" --> party
    web -- "HTTP / JSON" --> figure
    web -. "publishes<br/>test.completed" .-> kafka
    kafka -. "consumes" .-> stats
```

Calls to the catalogues are synchronous (the result page needs the answer). Statistics are asynchronous: the web application publishes an event and moves on, so a slow or unavailable statistics service never affects the person taking the test.

| Service | Responsibility | App port | Database port | phpMyAdmin |
|---|---|---|---|---|
| [`politest`](politest) | Questionnaire, scoring, result pages, share links, legal pages. Server-side rendered with Thymeleaf. | 8080 | 3306 | 8081 |
| [`party-service`](party-service) | Party catalogue and affinity calculation against parties. | 8090 | 3307 | 8083 |
| [`figure-service`](figure-service/figure-service) | Figure catalogue (localised) and affinity calculation against figures. | 8091 | 3308 | 8084 |
| [`stats-service`](stats-service) | Consumes `test.completed` events from Kafka and exposes aggregated statistics. | 8092 | 3309 | 8086 |
| [`kafka`](kafka) | Single-node Kafka in KRaft mode and Kafka UI. | 9092 | n/a | 8085 (Kafka UI) |

Each service owns its database. No service reads another service's tables.

## Context map

```mermaid
flowchart TB
    W["<b>Test and results</b><br/>(politest)<br/>Downstream · conformist"]
    P["<b>Party catalogue</b><br/>(party-service)<br/>Upstream"]
    F["<b>Figure catalogue</b><br/>(figure-service)<br/>Upstream"]
    S["<b>Statistics</b><br/>(stats-service)<br/>Downstream"]

    P -- "published language:<br/>REST + JSON" --> W
    F -- "published language:<br/>REST + JSON" --> W
    W -- "domain event:<br/>test.completed (Kafka)" --> S
```

| Context | Core concepts | Notes |
|---|---|---|
| Test and results | Question, answer, axis score, result, share link | Consumes the other two contexts through HTTP clients and does not model parties or figures itself. |
| Party catalogue | `Party`, `Position`, `AxisScores`, `AxisWeights`, `Affinity` | Aggregate root `Party`; domain service `AffinityCalculator`. |
| Figure catalogue | `Figure`, `Position`, `Language`, `Affinity` | Aggregate root `Figure`; texts are localised, so every query takes a `lang` parameter. |
| Statistics | `CompletedTest`, `EventId`, `StatsSummary` | Fed only by events. Idempotent: the event id is the identity, so a redelivered event changes nothing. |

The only shared contract between the contexts is deliberately small: the eight axis codes (`ECON`, `WELFARE`, `LIBERTY`, `CULTURE`, `NATION`, `ECOLOGY`, `DEMOCRACY`, `CHANGE`) and the 0–100 scale, where 0 is the left pole and 100 is the right pole. It is duplicated rather than shared as a library so that the services stay independently deployable.

### How affinity is computed

For the axes shared by the user and the target, affinity is `100 − weighted mean absolute distance`. Axis weights range from 0 to 10 and default to 1. A result of 100 means identical positions.

### Event contract: `test.completed`

Published by the web application when a person sees their result for the first time. It carries no personal data.

```json
{
  "eventId": "3f2b8c1e-5d4a-4c3b-9a7e-1b2c3d4e5f60",
  "occurredAt": "2026-11-25T14:30:00Z",
  "language": "ca",
  "rightPct": { "ECON": 70, "LIBERTY": 20 }
}
```

The topic has three partitions and the record key is the event id. Delivery is at-least-once, so consumers must tolerate duplicates; `stats-service` does by using the event id as primary key. Producer and consumer each keep their own copy of the schema, and the consumer ignores unknown fields so the producer can add fields without a coordinated release.

## Architecture

`party-service`, `figure-service` and `stats-service` follow a hexagonal (ports and adapters) layout with tactical DDD patterns:

```
domain/         value objects, aggregate roots, domain service, domain exceptions
application/    inbound ports (use cases), outbound ports, application services
adapter/in/     REST controllers and API models
adapter/out/    JDBC persistence adapters
config/         composition root (the only place where Spring wires the layers)
```

The dependency rules are enforced automatically by [ArchUnit](https://www.archunit.org/) tests, so a violation fails the build. See each service's README for details.

The `politest` web application is organised as a modular monolith using Spring Modulith (`affinity`, `catalog`, `figures`, `quiz`, `result`, `scoring`).

## Documentation

| Document | Content |
|---|---|
| [`party-service/README.md`](party-service/README.md) | Hexagonal layout, DDD model, API reference, running locally |
| [`figure-service/figure-service/README.md`](figure-service/figure-service/README.md) | Same for the figure catalogue, including language handling |
| [`stats-service/README.md`](stats-service/README.md) | Event-driven design, idempotent consumer, API, running locally |
| [`jenkins/README.md`](jenkins/README.md) | Jenkins setup in Docker, pipeline description, troubleshooting |

## Technology

Java 25 · Spring Boot 4.1 · Spring Modulith · Thymeleaf · Spring JDBC · Flyway · MySQL 8.4 · Apache Kafka (KRaft) · Maven · JUnit 5 · ArchUnit · Docker Compose · Jenkins

## Running locally

Prerequisites: JDK 25, Maven (or the included wrapper in `politest`), Docker. Start Kafka before the web application and `stats-service`.

Start Kafka and each service's database, then the services. Start the APIs first, since the web application calls them.

```bash
# Kafka and Kafka UI  (broker localhost:9092, UI http://localhost:8085)
cd kafka
docker compose up -d

# party-service  (http://localhost:8090)
cd party-service
docker compose up -d
mvn spring-boot:run

# figure-service  (http://localhost:8091)
cd figure-service/figure-service
docker compose up -d
mvn spring-boot:run

# stats-service  (http://localhost:8092/api/v1/stats)
cd stats-service
docker compose up -d
mvn spring-boot:run

# web application  (http://localhost:8080)
cd politest
docker compose up -d
./mvnw spring-boot:run
```

Finish a test in the web application, then open `http://localhost:8092/api/v1/stats` to see it counted. The event is also visible in Kafka UI under the topic `test.completed`.

Flyway creates the schema and loads the seed data on first start. The service URLs the web application uses can be changed with `app.party-service.url` and `app.figure-service.url`.

The credentials in the compose files are development defaults for local use only.

## Tests and continuous integration

```bash
mvn verify        # inside any of the three modules
```

The suite covers domain rules, application services against in-memory ports, seed data consistency, the Spring wiring and the architecture rules.

A [`Jenkinsfile`](Jenkinsfile) at the repository root builds and tests the four modules in parallel inside a Maven container, archives the jars and publishes the JUnit reports. The Jenkins setup itself runs in Docker and is documented in [`jenkins/README.md`](jenkins/README.md).

<!-- Replace with a screenshot of a green pipeline run: -->
<!-- ![Jenkins pipeline](docs/jenkins-pipeline.png) -->

## Roadmap

- Reliability patterns for messaging: retries with back-off, dead-letter topic, transactional outbox on the producer
- Integration tests with Testcontainers (real Kafka and MySQL)
