/**
 * k6 Autoscaling Demonstration Script
 *
 * PURPOSE: Demonstrate HPA (Horizontal Pod Autoscaler) in action by
 * generating ramping load that triggers CPU-based autoscaling of the
 * inventory-service and order-service.
 *
 * HOW TO RUN (local Docker Compose):
 *   k6 run tests/load/k6-autoscaling-demo.js \
 *     -e BASE_URL=http://localhost \
 *     -e TOKEN=<your-jwt-token>
 *
 * HOW TO RUN (Kind / Kubernetes):
 *   k6 run tests/load/k6-autoscaling-demo.js \
 *     -e BASE_URL=http://shopsphere.local \
 *     -e TOKEN=<your-jwt-token>
 *
 * WHAT TO OBSERVE:
 *   # In a separate terminal, watch HPA scaling events:
 *   watch -n5 'kubectl get hpa -n shopsphere-dev && kubectl top pods -n shopsphere-dev'
 *
 * PHASES:
 *   0:00–2:00  Warm-up  (10 VUs)   → Baseline, no scaling expected
 *   2:00–6:00  Ramp-up  (10→100 VUs) → CPU rises, HPA triggers scale-out
 *   6:00–10:00 Sustain  (100 VUs)  → Observe steady-state with more pods
 *   10:00–12:00 Ramp-down (100→10 VUs) → Load drops, HPA scale-in after cooldown
 *   12:00–15:00 Cool-down (10 VUs) → Observe HPA scale-in (takes ~5 min)
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// ── Custom metrics ────────────────────────────────────────────────────────────
const errorRate        = new Rate('error_rate');
const productListDuration = new Trend('product_list_duration_ms');
const inventoryCheckDuration = new Trend('inventory_check_duration_ms');
const successfulOrders = new Counter('successful_orders');

// ── Configuration ─────────────────────────────────────────────────────────────
const BASE_URL = __ENV.BASE_URL || 'http://localhost';
const TOKEN    = __ENV.TOKEN    || '';

const HEADERS = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${TOKEN}`,
};

// ── Load profile: ramp up to demonstrate HPA ──────────────────────────────────
export const options = {
    stages: [
        { duration: '2m',  target: 10  },  // Warm-up
        { duration: '4m',  target: 100 },  // Ramp-up → trigger autoscaling
        { duration: '4m',  target: 100 },  // Sustain at peak
        { duration: '2m',  target: 10  },  // Ramp-down
        { duration: '3m',  target: 10  },  // Cool-down (observe scale-in)
    ],
    thresholds: {
        'http_req_failed':            ['rate<0.05'],     // <5% error rate at peak
        'http_req_duration':          ['p(95)<2000'],    // p95 < 2s
        'product_list_duration_ms':   ['p(95)<1000'],    // Product list < 1s
        'inventory_check_duration_ms':['p(95)<1500'],    // Inventory check < 1.5s
        'error_rate':                 ['rate<0.05'],
    },
};

// ── Scenario helper: browse products ─────────────────────────────────────────
function browseProducts() {
    const start = Date.now();
    const res = http.get(`${BASE_URL}/api/products?page=0&size=20`, { headers: HEADERS });
    productListDuration.add(Date.now() - start);

    const ok = check(res, {
        'product list status 200': (r) => r.status === 200,
        'product list has items':  (r) => {
            try {
                return JSON.parse(r.body).content?.length > 0;
            } catch { return false; }
        },
    });
    errorRate.add(!ok);
}

// ── Scenario helper: check inventory ─────────────────────────────────────────
function checkInventory() {
    const products = ['PROD-001', 'PROD-002', 'PROD-003'];
    const productId = products[Math.floor(Math.random() * products.length)];

    const start = Date.now();
    const res = http.get(`${BASE_URL}/api/inventory/${productId}`, { headers: HEADERS });
    inventoryCheckDuration.add(Date.now() - start);

    const ok = check(res, {
        'inventory check 200 or 404': (r) => r.status === 200 || r.status === 404,
    });
    errorRate.add(!ok);
}

// ── Scenario helper: view cart ────────────────────────────────────────────────
function viewCart() {
    if (!TOKEN) return;
    const res = http.get(`${BASE_URL}/api/cart`, { headers: HEADERS });
    check(res, {
        'cart accessible': (r) => r.status === 200 || r.status === 401,
    });
}

// ── Main VU function ──────────────────────────────────────────────────────────
export default function () {
    // Distribute load across multiple endpoints to stress different services
    const rand = Math.random();

    if (rand < 0.50) {
        // 50% — Product browsing (stresses product-service)
        browseProducts();
        sleep(0.5);

    } else if (rand < 0.80) {
        // 30% — Inventory checks (stresses inventory-service → triggers HPA)
        checkInventory();
        sleep(0.3);

    } else {
        // 20% — Cart viewing (stresses cart-service via Redis)
        viewCart();
        sleep(0.5);
    }

    sleep(0.2);
}

// ── Summary ───────────────────────────────────────────────────────────────────
export function handleSummary(data) {
    const peakVUs    = 100;
    const errorPct   = (data.metrics.error_rate?.values?.rate * 100 || 0).toFixed(2);
    const p95Latency = (data.metrics.http_req_duration?.values?.['p(95)'] || 0).toFixed(0);

    console.log('\n═══════════════════════════════════════════════════════════════');
    console.log('  ShopSphere Autoscaling Demonstration — Summary');
    console.log('═══════════════════════════════════════════════════════════════');
    console.log(`  Peak VUs:           ${peakVUs}`);
    console.log(`  Error rate:         ${errorPct}%`);
    console.log(`  p95 latency:        ${p95Latency}ms`);
    console.log(`  Successful requests:${data.metrics.http_reqs?.values?.count || 0}`);
    console.log('');
    console.log('  ► To verify HPA scaled up during test:');
    console.log('    kubectl describe hpa -n shopsphere-dev');
    console.log('');
    console.log('  ► To see scale events:');
    console.log('    kubectl get events -n shopsphere-dev --field-selector reason=SuccessfulRescale');
    console.log('═══════════════════════════════════════════════════════════════\n');

    return {
        stdout: `Autoscaling demo complete. Error rate: ${errorPct}%, p95: ${p95Latency}ms\n`,
    };
}
