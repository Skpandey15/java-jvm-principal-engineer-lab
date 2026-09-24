# ADR-000: Lab platform and pinned versions

- **Status:** Accepted
- **Date:** 2026-09-24

## Context

Lab evidence (GC logs, JFR recordings, benchmark numbers, latency percentiles) is only comparable if the
runtime, framework and infrastructure versions stay fixed. The lab document (v1.1, Section 3) defines the stack.

## Decision

| Area | Choice | Pinned in |
|---|---|---|
| JDK (baseline) | Temurin JDK 25 LTS | `libs.versions.toml` → `java = "25"` (Gradle toolchain) |
| JDK (comparison) | JDK 27 (non-LTS, GA 2026-09-15) | `java-comparison = "27"`; used only by `runOnComparisonJdk` |
| Build | Gradle 9.8.0 (wrapper), Groovy DSL, version catalog, convention plugins | `gradle/wrapper/gradle-wrapper.properties` |
| Framework | Spring Boot 4.1.1 (Spring Framework 7, Hibernate 7.4, JUnit 6) | `spring-boot = "4.1.1"` |
| Database | PostgreSQL 18.1 | `platform/docker-compose.yml` |
| Messaging | Apache Kafka 4.2.1 (KRaft) | compose (matches the Boot-managed Kafka client) |
| Cache | Valkey 9.0 | compose |
| Identity | Keycloak 26.7.4 | compose |
| Fault injection | Toxiproxy 2.12.0 | compose |
| Observability | grafana/otel-lgtm 0.33.1 | compose |
| Benchmarks | JMH 1.37 via me.champeau.jmh 0.7.3 | `libs.versions.toml` |
| Container image | Layered-jar Dockerfile on `eclipse-temurin:25-jdk-noble` (JRE optional), non-root | `platform/docker/spring-boot.Dockerfile` |
| CI/CD | GitHub Actions, actions pinned by commit SHA | `.github/workflows/` |

## Consequences

- Versions change only at phase boundaries (lab document, Section 14), recorded as a new ADR that supersedes this one.
- Rerun the key baselines (WP-02 GC comparison, WP-07 benchmark suite) after any upgrade so results stay comparable.
- JDK 27 is not used for the Spring services: Spring Boot 4.1 documents support up to Java 26.
- Buildpacks (`bootBuildImage`) were rejected as the default image path: on Docker daemons using the containerd image
  store (Docker 29 default) the image contains a zero-size layer that containerd refuses to pull, and the buildpack
  memory calculator sets -Xmx itself, which would hide the MaxRAMPercentage behavior WP-09 studies.
