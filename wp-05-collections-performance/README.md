# WP-05 — Collections & Java Performance Internals

_Effort: ~1.5 weeks   |   Depends on: WP-03 (contention), WP-01 (JIT warm-up)_

Use collections and functional constructs with awareness of algorithmic complexity, concurrency, allocation and cache/contention behavior.

## Core topics

- ArrayList vs LinkedList workload implications
- HashMap hashing, buckets, resize, treeification and collision behavior
- equals/hashCode contracts
- ConcurrentHashMap concurrency model
- Blocking and concurrent queues
- Immutable collections and safe sharing; sequenced collections
- Streams vs loops: readability and performance context
- Parallel streams and common-pool implications
- Allocation, boxing and memory-layout costs
- Big-O plus real workload/constant-factor reasoning
- JMH basics sufficient for these benchmarks (deep traps in WP-07)

## Hands-on labs

- [ ] Benchmark HashMap operations across sizing/collision scenarios with JMH.
- [ ] Compare synchronized map patterns with ConcurrentHashMap under contention.
- [ ] Measure allocation/boxing effects in the pricing index hot path.
- [ ] Compare sequential stream, parallel stream and explicit execution for controlled workloads.
- [ ] Document collection-selection rules for common service patterns.

## Interview defense

- [ ] How does HashMap work internally?
- [ ] Why is ConcurrentHashMap not just a synchronized HashMap?
- [ ] When is parallelStream a production risk?

## Definition of done

See section 12 of the lab document. Commit evidence (JFR files, dumps, benchmark JSON, screenshots) under `notes/`,
write the decision into `docs/adrs/`, and score yourself in `docs/readiness-scorecard.md`.
