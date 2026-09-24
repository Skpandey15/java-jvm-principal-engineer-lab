# WP-03 — Java Memory Model & Concurrency

_Effort: ~2 weeks   |   Depends on: WP-01 (thread dumps from the primer)_

Move from API-level concurrency knowledge to correctness reasoning using the Java Memory Model.

## Core topics

- Atomicity, visibility and ordering
- Happens-before relationships
- volatile semantics and limitations
- synchronized monitor semantics
- ReentrantLock, ReadWriteLock, StampedLock and lock trade-offs
- Atomic classes, CAS and LongAdder
- Race conditions and lost updates
- Deadlock, livelock and starvation
- Safe publication and immutability
- Concurrent state design (confinement, immutability, ownership)
- Correctness testing with jcstress awareness

## Hands-on labs

- [ ] Build a reproducible race condition in the inventory reservation component and fix it with alternative synchronization strategies.
- [ ] Demonstrate a visibility problem and reason about volatile/happens-before.
- [ ] Create and diagnose a deadlock using thread dumps.
- [ ] Compare lock-based, atomic/CAS and LongAdder implementations under contention (JMH).
- [ ] Write an ADR for shared-state strategy in a high-throughput component.

## Interview defense

- [ ] volatile vs synchronized?
- [ ] What does happens-before guarantee?
- [ ] How do you diagnose a deadlock in production?
- [ ] When can CAS perform worse than locking?

## Definition of done

See section 12 of the lab document. Commit evidence (JFR files, dumps, benchmark JSON, screenshots) under `notes/`,
write the decision into `docs/adrs/`, and score yourself in `docs/readiness-scorecard.md`.
