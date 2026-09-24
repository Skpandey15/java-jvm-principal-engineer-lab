# Observability and measurement

The `observability` service in `platform/docker-compose.yml` runs the Grafana LGTM stack
(`grafana/otel-lgtm`): Grafana, Prometheus, Tempo, Loki, Pyroscope and an OpenTelemetry Collector.
Design decisions: `docs/adrs/ADR-002-observability-and-measurement.md`.

| URL | What |
|---|---|
| http://localhost:13000 | Grafana (admin / admin). Start with **JVM Lab → JVM Lab — Service Overview** |
| http://localhost:19090 | Prometheus (scrapes the services; receives k6 remote-write) |
| http://localhost:13200 | Tempo API |
| http://localhost:14040 | Pyroscope (continuous profiling, WP-07) |
| localhost:14317 / 14318 | OTLP gRPC / HTTP (traces and logs from the services) |

## How the signals flow

| Signal | Path | Why this path |
|---|---|---|
| Metrics | Prometheus **scrapes** `/actuator/prometheus` (jobs `order-service`, `payment-stub`) and `postgres-exporter` | Same pull model as Kubernetes (WP-09); OpenMetrics carries **exemplars** |
| Traces | services push **OTLP** → Tempo | Standard for traces |
| Logs | Logback `OpenTelemetryAppender` → OTLP → Loki, with `trace_id`/`span_id` on every record | Logs link to traces and back without parsing text |
| Load-test results | k6 **remote-write** → Prometheus (native histograms) | Client-side latency next to server-side latency |

Correlation path (verified): p99 panel → exemplar dot → trace in Tempo → "Logs for this span" → Loki lines with the same `trace_id`.

## Measurement conventions (the lab's rules of evidence)

1. **Percentiles come from histograms.** `http.server.requests` and `hikaricp.connections.acquire` publish buckets;
   compute p95/p99 with `histogram_quantile(q, sum by (le) (rate(..._bucket[5m])))`. Never average percentiles.
2. **SLO thresholds are bucket boundaries** (50/100/200/500 ms, 1 s, 2 s), so "share of requests under 200 ms" is exact.
3. **Load is open-model** (k6 `constant-arrival-rate`) to avoid coordinated omission. Compare client p99 (k6) with
   server p99: a gap means queueing outside the handler.
4. **Warm up first, then measure** a steady window; state the window in the write-up.
5. **One variable per experiment**, written hypothesis first, repeat runs, report variance.
6. **Measure saturation, not only latency**: Hikari active/pending, thread states, GC time per second, DB time per statement.
7. **Keep the evidence**: export panels/queries and raw results into the WP's `notes/`, link them from the ADR or incident report.

## Common tasks

```bash
docker compose -f platform/docker-compose.yml up -d postgres postgres-exporter toxiproxy observability
./gradlew :reference-service:order-service:bootRun
RATE=100 DURATION=3m load-tests/run-k6.sh               # results appear in the dashboard's k6 row
./gradlew :benchmarks:jmh -Pjmh.includes=HashMap -Pjmh.profilers=gc
```

- **Dashboards** live in `dashboards/` and are provisioned into the *JVM Lab* folder. After editing a JSON file, run
  `docker compose -f platform/docker-compose.yml restart observability` (file-change detection on Windows bind mounts is unreliable).
  The image's own *JVM Overview* and *RED Metrics* dashboards are also available (the RED ones expect OpenTelemetry
  semantic-convention metric names, so they stay empty for Micrometer services; use the lab dashboard).
- **Prometheus config** is `prometheus.yaml` (replaces the image default; re-check it when upgrading `otel-lgtm`).
- **Pyroscope** receives profiles from a Java agent or eBPF on Linux. Profiling a Windows `bootRun` process is not
  supported; profile the containerized service (WP-07/09).

## Not yet covered (added by later work packages)

| Gap | Work package |
|---|---|
| JDBC spans inside traces (see DB time per request in Tempo) | WP-06 |
| Executor / virtual-thread metrics, Kafka consumer lag | WP-04 |
| Container CPU throttling, kube-state metrics in kind | WP-09 (see `platform/k8s/README.md`) |
| SLO recording rules and multi-window burn-rate alerts (`alerts/`) | WP-09 |
