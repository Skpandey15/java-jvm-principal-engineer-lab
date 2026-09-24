// Open-model load test for POST /orders (WP-07).
// constant-arrival-rate keeps sending requests at the target rate even when the service slows down,
// which avoids coordinated omission (a closed model would quietly send fewer requests).
//
//   k6 run load-tests/order-create.js
//   k6 run -e RATE=200 -e DURATION=5m load-tests/order-create.js
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
  const body = JSON.stringify({
    customerId: `customer-${Math.floor(Math.random() * 1000)}`,
    totalAmount: (Math.random() * 500 + 1).toFixed(2),
  });
  const res = http.post(`${BASE_URL}/orders`, body, { headers: { 'Content-Type': 'application/json' } });
  check(res, { 'status is 201': (r) => r.status === 201 });
}
