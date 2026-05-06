# Day 24 — May 6, 2026

## What I learned

### Phase 7 — Observability (completed)
- Three pillars — logs, metrics, traces
- Prometheus pull model, Grafana dashboards and alerting
- RED method — Rate, Errors, Duration
- USE method — Utilization, Saturation, Errors
- Distributed tracing — Trace ID, spans, Jaeger
- OpenTelemetry — vendor-neutral standard
- p95 vs p99 — why p99 matters more

### Phase 8 — System Design (started)

URL Shortener:
- Requirements clarification — scale, analytics, custom URLs
- Capacity estimation — 12 writes/s, 1160 reads/s, 1.8TB in 10 years
- Base62 encoding — 62^6 = 56 billion combinations
- 301 vs 302 — analytics requires 302
- CQRS — read/write separation, 100:1 ratio
- Redis cache-aside — key: short code, value: original URL
- Hash-based sharding — hot spot problem solved
- Sequential ID security — hash before Base62 encoding
- Custom URL support — namespace separation
- URL expiration — expires_at + Redis TTL + scheduled job
- Snowflake ID — distributed unique ID for sharded systems
- 410 Gone vs 404 Not Found

Rate Limiter:
- Two-layer architecture — API Gateway + Redis
- Three algorithms — Fixed Window, Sliding Window, Token Bucket
- Token Bucket for LINE — burst-friendly, natural usage pattern
- Redis INCR + Lua script atomicity
- 429 + Retry-After header
- Distributed rate limiter — centralized Redis
- Fail-open vs fail-closed — Redis failure strategy
- Redis Sentinel / Cluster — high availability

## How I feel
Strong day. System Design clicked in a way it hadn't before.
CQRS connection to URL Shortener was a great moment.
Snowflake ID was a new concept — landed well.
Starting to think proactively about failure scenarios instead of just answering questions.
That shift feels important.

## Next
- More System Design scenarios — Chat System, Notification System
- Mock interview preparation
- Algorithm practice