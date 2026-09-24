# WP-10 — Principal Architecture, Failure & Interview Lab

_Effort: ~3 weeks   |   Depends on: All previous WPs_

Integrate all previous WPs into ambiguous, multi-layer production scenarios and defend decisions at Principal Engineer depth.

## Core topics

- Requirements and NFR discovery
- Capacity/performance reasoning and cost per request (FinOps)
- Architecture trade-offs and ADRs
- Failure domains and blast radius
- Bulkheads, backpressure and graceful degradation
- Data-access, transaction and polyglot-persistence trade-offs
- JVM/runtime implications of architecture
- Observability and SLO design
- Incident leadership and RCA
- Migration/rollout/rollback strategy (including Java/Spring upgrades)
- Cross-team technical governance, design reviews and mentoring
- Interview communication: assumptions, alternatives, evidence and decision

## Hands-on labs

- [ ] Capstone: p99 rises from 200 ms to 3 s while DB pool reaches 100%; diagnose across JVM, threads, ORM and DB.
- [ ] Capstone: CPU 95%, GC normal, Kafka consumer lag rising; build a diagnostic tree and evidence plan.
- [ ] Capstone: memory grows until pod restart; determine heap leak vs native/container pressure.
- [ ] Capstone: a retry storm from a slow payment dependency cascades; contain it and write the RCA.
- [ ] Design a high-throughput Java service and justify threading, DB access, caching, messaging, resilience and observability choices.
- [ ] Polyglot ADR (optional MongoDB): PostgreSQL JSONB vs MongoDB for a catalog read model, with measured evidence.
- [ ] Produce an ADR pack, a production-readiness review and a cost/capacity model.
- [ ] Run timed Principal Engineer interview-defense sessions using CV-style claims and follow-up drilling; score with the rubric in Section 13.

## Interview defense

- [ ] What do you check first and why?
- [ ] What evidence would falsify your hypothesis?
- [ ] What trade-off did you accept?
- [ ] How does this decision change at 10x scale?
- [ ] How do you roll it out safely across multiple teams?

## Definition of done

See section 12 of the lab document. Commit evidence (JFR files, dumps, benchmark JSON, screenshots) under `notes/`,
write the decision into `docs/adrs/`, and score yourself in `docs/readiness-scorecard.md`.
