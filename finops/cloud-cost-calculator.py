#!/usr/bin/env python3
"""
ShopSphere / Go N Buy Cloud Cost Calculator
Computes estimated monthly AWS infrastructure costs for Dev, Staging, and Production environments.
"""

import sys

COST_TABLE = {
    "ec2_hourly": {
        "t3.medium": 0.0416,
        "m6i.large": 0.096,
        "m6g.large": 0.077,
    },
    "rds_hourly": {
        "db.t4g.small": 0.032,
        "db.r6g.large": 0.24,
    },
    "nat_hourly": 0.045,
    "eks_cluster_hourly": 0.10,
    "storage_gb_monthly": 0.08, # gp3
}

def calculate_monthly_cost(env: str) -> dict:
    hours_in_month = 730

    if env == "dev":
        node_count = 2
        node_type = "t3.medium"
        rds_type = "db.t4g.small"
        rds_storage_gb = 20
        nat_count = 1
        # Dev scaled down 65% of the time
        active_hours = hours_in_month * 0.35
    elif env == "prod":
        node_count = 6
        node_type = "m6g.large"
        rds_type = "db.r6g.large"
        rds_storage_gb = 200
        nat_count = 3
        active_hours = hours_in_month
    else:
        raise ValueError(f"Unknown environment: {env}")

    eks_cost = COST_TABLE["eks_cluster_hourly"] * hours_in_month
    compute_cost = node_count * COST_TABLE["ec2_hourly"][node_type] * active_hours
    rds_compute = COST_TABLE["rds_hourly"][rds_type] * hours_in_month
    rds_storage = rds_storage_gb * COST_TABLE["storage_gb_monthly"]
    nat_cost = nat_count * COST_TABLE["nat_hourly"] * hours_in_month

    total = eks_cost + compute_cost + rds_compute + rds_storage + nat_cost

    return {
        "environment": env,
        "eks_control_plane": round(eks_cost, 2),
        "ec2_worker_nodes": round(compute_cost, 2),
        "rds_postgresql": round(rds_compute + rds_storage, 2),
        "nat_gateways": round(nat_cost, 2),
        "total_estimated_monthly_usd": round(total, 2)
    }

def main():
    print("=========================================================")
    print(" ShopSphere FinOps Monthly Cost Estimates (AWS us-east-1)")
    print("=========================================================")
    for env in ["dev", "prod"]:
        data = calculate_monthly_cost(env)
        print(f"\n--- Environment: {data['environment'].upper()} ---")
        for k, v in data.items():
            if k != "environment":
                print(f"  {k:30}: ${v:,.2f}")

if __name__ == "__main__":
    main()
