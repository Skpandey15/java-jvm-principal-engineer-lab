# WP-04 — Threads, Executors, Virtual Threads & Consumers

_Effort: ~2.5 weeks   |   Depends on: WP-03_

Design execution models, including message consumers, based on workload characteristics, resource constraints and failure behavior.

## Core topics

- Platform threads and OS scheduling relationship
- ExecutorService and ThreadPoolExecutor: core/max pool size, queues and rejection policies
- CPU-bound vs I/O-bound sizing (Little's Law)
- CompletableFuture and executor control
- ForkJoinPool/common-pool risks
- Blocking and thread starvation
- Virtual threads: scheduler, carrier threads, when they help; JDK 24+ no longer pins on synchronized (JEP 491); remaining pinning cases (native frames) and the jdk.VirtualThreadPinned JFR event
- Structured concurrency (preview in JDK 25) and scoped values
- Virtual threads vs reactive (WebFlux) as a trade-off: programming model, backpressure, debuggability, ecosystem
- Kafka consumer execution model: poll loop, max.poll.records / max.poll.interval.ms, partitions as the unit of parallelism, consumer lag, rebalances (KIP-848 protocol)
- Backpressure, concurrency limits and bounded-resource design

## Hands-on labs

- [ ] Cause thread-pool saturation and observe queue growth/latency.
- [ ] Compare bounded vs unbounded queues and rejection policies.
- [ ] Build CompletableFuture pipelines with controlled executors and failure propagation.
- [ ] Compare platform and virtual threads for blocking I/O workloads; observe carriers with JFR.
- [ ] Show that more concurrency moves the bottleneck to a bounded downstream: call the payment stub (limited to N concurrent requests) from thousands of virtual threads, then protect it with a semaphore / @ConcurrencyLimit. The same effect on HikariCP is revisited in WP-06.
- [ ] Slow down the Kafka consumer to create lag and a rebalance storm; fix with correct poll settings and bounded parallel processing.

## Interview defense

- [ ] How do you size a thread pool?
- [ ] Why can an unbounded executor queue be dangerous?
- [ ] When do virtual threads help and when do they not?
- [ ] Virtual threads or reactive for a new service, and why?
- [ ] Consumer lag is rising: scale consumers, partitions or fix processing?
- [ ] How do you prevent overload from becoming cascading failure?

## Definition of done

See section 12 of the lab document. Commit evidence (JFR files, dumps, benchmark JSON, screenshots) under `notes/`,
write the decision into `docs/adrs/`, and score yourself in `docs/readiness-scorecard.md`.
