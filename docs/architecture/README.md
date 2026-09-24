# Architecture

Reference service (Order Platform) diagrams and design notes live here.

```
 client ──HTTP──▶ order-service ──JDBC/Hikari──▶ PostgreSQL 18
                     │  │
                     │  └──HTTP (timeouts, retry, circuit breaker)──▶ payment-stub
                     │
                     ├──Kafka (order events, outbox)──▶ Kafka 4.2   [WP-04/08]
                     ├──cache──▶ Caffeine / Valkey                  [WP-08]
                     └──OTLP──▶ Grafana LGTM (metrics, traces, logs)

 Toxiproxy sits in front of PostgreSQL (:25432) and payment-stub (:28081) for fault injection.
```

Add a section per work package as the service evolves (lab document, Section 4).
