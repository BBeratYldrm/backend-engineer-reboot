# [12] Distributed Systems

## Keywords
cap-theorem · eventual-consistency · split-brain · quorum · raft · circuit-breaker ·
bulkhead · timeout · rate-limiter · token-bucket · distributed-tracing · trace-id ·
span · opentelemetry · red-metrics · use-metrics · lambda-architecture · reconciliation

---

## [12.1] CAP Theorem

Distributed systems cannot guarantee all three simultaneously:
- C → Consistency: all nodes see the same data
- A → Availability: system always responds
- P → Partition Tolerance: survives network partition

P is unavoidable in distributed systems → real choice is C vs A.

Messaging → AP
+ system always responds
+ eventual consistency acceptable (100ms delay tolerable)
- stale data possible during partition → resolves when partition heals

Banking → CP
+ strong consistency required
- availability sacrificed during partition

---

## [12.2] Eventual Consistency

Definition: system becomes consistent over time, not immediately.
Network partition → User B cannot see message → partition heals → message appears.
This is acceptable for messaging. Not acceptable for financial transactions.

ACID → relational DB, strong consistency, single server
BASE → distributed systems, eventual consistency, availability first
BA → Basically Available
S  → Soft state
E  → Eventually consistent

---

## [12.3] Exactly-once vs At-least-once End-to-End

At-least-once + idempotency key → practical choice for messaging
→ Kafka at-least-once delivery + clientMessageId deduplication
→ consumer checks: "did I already process this message_id?"
→ if yes → skip, if no → process + mark as processed

Exactly-once → idempotent producer + transactional API → expensive, for financials only

---

## [12.4] Consistent Hashing + Raft/KRaft

Consistent hashing → covered in Redis Cluster and HBase sections
→ CRC16(key) % 16384 in Redis, salt prefix in HBase

Raft consensus:
→ leader election via majority vote
→ log replication: leader → followers → majority ack → commit
→ Kafka uses KRaft (Raft-based) since ZooKeeper removal

Why odd number of nodes (3, 5, 7):
→ 2 nodes → tie possible → split-brain
→ 3 nodes → majority always on one side

---

## [12.5] Resilience Patterns

### Circuit Breaker
Definition: prevents cascade failure by stopping calls to a failing downstream service.
CLOSED    → normal, requests pass through
OPEN      → failure rate exceeded threshold → fail-fast, no requests
HALF_OPEN → after wait duration → test requests → if ok → CLOSED

+ we do not open/close it manually → Resilience4j monitors automatically
+ fallback method handles OPEN state

Config:
failureRateThreshold=50     → 50% errors → OPEN
waitDurationInOpenState=30s → 30s then HALF_OPEN
permittedCallsInHalfOpen=3  → 3 test requests

### Bulkhead
Definition: resource isolation per downstream service → prevents one slow service from consuming all threads.

Named after ship compartments → one compartment floods, ship survives.
Without → HBase slow → all threads waiting → nothing else works
With    → HBase gets max 10 threads → other services unaffected

+ circuit breaker cuts the service entirely
+ bulkhead limits resources but keeps service running

### Timeout
Definition: give up waiting after N milliseconds → free the thread → return error fast.
Without → thread waits 30s → thread pool fills → cascade failure
With    → timeout after 2s → retry or fallback → system stays healthy

Circuit breaker + bulkhead + timeout work together:
→ timeout causes errors → errors accumulate → circuit breaker OPEN

### Rate Limiter
Definition: limits requests per time window → prevents abuse and burst overload.

Token bucket → best for messaging:
+ allows natural burst (user opens many chats at once)
+ then returns to normal rate
+ fixed window does not allow burst → rejects legitimate traffic at window boundaries

Implementation: Redis INCR + TTL for distributed rate limiting.

---

## [12.6] Distributed Tracing

Problem: request spans multiple services → which service caused the failure?

Solution:
→ API Gateway assigns unique Trace ID to every request
→ Trace ID travels in HTTP headers service to service
→ every service logs the Trace ID
→ search by Trace ID in Kibana → full request journey visible
Trace ID: abc123
Span 1: API Gateway       0-10ms   ✅
Span 2: Message Service   10-50ms  ✅
Span 3: Kafka publish     50-80ms  ✅
Span 4: Delivery Service  80-120ms ❌ → error here

Tools:
→ OpenTelemetry → vendor-neutral instrumentation (preferred)
→ Jaeger / Grafana Tempo → visualization
→ ELK Stack → log aggregation + Kibana search by trace_id

### RED Metrics (for services)
Rate     → requests per second
Errors   → error rate
Duration → latency (p50, p95, p99)

### USE Metrics (for infrastructure)
Utilization → resource usage (CPU, memory, disk)
Saturation  → queue depth, consumer lag
Errors      → hardware/system errors

Messaging:
→ RED on Message Service: QPS, error rate, latency
→ USE on Kafka: broker CPU, consumer lag (saturation), broker errors
→ Prometheus collects, Grafana visualizes

---

## [12.7] Split-Brain + Reconciliation

Split-brain: network partition → two sides each elect a leader → data diverges.
Broker 1      |      Broker 2
|      Broker 3
(partition)
Broker 1 → "I am leader"
Broker 2-3 → "Broker 1 is dead, we elect new leader"
→ two leaders → two diverging data sets

Solution: Quorum (majority voting)
→ 3 nodes → majority = 2
→ Broker 1 alone → 1 vote → cannot become leader
→ Broker 2-3 together → 2 votes → valid leader
→ single source of truth preserved

Kafka KRaft uses Raft for this. Always use odd number of nodes.

Reconciliation: partition heals → losing side discards its writes → syncs from winner.

Lambda Architecture:
Speed layer  → Kafka → real-time, fast, approximate
Batch layer  → Spark/HBase → slow, accurate, full history
Serving layer → merges both → correct final result

Messaging example:
→ unread count in Redis (speed) may drift during partition
→ nightly batch recalculates from HBase (batch)
→ serving layer returns accurate count