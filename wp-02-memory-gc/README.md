# WP-02 — Memory Management & Garbage Collection

_Effort: ~2 weeks   |   Depends on: WP-01 (diagnostics primer)_

Understand memory allocation and GC well enough to diagnose latency, throughput and memory-pressure incidents, and account for the full JVM process footprint, not just the heap.

## Core topics

- Heap generations/regions and object allocation; TLABs and allocation pressure
- Stack frames and references; reachability and GC roots
- Object layout and compact object headers (JEP 519, -XX:+UseCompactObjectHeaders)
- Metaspace and class metadata; class-loader leaks
- Native memory: Native Memory Tracking (NMT), direct ByteBuffers, thread stacks, code cache, GC structures
- Full process footprint = heap + metaspace + code cache + thread stacks + direct/native memory
- G1 architecture and pause goals
- Generational ZGC (the only ZGC mode since JDK 24) and low-latency trade-offs; generational Shenandoah awareness
- GC logs (-Xlog:gc*) and pause analysis
- OutOfMemoryError variants: Java heap space, Metaspace, Direct buffer memory, unable to create native thread
- Heap sizing and latency/throughput/footprint trade-offs

## Hands-on labs

- [ ] Generate allocation pressure through the reference service and inspect GC behavior.
- [ ] Compare G1 and generational ZGC for a controlled workload (throughput, p99, CPU, footprint).
- [ ] Measure heap reduction from compact object headers on an object-heavy workload; optionally rerun on JDK 27.
- [ ] Create a retained-object memory leak; capture and analyze a heap dump in Eclipse MAT.
- [ ] Create a direct-buffer (native) leak; use NMT summary/diff to show RSS growth while the heap stays flat.
- [ ] Trigger representative OOM conditions safely and document symptoms/evidence.
- [ ] Define a memory/GC ADR for a latency-sensitive service.

## Interview defense

- [ ] Why can a Java application leak memory despite GC?
- [ ] How would you investigate increasing p99 together with increasing GC pauses?
- [ ] When would you consider ZGC rather than G1?
- [ ] RSS keeps growing but the heap is flat: what are the suspects and how do you prove which one it is?

## Definition of done

See section 12 of the lab document. Commit evidence (JFR files, dumps, benchmark JSON, screenshots) under `notes/`,
write the decision into `docs/adrs/`, and score yourself in `docs/readiness-scorecard.md`.
