// Open-model load test for POST /orders (WP-07).
// constant-arrival-rate keeps sending requests at the target rate even when the service slows down,
// which avoids coordinated omission (a closed model would quietly send fewer requests).
//
//   load-tests/run-k6.sh                          (Docker; results go to Prometheus/Grafana)
//   RATE=200 DURATION=5m load-tests/run-k6.sh
//   k6 run load-tests/order-create.js             (native k6, results in the terminal only)
import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:18080';

export const options = {
  scenarios: {
    create_orders: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.RATE || 50),
      timeUnit: '1s',
      duration: __ENV.DURATION || '1m',
      preAllocatedVUs: 50,
      maxVUs: 500,
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(99)<500'],
  },
};

export default function () {
  // Seeded data: customers 1..10010, products 1..5050 (see db/seed/bulk-seed.sql)
  const lines = Array.from({ length: 1 + Math.floor(Math.random() * 3) }, () => ({
    productId: 1 + Math.floor(Math.random() * 5050),
    quantity: 1 + Math.floor(Math.random() * 3),
  }));
  const body = JSON.stringify({ customerId: 1 + Math.floor(Math.random() * 10010), lines });
  const res = http.post(`${BASE_URL}/orders`, body, { headers: { 'Content-Type': 'application/json' } });
  check(res, { 'status is 201': (r) => r.status === 201 });
}
