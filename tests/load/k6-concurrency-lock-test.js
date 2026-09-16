import http from 'k6/http';
import { check } from 'k6';

// Flash sale simulation: 50 concurrent requests hitting the reservation endpoint simultaneously
export const options = {
  scenarios: {
    flash_sale_spike: {
      executor: 'shared-iterations',
      vus: 50,
      iterations: 50,
      maxDuration: '30s',
    },
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost';

export default function () {
  const payload = JSON.stringify({
    items: [
      {
        productId: 'prod-flash-sale',
        quantity: 1,
      },
    ],
  });

  const res = http.post(`${BASE_URL}/api/inventory/reserve`, payload, {
    headers: { 'Content-Type': 'application/json' },
  });

  // Either 200 OK (reservation succeeded) or 409 Conflict / 400 Bad Request (out of stock)
  // Under NO circumstances should the server crash (500) or deadlock
  check(res, {
    'valid business response (200, 400, or 409)': (r) =>
      r.status === 200 || r.status === 400 || r.status === 409,
    'no internal server error (500)': (r) => r.status !== 500,
  });
}
