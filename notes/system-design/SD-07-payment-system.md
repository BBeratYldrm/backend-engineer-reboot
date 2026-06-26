# Payment System Design
// keywords: payment, PSP, idempotency, outbox, debezium, kafka, ledger, wallet, reconciliation

## 1. Scope (Step 1)

- 1M transactions/day, ~10 TPS average, ~50 TPS peak
- Single country, third-party PSP (Stripe)
- Pay-in flow + Pay-out flow
- No double charge — critical requirement
- We do NOT store card numbers (PCI DSS)

## 2. Key Terminology

- Merchant → seller, our direct customer
- PSP (Payment Service Provider) → Stripe, moves money between accounts
- Pay-in → money from user to our account
- Pay-out → money from our account to merchant's bank
- Ledger → append-only financial record, legal requirement, double-entry
- Wallet → merchant's balance in our system

## 3. High Level Design (Step 2)

```
User
  ↓
API Gateway (rate limiting - token bucket - Redis)
  ↓
Payment Service
  ├── Redis → idempotency check (order_id, 24h TTL, write after SUCCESS)
  ├── DB check → PENDING insert (duplicate check via unique constraint)
  ├── Balance check
  ├── Stripe → exponential backoff + jitter → DLQ on max retries
  ├── PostgreSQL (ACID) → update status SUCCESS/FAILED
  └── Outbox table → same transaction as DB write
         ↓
    Debezium (WAL) or Polling Job
         ↓
       Kafka (at-least-once)
      ↙        ↘
Ledger        Notification
Service       Service
(append-only) (merchant webhook)

Scheduled Jobs:
  → Pay-out Service → Merchant bank (batch, not event-driven)
  → Reconciliation Job → compare with Stripe settlement file nightly
```

## 4. Deep Dive (Step 3)

### Idempotency
- order_id comes from upstream (e-commerce service), not generated here
- order_id = primary key in payments table = idempotency key
- First request: INSERT with status=PENDING → success → proceed
- Duplicate request: INSERT fails (unique constraint) → reject
- Redis cache: store processed order_ids with 24h TTL, write AFTER SUCCESS only

### Exactly-once delivery
- Does not exist natively in distributed systems
- at-least-once (retry) + at-most-once (idempotency) = exactly-once behavior
- Kafka layer: use at-least-once + idempotency check at consumer side
- Application layer: retry PSP call + idempotency key on PSP side

### Exponential backoff + Jitter
- Double wait time on each retry: 1s → 2s → 4s → 8s
- Jitter: add random variation to prevent thundering herd
- After max retries: route to DLQ

### Reconciliation
- Stuck PENDING transactions: scheduled job queries PSP for actual status
- Nightly: compare internal ledger with Stripe settlement file
- Mismatches: auto-fix if classifiable, manual fix otherwise

### Outbox Pattern
- Payment Service writes to outbox table in SAME transaction as DB update
- Never publish directly to Kafka from service — risk of inconsistency
- Two relay options:

**Polling:**
+ Simple, no extra infrastructure
- DB load (SELECT + UPDATE every interval)
- Higher latency (waits for next poll cycle)

**Debezium (CDC):**
+ Low latency (reads WAL directly)
+ No extra DB load
+ Tracks LSN position, resumes after Kafka downtime
- Extra infrastructure (Kafka Connect + Debezium plugin)
- Must connect to primary/master node (WAL only on primary)
- WAL config: `wal_level = logical` in postgresql.conf

Debezium event structure:
```json
{
  "op": "c",
  "after": {
    "id": "uuid-123",
    "aggregate_type": "PAYMENT",
    "payload": "{\"eventType\":\"PAYMENT_SUCCESS\",\"orderId\":\"12345\"}"
  }
}
```
→ Consumer opens "after.payload" to get domain event

### DB Choice
- PostgreSQL — ACID, proven stability, rich tooling
- NOT Cassandra/HBase — AP systems, eventual consistency, unacceptable for payments
- Scaling path: shard by payment_id using consistent hashing when TPS grows

### Redis Usage
- Rate limiting: token bucket counters (INCR + TTL)
- Idempotency cache: processed order_ids (SET key value EX 86400)

### Kafka Delivery Semantics
- at-least-once: process message → commit offset (message can arrive twice)
- at-most-once: commit offset → process message (message can be lost)
- exactly-once: Kafka native transactions (heavy, rarely used)
- Offsets stored in: __consumer_offsets internal topic, per partition per consumer group

## 5. Observability (Step 3)

Three pillars:
- Logs → ELK Stack (structured JSON, Trace ID in every line)
- Metrics → Prometheus + Grafana (DLQ count, PENDING count, PSP success rate, latency)
- Traces → Jaeger or Zipkin (distributed tracing)

Trace ID flow:
```
API Gateway generates Trace ID
  → propagated via HTTP headers to all internal services
  → added to Kafka event payload
  → logged in every service
```

DLQ alert response:
1. Grafana → when did it start, which service
2. Kibana → search by Trace ID from failed message
3. Jaeger → find exact failure point and latency
4. Fix root cause
5. Replay DLQ messages

## 6. Internal Communication

- External (User → API Gateway): REST
- Internal (service to service): gRPC
    + Binary → 5-10x faster than JSON
    + Type safety via proto contracts
    + Code generation
    + HTTP/2 multiplexing
- PSP (Stripe): REST (their API)
- Async events: Kafka

## 7. Security (Scope out most, delegate)

- Card data: never stored, PSP (Stripe) handles via hosted payment page (PCI DSS)
- User auth: JWT / OAuth2
- Internal: mTLS between services
- Transport: TLS everywhere
- 3D Secure: handled by Stripe, not us

## 8. Wrap Up (Step 4)

Scaling path when TPS grows:
- Shard PostgreSQL by payment_id (consistent hashing)
- Scale Kafka partitions + consumers horizontally
- Add read replicas for reporting queries

Monitoring alerts:
- Stuck PENDING transactions > threshold
- DLQ message count spike
- PSP call failure rate > X%
- Average payment latency > Xms
