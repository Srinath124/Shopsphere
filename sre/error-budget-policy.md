# Error Budget Policy
## ShopSphere / Go N Buy Platform

### 1. Purpose
This policy governs engineering actions when the reliability error budget is consumed. It balances shipping velocity with system stability.

---

### 2. Error Budget Status Levels

| Budget Remaining | Status | Permitted Engineering Activities |
| :--- | :--- | :--- |
| **> 50%** | **Green** | Standard feature delivery, experimental deployments, regular CI/CD. |
| **20% – 50%** | **Yellow** | Canary deployments required. Elevated scrutiny on database migrations. |
| **0% – 20%** | **Orange** | Deployment freeze on non-critical features. Focus shifts to reliability improvements. |
| **< 0% (Exhausted)**| **Red** | **Full Feature Freeze**. 100% of engineering bandwidth diverted to bug fixes and resilience. |

---

### 3. Feature Freeze Exceptions
The only changes allowed during a Feature Freeze are:
1. P1/P2 incident hotfixes and patches.
2. Direct reliability enhancements (e.g. timeout tuning, circuit breaker improvements, query index optimization).
3. Critical security vulnerability remediations (CVE CVSS score >= 7.0).

---

### 4. Post-Mortem Requirements
Every P1 incident or error budget exhaustion event mandates a blameless post-mortem within 48 hours covering:
- Exact timeline of events (detection, triage, mitigation, resolution).
- Root Cause Analysis (5 Whys methodology).
- Corrective Action Items with assigned owners and Jira ticket links.
