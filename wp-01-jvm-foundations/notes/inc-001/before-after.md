# INC-001 — before/after evidence

Same conditions for both runs: `order-service` on JDK 25.0.4, `-Xmx256m`, `--lab.incident=inc-001`, k6 open-model
load at 30 req/s. Window: first 10 minutes at 30 req/s (before: 2026-09-25 22:53–23:03, after: 23:56–00:06).
Source: Prometheus, queried by `max_over_time(heap)`, `histogram_quantile(0.99, ...)` and `rate(jvm_gc_pause_seconds_sum)`.

| Metric | Before (leak) first → last (max) | After (fix) first → last (max) |
|---|---|---|
| Heap used, MB | 247.6 → 266.7 (266.7) | 62.5 → 37.5 (64.9) |
| p99 `POST /orders`, ms | 9.2 → 128.0 (128.0) | 13.6 → 7.9 (13.6) |
| GC pause, ms per second | 0.7 → 105.4 (105.4) | 0.2 → 0.1 (0.3) |
| Outcome | `OutOfMemoryError: Java heap space`, auto heap dump, JVM kept holding port 18080 | UP after 10 minutes |

Fix: removed `ReceiptCache` (unbounded, never read) and its call in `OrderService.create`.
The failing version is commit `579235d`; the fix is the commit that follows it.
