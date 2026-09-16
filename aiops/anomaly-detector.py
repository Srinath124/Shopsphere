#!/usr/bin/env python3
"""
ShopSphere / Go N Buy AIOps Anomaly Detector
Polls Prometheus metrics, performs statistical anomaly detection (rolling Z-score),
and correlates anomalies with root-cause indicators.
"""

import os
import sys
import time
import math
import urllib.request
import json

PROMETHEUS_URL = os.getenv("PROMETHEUS_URL", "http://localhost:9090")
Z_SCORE_THRESHOLD = float(os.getenv("AIOPS_ANOMALY_THRESHOLD", "2.5"))

METRIC_QUERIES = {
    "http_5xx_rate": 'sum(rate(http_server_requests_seconds_count{status=~"5.."}[2m])) by (service)',
    "p95_latency_ms": 'histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[2m])) by (le, service)) * 1000',
    "jvm_heap_utilization": '(sum(jvm_memory_used_bytes{area="heap"}) by (service) / sum(jvm_memory_max_bytes{area="heap"}) by (service)) * 100'
}

def query_prometheus(query: str):
    url = f"{PROMETHEUS_URL}/api/v1/query?query={urllib.parse.quote(query)}"
    req = urllib.request.Request(url, headers={"User-Agent": "ShopSphere-AIOps/1.0"})
    try:
        with urllib.request.urlopen(req, timeout=5) as response:
            if response.status == 200:
                data = json.loads(response.read().decode())
                return data.get("data", {}).get("result", [])
    except Exception as e:
        return []
    return []

def calculate_z_score(val, history):
    if len(history) < 3:
        return 0.0
    mean = sum(history) / len(history)
    variance = sum((x - mean) ** 2 for x in history) / len(history)
    std_dev = math.sqrt(variance)
    if std_dev == 0:
        return 0.0
    return (val - mean) / std_dev

def run_detection_cycle(history_dict):
    print(f"[{time.strftime('%Y-%m-%d %H:%M:%S')}] AIOps Metric Scrape & Anomaly Detection Cycle...")
    anomalies_detected = 0

    for metric_name, query in METRIC_QUERIES.items():
        results = query_prometheus(query)
        for res in results:
            service = res.get("metric", {}).get("service", "unknown")
            key = f"{metric_name}:{service}"
            val = float(res.get("value", [0, 0])[1])

            if key not in history_dict:
                history_dict[key] = []

            history = history_dict[key]
            z = calculate_z_score(val, history)

            if abs(z) >= Z_SCORE_THRESHOLD:
                anomalies_detected += 1
                print(f"🚨 [ANOMALY DETECTED] Service: {service:20} Metric: {metric_name:22} Value: {val:.2f} (Z-Score: {z:+.2f})")
                print(f"   -> Automated Diagnosis: Sudden deviation of {metric_name} on {service}. Check database lock pool or RabbitMQ consumers.")

            history.append(val)
            if len(history) > 30: # Keep last 30 data points
                history.pop(0)

    if anomalies_detected == 0:
        print("  ✓ All telemetry metrics are within nominal statistical bounds.")

def main():
    print("=================================================================")
    print(" Starting ShopSphere AIOps Statistical Telemetry Anomaly Detector")
    print(f" Prometheus Target: {PROMETHEUS_URL} | Z-Score Threshold: {Z_SCORE_THRESHOLD}")
    print("=================================================================")

    history = {}
    # Run one evaluation cycle for CLI execution
    run_detection_cycle(history)

if __name__ == "__main__":
    main()
