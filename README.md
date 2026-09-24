# Java / JVM Principal Engineer Lab

A production-first lab for building Principal Engineer depth in Java/JVM internals, concurrency, data access,
Spring Boot, diagnostics, Kubernetes and architecture. It follows the lab document
`Java_JVM_Principal_Engineer_Lab_v1.1.docx`: 10 work packages, one evolving reference service.

**Stack:** JDK 25 LTS · Gradle 9.8 (Groovy DSL) · Spring Boot 4.1.1 · PostgreSQL 18 · Kafka 4.2 · Valkey · Keycloak ·
Toxiproxy · Grafana LGTM · JMH · k6 · kind. The reasons for each choice are in `docs/adrs/ADR-000-platform-and-versions.md`.

## Prerequisites

- Any JDK 17+ to launch Gradle. Gradle downloads the JDK 25 (and JDK 27 comparison) toolchains automatically.
- Docker (Docker Desktop, Rancher Desktop or Podman) for the platform and Testcontainers.
- Optional: [k6](https://k6.io) for load tests, [kind](https://kind.sigs.k8s.io) + kubectl for WP-09,
  JDK Mission Control, async-profiler and Eclipse MAT for diagnostics.

## Quick start

```bash
./gradlew build                                              # compile + test everything
docker compose -f platform/docker-compose.yml up -d          # start the platform
./gradlew :reference-service:payment-stub:bootRun            # terminal 1, port 18081
./gradlew :reference-service:order-service:bootRun           # terminal 2, port 18080
curl -X POST localhost:18080/orders -H 'Content-Type: application/json' -d '{"customerId":"c1","totalAmount":42.50}'
k6 run load-tests/order-create.js                            # open-model load
```

## Port map

Host ports use a lab-specific range so the lab runs next to other local stacks that already use the defaults.

| Component | Host port | Notes |
|---|---|---|
| order-service | 18080 | `/orders`, `/actuator/*` |
| payment-stub | 18081 | `/payments`, `/admin/behaviour` |
| PostgreSQL 18 | 15432 | db `lab`, user/password `lab` |
| PostgreSQL via Toxiproxy | 25432 | point the datasource here for DB fault injection |
| payment-stub via Toxiproxy | 28081 | point the payment client here for downstream fault injection |
| Toxiproxy API | 18474 | |
| Kafka | 19092 | single-node KRaft |
| Valkey | 16379 | |
| Keycloak | 18180 | admin / admin |
| Grafana | 13000 | admin / admin |
| Prometheus | 19090 | |
| OTLP gRPC / HTTP | 14317 / 14318 | order-service exports here |
| kind NodePort | 30080 | order-service in WP-09 |

## Layout

| Path | Purpose |
|---|---|
| `build-logic/` | Convention plugins: `lab.java-conventions`, `lab.spring-boot-conventions`, `lab.jmh-conventions` |
| `gradle/libs.versions.toml` | All versions in one place |
| `reference-service/order-service` | The Order Platform service that grows through the WPs |
| `reference-service/payment-stub` | Downstream with adjustable latency, failure rate and concurrency limit |
| `wp-01` … `wp-10` | Standalone experiments, lab checklist (README) and evidence (`notes/`) per work package |
| `benchmarks/` | JMH suite (`./gradlew :benchmarks:jmh -Pjmh.includes=<regex>`) |
| `load-tests/` | k6 scripts |
| `platform/` | docker compose, Toxiproxy config, kind/Kustomize manifests, observability |
| `docs/` | Roadmap, ADRs, incident reports, interview-defense log, readiness scorecard |
| `.github/` | CI/CD pipeline, CodeQL, Dependabot (see below) |
| `scripts/diagnose.sh` | Capture a JVM evidence bundle (flags, heap, threads, NMT, JFR) from a PID |

## Useful commands

```bash
# Same experiment on JDK 25 vs JDK 27
./gradlew :wp-02-memory-gc:runOnBaselineJdk   -PmainClass=lab.wp02.AllocationPressure -PjvmArgs="-Xmx512m -XX:+UseZGC -Xlog:gc"
./gradlew :wp-02-memory-gc:runOnComparisonJdk -PmainClass=lab.wp02.AllocationPressure -PjvmArgs="-Xmx512m -XX:+UseZGC -Xlog:gc"

# Inject a heap leak into the reference service (WP-02)
./gradlew :reference-service:order-service:bootRun --args='--lab.faults.retain-created-orders=true'

# Slow down the payment dependency (WP-04/08)
curl -X POST 'localhost:18081/admin/behaviour?latencyMs=2000&failureRate=0.2&maxConcurrent=5'

# Add 500 ms latency to every DB call through Toxiproxy (start order-service with --spring.datasource.url=jdbc:postgresql://localhost:25432/lab)
curl -X POST localhost:18474/proxies/postgres/toxics -d '{"type":"latency","attributes":{"latency":500}}'

# Container image for WP-09 (full JDK runtime so jcmd/jfr work in the pod)
./gradlew :reference-service:order-service:bootJar
docker build -f platform/docker/spring-boot.Dockerfile -t lab/order-service:dev reference-service/order-service
```

## CI/CD

GitHub Actions (`.github/workflows/`). All third-party actions are pinned to commit SHAs; Dependabot updates them monthly.

| Job | Runs on | What it proves |
|---|---|---|
| Build and test | PR, main | Gradle build, unit + Testcontainers tests, JMH suite compiles; dependency graph submitted on main |
| Validate platform config | PR, main | actionlint, docker compose config, Kustomize renders |
| Dependency review | PR | No new dependencies with high-severity CVEs |
| Build image, SBOM and scan | PR, main | Dockerfile image builds; CycloneDX SBOM; Trivy report to code scanning; fails on fixable CRITICAL CVEs |
| Deploy to kind and smoke test | PR, main | Image runs in Kubernetes with real limits and probes; create/read order, readiness, validation |
| Publish images to GHCR | main | Pushes `ghcr.io/<owner>/jvm-lab/<service>:<sha>` and `:main` |
| CodeQL (`codeql.yml`) | PR, main, weekly | Static security/quality analysis of the Java code |

Deployment stops at an ephemeral kind cluster on purpose. Promotion to a long-lived environment (GitOps, canary,
rollback) is a WP-09/WP-10 design exercise.

## Working method

For each lab: **learn → implement → break → observe → diagnose → fix → measure → decide → defend.**
Commit evidence under the WP's `notes/`, write decisions as ADRs, and update `docs/readiness-scorecard.md`.
