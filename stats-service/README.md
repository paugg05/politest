# stats-service

Microservice that turns `test.completed` events into aggregated statistics. It is the event-driven part of the [Politest](../README.md) system: it never calls the other services and nobody calls it to record data.

## How it works

```mermaid
flowchart LR
    web["politest (web)"] -- "test.completed" --> topic{{"Kafka topic<br/>3 partitions"}}
    topic --> listener["TestCompletedListener<br/>(inbound adapter)"]
    listener --> usecase["RegisterCompletedTestUseCase"]
    usecase --> domain["CompletedTest<br/>(validates the event)"]
    usecase --> store["CompletedTestStore<br/>(outbound port)"]
    store --> db[("MySQL")]
    api["GET /api/v1/stats"] --> query["QueryStatsUseCase"] --> store
```

1. The web application publishes one JSON event per finished test (see the contract in the [root README](../README.md#event-contract-testcompleted)).
2. `TestCompletedListener` receives it, parses the JSON and passes a command to the use case. Kafka and JSON stay in the adapter.
3. The domain validates every value (event id is a UUID, timestamp is ISO-8601, language is `es`/`ca`/`en`, axes are known, positions are 0–100). An invalid event is rejected, never stored.
4. The store inserts the test. The event id is the primary key, so a second delivery of the same event is detected and ignored.

## Idempotency

Kafka guarantees **at-least-once** delivery: after a crash or a rebalance, a record can be delivered again. The consumer therefore has to be idempotent. Here it is achieved with a primary key on `event_id`; the insert of a duplicate fails, the adapter reports it as such and the use case returns `DUPLICATE`. The test `processingTheSameEventTwiceIsIdempotent` documents the behaviour.

## Layout

```
domain/         CompletedTest, EventId, Language, AxisCode, Percentage, StatsSummary
application/    ports (RegisterCompletedTestUseCase, QueryStatsUseCase, CompletedTestStore) and StatsService
adapter/in/     messaging (Kafka listener) and web (REST controller)
adapter/out/    JDBC persistence
config/         composition root
```

The same ArchUnit rules as the other services keep the dependencies pointing inwards. Note that the Kafka listener is just another inbound adapter, next to the REST controller: the application layer does not know that messages exist.

## API

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/stats` | Total tests, tests per language and average position per axis (one decimal). |

```json
{
  "totalTests": 3,
  "testsByLanguage": { "es": 2, "ca": 1, "en": 0 },
  "averageRightPct": { "ECON": 50.0, "LIBERTY": 20.0 }
}
```

## Running locally

Start Kafka first (`../kafka`), then:

```bash
docker compose up -d      # MySQL on 3309, phpMyAdmin on 8086
mvn spring-boot:run       # http://localhost:8092
```

The consumer group is `stats-service` and starts from the earliest offset, so events published while the service was down are processed when it starts.

## Failure handling today, and what is next

- A record that cannot be processed (for example malformed JSON) is retried by Spring Kafka's default error handler and then skipped with a log line. It is not lost silently, but it is not kept either.
- Planned: back-off between retries, a dead-letter topic for records that keep failing, and integration tests against a real Kafka and MySQL with Testcontainers.
