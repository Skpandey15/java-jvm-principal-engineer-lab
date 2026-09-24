# WP-09 — Containers, Kubernetes, Observability & SLOs

_Effort: ~2.5 weeks   |   Depends on: WP-02, WP-07, WP-08_

Operate JVM services under container resource controls and connect application/runtime signals to reliability objectives.

## Core topics

- JVM container awareness; -XX:MaxRAMPercentage / InitialRAMPercentage; -XX:ActiveProcessorCount
- CPU requests/limits and CFS throttling; GC and JIT thread counts vs CPU limit
- Memory requests/limits and OOMKilled; container memory budget = heap + non-heap + native (NMT from WP-02)
- Kubernetes probes and startup/readiness/liveness semantics
- Startup and warm-up: AOT cache, CDS, CRaC and GraalVM native image trade-offs
- Horizontal scaling implications for JVM services (warm-up, connection pools × replicas)
- Container image hygiene: minimal base image, non-root user, SBOM (CycloneDX), dependency/CVE scanning
- Delivery pipeline: build → test → image → SBOM/scan → deploy to an ephemeral cluster → smoke test → publish; promotion and rollback strategy
- Micrometer metrics model; Prometheus/Grafana concepts
- Logs, metrics and traces; OpenTelemetry tracing and trace-log correlation
- SLI, SLO, SLA and error budgets; burn-rate alerting
- Latency percentiles and saturation indicators; JVM/GC/thread/DB-pool/Kafka metrics
- Alerting on symptoms vs causes

## Hands-on labs

- [ ] Deploy the reference service to kind with constrained CPU/memory and observe behavior.
- [ ] Reproduce CPU throttling that raises p99 while CPU usage looks below the limit; fix with sizing and ActiveProcessorCount.
- [ ] Create a container OOM scenario and distinguish JVM OOM from OOMKilled using NMT and Kubernetes events.
- [ ] Build a memory budget worksheet for the service and set limits from it.
- [ ] Compare startup time and time-to-steady-state with and without an AOT cache.
- [ ] Expose JVM/application/Hikari/Kafka metrics using Micrometer and traces via OpenTelemetry.
- [ ] Create an SLO for the order API with supporting SLIs and burn-rate alerts.
- [ ] Cause DB-pool saturation and correlate Hikari metrics and traces with p99 latency.
- [ ] Create a minimal production dashboard and incident runbook.
- [ ] Extend the CI/CD pipeline: add a load-test gate on the kind deployment and design promotion (GitOps/canary) and rollback in an ADR.

## Interview defense

- [ ] Why can -Xmx equal to container memory be dangerous?
- [ ] Why can p99 rise when CPU usage is only 60% of the limit?
- [ ] How would you define an SLO for this API?
- [ ] Which metrics tell you thread-pool or DB-pool saturation?
- [ ] Readiness vs liveness?

## Definition of done

See section 12 of the lab document. Commit evidence (JFR files, dumps, benchmark JSON, screenshots) under `notes/`,
write the decision into `docs/adrs/`, and score yourself in `docs/readiness-scorecard.md`.
