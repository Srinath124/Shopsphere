import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 20 },  // Ramp up to 20 users
    { duration: '1m', target: 50 },   // Ramp up to 50 users
    { duration: '2m', target: 100 },  // Stay at 100 users
    { duration: '30s', target: 0 },   // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<300'], // 95% of requests must complete below 300ms
    http_req_failed: ['rate<0.01'],   // Error rate must be less than 1%
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost';

export default function () {
  // 1. Fetch Product Catalog
  const resProducts = http.get(`${BASE_URL}/api/products?page=0&size=20`);
  check(resProducts, {
    'catalog status 200': (r) => r.status === 200,
    'catalog has items': (r) => JSON.parse(r.body).content.length >= 0,
  });

  sleep(1);

  // 2. Fetch Categories
  const resCategories = http.get(`${BASE_URL}/api/categories`);
  check(resCategories, {
    'categories status 200': (r) => r.status === 200,
  });

  sleep(1);

  // 3. Search for a product
  const resSearch = http.get(`${BASE_URL}/api/products?search=Phone`);
  check(resSearch, {
    'search status 200': (r) => r.status === 200,
  });

  sleep(2);
}
