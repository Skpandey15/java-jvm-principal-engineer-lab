# ShopFlow healthy baseline — 2026-09-26

The reference numbers every incident is compared against.

**Setup:** order-service (ShopFlow) on JDK 25.0.4, `-Xmx512m`, Hikari pool 10, PostgreSQL 18.4 with the bulk seed
(10,010 customers, 5,050 products, 200,000 orders, 600,571 order lines), payment-stub at 50 ms latency.
Load: `load-tests/jmeter/shopflow.jmx`, JMeter 5.6.3 open model, 30 s ramp + 5 min:
browse 20/s, order history 5/s, place order 5/s (+ read back, 80% paid), daily report 0.2/s.

**Totals:** 20,216 requests in 5 m 30 s, 61.3 req/s, average 9 ms, max 273 ms, 1 error (0.005%).

| Request | Count | p50 ms | p95 ms | p99 ms | Max ms |
|---|---|---|---|---|---|
| GET /products/{id} | 6,300 | 2 | 7 | 9 | 29 |
| GET /orders/{id} | 1,575 | 2 | 7 | 9 | 16 |
| GET /customers/{id} | 1,575 | 3 | 7 | 10 | 23 |
| GET /products?category | 6,300 | 6 | 13 | 19 | 273 |
| GET /customers/{id}/orders | 1,575 | 6 | 16 | 25 | 99 |
| POST /orders | 1,575 | 12 | 29 | 46 | 199 |
| GET /reports/daily-sales | 63 | 15 | 26 | 48 | 48 |
| POST /orders/{id}/pay | 1,253 | 64 | 76 | 87 | 155 |

**Notes**
- The one error was client-side (`java.net.BindException: Address already in use` in JMeter): Windows ran out of
  ephemeral ports on the load generator. Not a server error; exclude it when judging the service.
- `pay` includes the payment-stub's configured 50 ms.
- A history request is 4 SQL statements (customer check, page of ids, count, orders+lines+products); seen as spans
  in Tempo. This is the number to compare in the N+1 incident.

## Lesson from producing this baseline

The first attempt failed with 92% timeouts. Cause: `./gradlew build` overwrote the running service's jar; classes
are loaded lazily, so the first *logged exception* under load needed `ThrowableProxy`, which could no longer be read
from the replaced jar (`NoClassDefFoundError`), killing request threads. Services now run from a copy
(`scripts/start-shopflow.cmd`, `build/run/*.jar`) — the local equivalent of immutable deployment artifacts.
