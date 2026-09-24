# WP-07 — JVM Performance & Production Diagnostics

_Effort: ~2.5 weeks   |   Depends on: WP-01 to WP-06_

Develop a repeatable evidence-first workflow for CPU, memory, thread, GC and latency incidents.

## Core topics

- Performance methodology: baseline, hypothesis, measurement (USE and RED methods)
- JIT and warm-up implications for benchmarks; inlining and escape analysis
- JMH fundamentals and benchmark traps (dead-code elimination, constant folding, warm-up)
- Java Flight Recorder and JDK Mission Control; JDK 25 additions: CPU-time profiling (JEP 509, experimental, Linux), cooperative sampling (JEP 518), method timing and tracing (JEP 520)
- async-profiler: CPU, allocation, lock and wall-clock flame graphs
- jcmd, jstack, jmap and process inspection
- Thread dumps and state interpretation
- Heap dumps and dominator/retained-memory reasoning
- Allocation profiling
- GC log correlation
- p50/p95/p99 latency interpretation and histograms
- Coordinated omission in load testing; open-model load with k6 constant-arrival-rate

## Hands-on labs

- [ ] Create trustworthy JMH microbenchmarks and demonstrate a misleading benchmark.
- [ ] Capture JFR and async-profiler flame graphs during CPU pressure and identify hot execution paths.
- [ ] Diagnose blocked/waiting/runnable thread patterns from dumps.
- [ ] Correlate allocation pressure, GC logs and p99 latency.
- [ ] Show the difference between closed-model and open-model load results for the same service.
- [ ] Produce a Principal-style incident report: evidence, root cause, remediation and prevention.

## Interview defense

- [ ] CPU is 95%: what do you inspect first?
- [ ] Heap is stable but latency is high—what next?
- [ ] Why are microbenchmarks difficult?
- [ ] How do you distinguish JVM from downstream latency?

## Definition of done

See section 12 of the lab document. Commit evidence (JFR files, dumps, benchmark JSON, screenshots) under `notes/`,
write the decision into `docs/adrs/`, and score yourself in `docs/readiness-scorecard.md`.
