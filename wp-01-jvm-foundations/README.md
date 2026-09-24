# WP-01 — Java & JVM Foundations

_Effort: ~2 weeks   |   Depends on: Prerequisites (Section 2)_

Establish a precise mental model of Java execution from source code to bytecode, class loading, runtime memory areas, interpretation/JIT compilation and native execution, and set up the tools and platform every later WP relies on.

## Core topics

- JDK vs JVM and platform dependence
- Class-file structure and bytecode basics
- Class-loader hierarchy and delegation; bootstrap, platform and application class loaders
- Runtime data areas: heap, Java stacks, PC register, native stacks, metaspace, code cache
- Object creation and initialization
- JIT compilation, tiered compilation and warm-up; AOT cache for startup (JEP 483/514/515) awareness
- Java 25 language and platform features used in this lab: records, sealed classes, pattern matching for switch and record patterns, unnamed variables, flexible constructor bodies (JEP 513), module import declarations (JEP 511), compact source files (JEP 512), scoped values (JEP 506, final); structured concurrency is still a preview feature (JEP 505)
- Diagnostics toolkit primer: jcmd (VM.flags, Thread.print, GC.heap_info, GC.heap_dump, JFR.start), opening a JFR recording in JDK Mission Control. WP-07 goes deep; this primer lets WP-02 to WP-06 collect evidence.

## Hands-on labs

- [ ] Lab 0 — Platform bootstrap: Gradle multi-project skeleton (Groovy DSL, version catalog, Java toolchain 25, convention plugins), Temurin 25, docker compose platform, reference-service skeleton running; record versions in ADR-000.
- [ ] Compile/disassemble classes with javap and map source constructs (including records, switch patterns) to bytecode.
- [ ] Create a custom class-loading experiment and demonstrate class identity/class-loader boundaries.
- [ ] Observe JVM startup/warm-up; compare cold vs warmed execution and the effect of an AOT cache.
- [ ] Diagnostics primer: attach jcmd to the running service, take a thread dump, heap histogram and a 60-second JFR recording.
- [ ] Create a Java-version decision record: runtime support, ecosystem compatibility, performance and operational risk (JDK 21 vs 25 vs 27).

## Interview defense

- [ ] What exactly makes Java platform independent if the JVM is platform specific?
- [ ] Walk from .java source to native execution.
- [ ] When can class loading cause production memory or linkage problems?
- [ ] How would you roll a Java 21 → 25 upgrade across 40 services?

## Definition of done

See section 12 of the lab document. Commit evidence (JFR files, dumps, benchmark JSON, screenshots) under `notes/`,
write the decision into `docs/adrs/`, and score yourself in `docs/readiness-scorecard.md`.
