# WP-08 — Spring Boot Production Engineering

_Effort: ~3 weeks   |   Depends on: WP-04, WP-06_

Understand the framework mechanisms that affect correctness, latency, transactions, messaging, caching, security and resilience in Spring Boot 4.1.

## Core topics

- IoC/DI and bean lifecycle; Spring Boot 4 modular auto-configuration and conditions
- AOP proxies (JDK dynamic vs CGLIB), advice ordering and proxy limitations
- Transaction interception: how @Transactional is applied, self-invocation, rollback rules, interaction with other advice
- Web request/thread model; virtual threads in Spring Boot (spring.threads.virtual.enabled)
- HTTP clients: RestClient and HTTP service interfaces; connect/read timeouts
- Validation, exception handling (ProblemDetail) and API versioning
- Timeout, retry, circuit breaker and bulkhead patterns; Spring Framework 7 @Retryable/@ConcurrencyLimit vs Resilience4j
- Idempotency: idempotency keys, retry-safe operations, idempotent consumers
- Messaging with Spring for Apache Kafka: delivery semantics, retries and dead-letter topics, transactional outbox
- Caching: Spring Cache abstraction, Caffeine vs Redis/Valkey, TTL and invalidation, stampede protection, behavior when the cache is down
- Spring Security 7 filter chain; OAuth2/OIDC resource server with JWT (Keycloak)
- Configuration/profiles/secrets boundaries
- Graceful shutdown and readiness
- Testing pyramid: unit, slice, integration with Testcontainers; ArchUnit rules

## Hands-on labs

- [ ] Inspect bean/proxy behavior and demonstrate an AOP boundary surprise, including @Transactional self-invocation.
- [ ] Implement rollback/propagation experiments at the proxy level (checked vs unchecked exceptions, REQUIRES_NEW).
- [ ] Introduce a slow payment stub with Toxiproxy and test timeout/retry/circuit-breaker behavior.
- [ ] Demonstrate retry amplification and correct it with budgets, jitter and idempotency keys.
- [ ] Implement a transactional outbox and an idempotent Kafka consumer; prove no duplicates under redelivery.
- [ ] Add a Redis cache; measure hit ratio, reproduce a cache stampede and fix it (jittered TTL, request coalescing).
- [ ] Secure the API with Keycloak-issued JWTs and verify authorization rules in tests.
- [ ] Build production-readiness checks and graceful shutdown behavior.

## Interview defense

- [ ] How does @Transactional work internally?
- [ ] Why can retries make an outage worse?
- [ ] Where should timeout values come from?
- [ ] How do you guarantee an event is published if and only if the DB commit succeeds?
- [ ] How do Spring proxies affect design?
- [ ] What is your caching strategy and what happens when the cache is unavailable?

## Definition of done

See section 12 of the lab document. Commit evidence (JFR files, dumps, benchmark JSON, screenshots) under `notes/`,
write the decision into `docs/adrs/`, and score yourself in `docs/readiness-scorecard.md`.
