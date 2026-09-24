# ADR-002: Observability and measurement baseline

- **Status:** Accepted
- **Date:** 2026-09-24
- **Work package:** cross-cutting (used from WP-01; deepened in WP-07 and WP-09)

## Context

Every lab ends in a decision backed by evidence. The evidence is only trustworthy if latency is measured as
distributions (not averages), load is generated without coordinated omission, saturation is visible, and a slow
request can be followed from metric to trace to log. The platform already ran `grafana/otel-lgtm` with OTLP metrics,
but had no histograms, no log export, no exemplars, no database metrics and no client-side load results.

## Decision

| Concern | Decision | Alternatives considered |
|---|---|---|
| Metrics transport | Prometheus **scrapes** `/actuator/prometheus` | OTLP push: fewer moving parts locally, but differs from the Kubernetes pull model in WP-09 and does not carry exemplars from Micrometer |
| Latency distribution | Micrometer **percentile histograms** + **SLO buckets** (50/100/200/500 ms, 1 s, 2 s) on `http.server.requests`; histogram on `hikaricp.connections.acquire` | Client-side percentiles (`percentiles: 0.99`): cannot be aggregated across instances |
| Traces | OTLP → Tempo, 100% sampling (lab only) | Tail sampling: production concern, discussed in WP-09 |
| Logs | Logback `OpenTelemetryAppender` 2.28.0-alpha → OTLP → Loki (matches OTel API 1.62.0 managed by Boot 4.1.1) | JSON file + Promtail/Alloy: extra agent, trace ids parsed from text |
| Correlation | Exemplars (`trace_id`) on histogram buckets; `trace_id`/`span_id` on log records | Log-pattern parsing only |
| Database | `postgres-exporter` v0.20.1 with `--collector.stat_statements` | Query `pg_stat_statements` by hand only |
| Load generation | k6 `constant-arrival-rate` in Docker (`grafana/k6:1.2.3`), remote-write to Prometheus as native histograms | Closed-model tools (coordinated omission); terminal-only results |
| Microbenchmarks | JMH with opt-in profilers (`-Pjmh.profilers=gc,stack`) | Timing loops (JIT/DCE traps) |
| Dashboards | One provisioned "JVM Lab — Service Overview" (RED → USE → JVM/GC → DB → client vs server → logs) | Ad-hoc Explore queries: not reproducible |

## Consequences

- Verified end to end on 2026-09-24: server p99 16.7 ms vs k6 client p99 16.8 ms at 40 req/s; 99.89% of requests under
  200 ms; 53 exemplars stored; an exemplar's trace found in Tempo and its log line (same `trace_id`) in Loki; all
  dashboard queries return data.
- `/actuator/prometheus` output grows with histogram buckets (~67 series for one endpoint set); acceptable for the lab,
  and a cardinality discussion point for WP-09.
- Local scraping relies on `host.docker.internal` (native Linux Docker needs the `extra_hosts` line in compose).
- The OTel Logback appender is an alpha artifact; its version must move with the OTel API version Boot manages.
- `prometheus.yaml` replaces the image default and must be re-checked on `otel-lgtm` upgrades.
- Remaining gaps are assigned to work packages (see `platform/observability/README.md`).
