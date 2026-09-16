import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '1m', target: 30 },
    { duration: '1m', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<800'],
    http_req_failed: ['rate<0.05'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost';

function generateRandomString(len) {
  return Math.random().toString(36).substring(2, 2 + len);
}

export default function () {
  const user = `user_${generateRandomString(8)}`;
  const password = 'Password123!';
  const email = `${user}@example.com`;

  // 1. Register User
  const regPayload = JSON.stringify({
    username: user,
    email: email,
    password: password,
    firstName: 'Load',
    lastName: 'Tester',
  });

  const regRes = http.post(`${BASE_URL}/api/auth/register`, regPayload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(regRes, {
    'registered successfully': (r) => r.status === 200 || r.status === 201,
  });

  if (regRes.status !== 200 && regRes.status !== 201) {
    return;
  }

  const token = JSON.parse(regRes.body).token;
  const authHeaders = {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${token}`,
  };

  // 2. Add Item to Cart
  const cartPayload = JSON.stringify({
    productId: 'prod-001',
    productName: 'Stress Test Laptop',
    unitPrice: 999.99,
    quantity: 1,
  });

  const cartRes = http.post(`${BASE_URL}/api/cart/items`, cartPayload, {
    headers: authHeaders,
  });

  check(cartRes, {
    'item added to cart': (r) => r.status === 200,
  });

  sleep(1);

  // 3. Checkout with Idempotency Key
  const idempotencyKey = `idemp_${generateRandomString(16)}`;
  const orderPayload = JSON.stringify({
    shippingAddress: '100 Stress Test Blvd, Suite 400',
    items: [
      {
        productId: 'prod-001',
        quantity: 1,
      },
    ],
  });

  const orderRes = http.post(`${BASE_URL}/api/orders`, orderPayload, {
    headers: {
      ...authHeaders,
      'Idempotency-Key': idempotencyKey,
    },
  });

  check(orderRes, {
    'order created or accepted': (r) => r.status === 201 || r.status === 200,
  });

  sleep(2);
}
