# Day 68 — June 24, 2026

## What I studied

### Payment System Design — Full Session (Alex Xu Vol.2 Chapter 11)

Completed a full payment system design session following the 4-step framework.

**Step 1 — Scope**
- 1M transactions/day, ~10 TPS average, ~50 TPS peak
- Single country, third-party PSP, pay-in and pay-out flows required
- No double charge as critical requirement

**Step 2 — High Level Design**
- API Gateway with token bucket rate limiting (Redis-backed)
- Payment Service handling order processing, PSP integration, DB writes
- PostgreSQL for payment storage — chosen for ACID guarantees over Cassandra/HBase
- Outbox Pattern within same DB transaction to guarantee event delivery
- Kafka for async side effects — Ledger Service and Notification Service as consumers
- Scheduled job for pay-out to merchant bank accounts

**Step 3 — Deep Dive**
- Idempotency via order_id as primary key — PENDING insert prevents double charge
- Exactly-once = at-least-once (retry) + at-most-once (idempotency)
- Exponential backoff with jitter for PSP retries — prevents thundering herd
- Dead letter queue for permanently failed messages
- Reconciliation job for stuck PENDING transactions — queries PSP for actual status
- Redis for rate limiting counters and idempotency cache (24h TTL, write after SUCCESS)
- Kafka delivery semantics — at-least-once via offset commit, idempotency at application layer
- __consumer_offsets internal topic tracks per-partition consumer group progress

**Step 4 — Wrap Up**
- Monitoring: alerts on stuck PENDING, DLQ count, failed PSP calls
- Scaling path: PostgreSQL sharding by payment_id when TPS grows
- Reconciliation: nightly comparison with PSP settlement file

### Key concepts clarified
- order_id produced by upstream service, not payment system — clean scope separation
- Ledger is append-only, eventually consistent, legal requirement
- Wallet is merchant balance tracker, separate from payment storage
- Pay-out is scheduled batch job, not event-driven — cost and efficiency reason
- Kafka exactly-once vs application-layer exactly-once are different layers entirely

## Job pipeline update

New opportunity added to pipeline. Advertising technology position at a major e-commerce company — Java, Spring Boot, MySQL, HBase, Kafka, GCP stack. Single interview, general technical conversation format. Applied via recruiter.

## Next

- Payment system deep dive continuation: sharding, Debezium vs polling, observability, internal communication patterns
- Pair programming practice — broken Spring Boot code, find and fix format
- Security topics: mTLS, PCI DSS scope