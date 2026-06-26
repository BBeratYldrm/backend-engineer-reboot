# Day 70 — June 26, 2026

## What I studied

### Payment System Design — Deep Dive Continued

Completed the remaining deep dive topics for the payment system design session.

**Outbox Pattern — Relay mechanisms**
- Polling: scheduled job reads outbox table, relays to Kafka. Simple but adds DB load and latency.
- Debezium (CDC): reads PostgreSQL WAL directly, no DB load, low latency. Runs as Kafka Connect plugin. Requires `wal_level = logical` in PostgreSQL config. Connects to primary only. Tracks LSN position — resumes after Kafka downtime without data loss.
- Payment Service never publishes directly to Kafka — outbox guarantees consistency via same DB transaction.

**Observability — Three pillars**
- Logs: ELK Stack, structured JSON, Trace ID in every line
- Metrics: Prometheus + Grafana — DLQ count, PENDING count, PSP failure rate, latency
- Traces: Jaeger — Trace ID generated at API Gateway, propagated through all services and Kafka events
- DLQ alert response: Grafana → Kibana → Jaeger → fix → replay

**gRPC vs REST**
- External APIs: REST (universal, browser/mobile compatible)
- Internal service-to-service: gRPC (binary, faster, type-safe via proto, HTTP/2)
- PSP communication: REST (Stripe's API)
- Async events: Kafka

**Notes saved to:** `notes/system-design/SD-07-payment-system.md`

## Next

- Review SD-07 notes
- Pair programming practice — broken Spring Boot code format
- Payment system mock interview run-through