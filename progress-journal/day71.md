# Day 71 — June 27, 2026

## What I studied

### Payment System Design (SD-07) — Review

Reviewed full payment system flow. Key concepts solid:
- Idempotency via order_id as primary key, PENDING insert
- Exactly-once = at-least-once (retry) + at-most-once (idempotency)
- Reconciliation for stuck PENDING transactions
- Outbox pattern — Debezium vs polling
- Kafka delivery semantics — at-least-once via offset commit
- __consumer_offsets internal topic, per partition per consumer group

### Ad Click Event Aggregation System Design (SD-08)

Designed a large-scale ad click aggregation system, Rakuten stack focused.

**High level flow:**
- Click → Kafka → Flink → HBase (raw) + BigQuery/OLAP (aggregated)
- Spark nightly batch → reconciliation against GCS data lake
- Lambda Architecture: speed layer (Flink) + batch layer (Spark)

**Key concepts covered:**
- LSM Tree — why HBase and Cassandra are write-heavy optimized
- Reversed timestamp trick in HBase row key design
- Flink windowing — tumbling vs sliding, event time vs processing time, watermarks
- Flink checkpoint + Kafka retention = fault tolerance + exactly-once
- OLAP columnar storage — why it's fast for aggregation queries
- Hot shard problem — random suffix on partition key, strip before writing to DB
- Impression ID + HMAC — idempotent click tracking, dedup before Kafka
- Lambda Architecture — speed layer for latency, batch layer for correctness

**Notes saved to:** notes/system-design/SD-08-ad-click-aggregation.md

### Other

Updated LinkedIn About section with expanded specialization bullets.
New contract position opportunity in advertising technology — applied via recruiter.

## Next

- Review SD-07 and SD-08 notes before any interview
- Pair programming practice — broken Spring Boot code format
- SLI/SLO concepts