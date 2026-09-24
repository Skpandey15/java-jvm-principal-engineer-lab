# WP-06 — JDBC, JPA, Hibernate & Transactions

_Effort: ~3 weeks   |   Depends on: WP-04 (concurrency limits); Spring Boot usage prerequisite_

Understand the complete data-access path from Spring Data JPA through Hibernate and JDBC to the connection pool and PostgreSQL, including transaction and locking consequences. This WP owns transaction semantics (what the database sees); WP-08 owns the proxy mechanism that applies @Transactional.

## Core topics

- JDBC connection/statement/result-set lifecycle
- Prepared statements and batching (JDBC and Hibernate batching)
- Connection pooling and HikariCP: sizing, timeouts, leak detection
- JPA specification vs Hibernate implementation
- Entity lifecycle and persistence context; dirty checking and flushing
- First-level cache; second-level cache awareness
- Lazy vs eager loading; N+1 query problem and fetch strategies (join fetch, entity graphs, batch fetching)
- JPQL/native SQL; JdbcClient for read models; JPA vs JdbcClient vs jOOQ decision criteria
- Spring Data JPA repository abstraction
- ACID and transaction boundaries; @Transactional propagation and rollback rules as observed behavior
- Isolation levels and anomalies in PostgreSQL (MVCC, READ COMMITTED vs REPEATABLE READ vs SERIALIZABLE)
- Optimistic vs pessimistic locking; SELECT … FOR UPDATE / SKIP LOCKED
- Long-running transactions and connection retention
- PostgreSQL evidence: EXPLAIN ANALYZE, pg_stat_statements, pg_locks, indexing
- Schema migration with Flyway

## Hands-on labs

- [ ] Implement the same use case with plain JDBC, JdbcClient and JPA and compare SQL, latency and code.
- [ ] Create N+1 intentionally; detect it from SQL logs / Hibernate statistics / metrics and fix it; assert query counts in a test.
- [ ] Exhaust HikariCP connections and diagnose request latency.
- [ ] Create a connection leak; find it with Hikari leak detection.
- [ ] Flood the pool from virtual threads (WP-04 revisited) and cap concurrency to pool capacity.
- [ ] Create concurrent stock updates; compare optimistic and pessimistic locking.
- [ ] Demonstrate isolation anomalies in a controlled PostgreSQL lab.
- [ ] Create a slow-query + long-transaction scenario and trace its effect to connection-pool saturation.

## Interview defense

- [ ] JPA vs Hibernate vs JDBC? When would you drop to JdbcClient or jOOQ?
- [ ] Why does N+1 happen and how do you prove it?
- [ ] How should DB pool size relate to request concurrency?
- [ ] Optimistic vs pessimistic locking?
- [ ] Why might @Transactional appear not to work? (mechanism in WP-08)

## Definition of done

See section 12 of the lab document. Commit evidence (JFR files, dumps, benchmark JSON, screenshots) under `notes/`,
write the decision into `docs/adrs/`, and score yourself in `docs/readiness-scorecard.md`.
