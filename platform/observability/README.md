# Observability

The `observability` service in `platform/docker-compose.yml` runs the Grafana LGTM stack:

| URL | What |
|---|---|
| http://localhost:13000 | Grafana (admin / admin): Prometheus, Tempo and Loki data sources are preconfigured |
| http://localhost:19090 | Prometheus |
| localhost:14318 | OTLP HTTP endpoint |

order-service pushes metrics and traces to `localhost:14318` over OTLP (via `spring-boot-starter-opentelemetry`).
It also exposes `/actuator/prometheus` for pull-based scraping, which WP-09 uses in Kubernetes.

## Folders to fill during the lab

- `dashboards/`: Grafana dashboard JSON exported in WP-09 (JVM, HTTP, Hikari, Kafka, SLO panels)
- `alerts/`: Prometheus alert and burn-rate rules written in WP-09
