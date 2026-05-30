# [5.2–5.6] Kafka Deep Dive

## Keywords
acks · ISR · in-sync-replica · idempotent-producer · exactly-once · transactional-api ·
rebalancing · stop-the-world · cooperative-sticky · static-membership · group.instance.id ·
session.timeout.ms · heartbeat.interval.ms · max.poll.interval.ms · back-pressure ·
log-compaction · producer-tuning · batch.size · linger.ms · unclean-leader-election ·
hot-partition · salting · Decaton · watermark-offset-commit · DLQ · retry-topic

---

## [5.2] Delivery Semantics & Durability

### The Three Modes

At-most-once:
→ Commit before processing. Message may be lost on crash.
→ Use case: analytics, logs — loss tolerable, speed priority.
→ Never for messaging.

At-least-once:
→ Commit after processing. Message never lost, may be duplicated.
→ Most common choice. Handle duplicates with idempotency key (clientMessageId).
→ LINE's choice for messaging.

Exactly-once:
→ Never lost, never duplicated.
→ Requires idempotent producer + transactional API.
→ Expensive — performance cost. Use for financial operations.

### acks Setting

```
acks=0   → producer does not wait. Fire and forget. Fastest, least safe.
acks=1   → leader broker acknowledged. Leader crash before replication → lost.
acks=all → leader + all ISR replicas acknowledged. Safest.
```

Messaging system: acks=all. "Messages must not be lost."

### Durability Trio (always together)

```
acks=all
replication.factor=3
min.insync.replicas=2
```

Why: 3 replicas exist. 1 crashes. Still 2 in sync → acks=all still satisfied → system continues.

### Idempotent Producer

Problem: producer sends message, timeout, retries → broker receives twice → duplicate.
Solution: each producer gets a producer ID + sequence number per partition.
Broker sees same producer ID + sequence → rejects duplicate.
Enable: `enable.idempotence=true`

### Transactional API

Atomic writes across multiple partitions — either all go through or none.
Used mainly in Kafka Streams for exactly-once end-to-end.
Exactly-once = idempotent producer + transactional API together.

### ISR (In-Sync Replicas)

ISR = replicas that are caught up with the leader.
acks=all waits for all ISR members to confirm.
ISR shrinks when a replica falls behind → min.insync.replicas prevents writing with too few replicas.
Monitor ISR size — if it drops to 1, you're one failure away from data loss.

---

## [5.3] Rebalancing

### What Is It

When partition-to-consumer assignment changes, Kafka redistributes partitions across consumers.

### Triggers

```
1. Consumer joins the group (new instance deployed)
2. Consumer leaves or crashes
3. Partition count changes
4. Session timeout — consumer stops sending heartbeats
```

### Stop-the-World Problem

Eager rebalancing (classic): ALL consumers stop, return all partitions, wait for new assignment.
At 200M users / 25B messages per day → any pause = real impact.

### Solution: Cooperative Sticky Rebalancing

Only partitions that need to move are reassigned.
Other consumers keep reading — no stop-the-world.
`partition.assignment.strategy=CooperativeStickyAssignor`

### Key Timeouts

```
session.timeout.ms      → how long without heartbeat before consumer is declared dead
heartbeat.interval.ms   → how often consumer sends heartbeat
max.poll.interval.ms    → max time between two poll() calls
```

Rule: `session.timeout.ms = 3 × heartbeat.interval.ms`
Same logic as Redis TTL + WebSocket heartbeat — 3x is the industry standard.

Why 3x: 1 heartbeat can be lost, 2nd too, 3rd missing → real problem.

False rebalancing: consumer is alive but processing a heavy batch → misses heartbeat → Kafka declares it dead → rebalance → consumer comes back → partitions already reassigned.
Fix: increase `max.poll.interval.ms` or reduce batch size.

### Static Membership

Problem: Kubernetes pod restarts → Kafka sees a new consumer → unnecessary rebalance.
Solution: `group.instance.id` — fixed ID per consumer instance.
Pod restarts with same ID → Kafka recognizes it → no rebalance triggered.

### Debug Scenario: Consumer Group Rebalancing Every 3 Minutes

```
1. Check consumer logs → look for "Rebalancing" messages
2. Check max.poll.interval.ms → is processing taking longer than this?
3. Measure actual processing time per batch
4. Check session.timeout.ms / heartbeat.interval.ms ratio → is it 3:1?
5. Fix: increase max.poll.interval.ms or reduce max.poll.records
```

---

## [5.4] Advanced

### Back-Pressure

Kafka is pull-based → consumer controls pace. Natural back-pressure built in.
Consumer can't keep up → it simply doesn't pull more.

Problem: consumer processes at 10K/sec, Kafka has 50K/sec incoming.
Solutions:
```
→ Add consumers (up to partition count)
→ Reduce max.poll.records → smaller batches
→ Reduce fetch.max.bytes → less data per fetch
→ Optimize consumer processing logic
```

Key insight: pull-based is an advantage over push-based (RabbitMQ).
Push-based can overwhelm consumers. Pull-based cannot — consumer controls the pace.

Other back-pressure configs:
```
fetch.min.bytes       → wait for this much data before returning (reduces requests)
fetch.max.wait.ms     → max wait even if fetch.min.bytes not reached
buffer.memory         → producer-side buffer before blocking
max.block.ms          → how long producer blocks when buffer full
pause/resume          → manually pause a consumer partition, resume when ready
```

### Log Compaction

Normal retention: messages deleted after 2 weeks.
Log compaction: per-key retention — only the latest value per key is kept.

```
Key: user_123 → online
Key: user_123 → offline
Key: user_123 → online
Key: user_123 → offline   ← only this survives after compaction
```

Use case: user presence state, conversation metadata, last read receipt.
Not for message history — every message matters there, nothing should be deleted.

### Producer Tuning

```
batch.size    → how many bytes before sending (default 16KB)
linger.ms     → wait this long even if batch not full
compression   → lz4 (speed) vs zstd (ratio)
```

Trade-off:
```
linger.ms high → bigger batches → higher throughput, higher latency
linger.ms=0   → send immediately → low latency, small batches
```

Messaging system: `linger.ms=0` or very low — latency is priority.
Analytics pipeline: higher `linger.ms` — throughput is priority.
Compression: `lz4` for messaging — speed over ratio.

### Unclean Leader Election

Leader crashes. ISR replicas exist but are behind (missed some messages).
Two choices:
```
Wait for ISR replica to catch up → system paused, no data loss
Elect non-ISR replica as leader → system continues, messages lost
```

`unclean.leader.election.enable=false` — always for messaging.
"Availability can be sacrificed. Durability cannot."

### Kafka Streams vs Raw Consumer API

```
Raw Consumer API:
→ Full control
→ Simple pipeline: receive → process → forward
→ No state management, no windowing needed
→ Messaging system delivery pipeline → raw API

Kafka Streams:
→ Built-in state store, windowed aggregation, stream joins
→ Exactly-once built-in
→ Use for: analytics, real-time aggregation, stateful processing
→ "How many messages sent in the last 5 minutes?" → Kafka Streams
```

Messaging system: raw Consumer API — no stateful processing needed.

### Hot Partition Problem

Problem: chat_id as partition key. One chat with millions of users → all messages to one partition → that partition overwhelmed, others idle.

Solutions and trade-offs:
```
Salting (add random suffix to key):
+ Distributes load across partitions
- Breaks ordering → unacceptable for messaging

Add more partitions:
+ Slightly better distribution
- Does not fully solve skew

Custom partitioner:
+ Load-based routing
- Still ordering risk

Decaton (LINE's solution):
+ Concurrent processing within one partition
+ Ordering preserved via watermark commit
→ Best solution for messaging — see [5.6]
```

### Retry Topics + DLQ

Consumer fails to process → retrying in place blocks partition → all following messages wait.

Solution — retry topic chain:
```
main-topic → consumer fails
→ retry-topic-1 (retry after 1 min)
→ retry-topic-2 (retry after 5 min)
→ retry-topic-3 (retry after 30 min)
→ dlq (dead letter queue)
```

DLQ: alert fires → engineer investigates manually → fix → replay.
"Messages must not be lost silently" → DLQ + alert satisfies this requirement.
Poison message = message that always fails regardless of retries.

---

## [5.5] Outbox Pattern

Problem: save to DB + publish to Kafka → two separate systems → cannot be atomic.
Service crashes between the two → DB has record, Kafka does not → inconsistency.

Solution:
```
BEGIN TRANSACTION
  INSERT INTO messages (...)      ← actual record
  INSERT INTO outbox (event, status=PENDING)  ← outbox record
COMMIT                            ← atomic — both or neither
```

Separate outbox sender reads PENDING records → publishes to Kafka → marks as PROCESSED.

Two implementations:

Polling:
```
+ Simple to implement
- DB query every N seconds → overhead
- Latency up to polling interval
```

CDC (Change Data Capture via Debezium):
```
+ Reads DB WAL (transaction log) directly — no polling overhead
+ Real-time
- Complex infrastructure setup
```

At-least-once risk: outbox sender may publish same event twice (crash after publish, before marking PROCESSED).
Solution: unique event ID + idempotent consumer.

---

## [5.6] LINE-Specific: Decaton

### The Problem

One partition → one consumer (Kafka rule).
High-traffic chat: saniyede 100K+ mesaj → single consumer can't keep up.
Cannot add consumers (1 partition = 1 consumer).
Cannot use salting (breaks ordering).

### What Decaton Does

Concurrent processing within a single consumer, while preserving ordering.

```
Normal Kafka consumer:
msg_1 → process → msg_2 → process → msg_3 → process  (sequential)

Decaton:
msg_1, msg_2, msg_3 → process concurrently
but offset commit is ordered via watermark
```

### Watermark Offset Commit

Memory-side tracker keeps processed status of all in-flight messages.
Watermark = leftmost completed message.

```
[✅ msg_1] [✅ msg_2] [✅ msg_3] [⏳ msg_4] [✅ msg_5]
Watermark = msg_3 → commit offset 3 to Kafka
msg_4 still processing → cannot advance watermark yet
```

Commit is to `__consumer_offsets` topic — same as normal Kafka.
Watermark tracker lives in memory within the consumer process.

### Numbers

Decaton: 1M+ I/O-intensive tasks/sec per stream.
LINE uses it across the entire messaging pipeline.

### When NOT to Use Decaton

Use Kafka Streams instead when:
→ Windowed operations needed (aggregate over time)
→ Stream joins needed
→ Built-in state store needed

Decaton = concurrency tool, not analytics tool.

### Interview Connection

"Hot partition problem can't be solved by adding consumers — one partition, one consumer rule.
Salting breaks ordering — unacceptable for messaging.
Decaton solves this by enabling concurrent processing within a single consumer
while preserving message ordering via watermark offset commits.
LINE achieves 1M+ tasks/sec per stream with this approach."

---

## Interview Checklist — New Material

→ acks=all + replication.factor=3 + min.insync.replicas=2 → durability trio
→ Idempotent producer → producer ID + sequence, enable.idempotence=true
→ Exactly-once → idempotent producer + transactional API, expensive, for financials
→ Rebalancing triggers → join/leave/crash/timeout
→ Stop-the-world → eager rebalancing, all consumers pause
→ Fix → cooperative sticky rebalancing, only moving partitions reassigned
→ 3x rule → session.timeout = 3 × heartbeat.interval (same as Redis TTL pattern)
→ False rebalancing → heavy processing → missed heartbeat → fix: increase max.poll.interval.ms
→ Static membership → group.instance.id → K8s pod restarts without rebalance
→ Back-pressure → pull-based natural back-pressure, reduce max.poll.records
→ Log compaction → per-key retention, use for state (presence, last read receipt)
→ Unclean leader election → always false for messaging, availability < durability
→ Hot partition → salting breaks ordering → Decaton solves at application level
→ DLQ → retry chain → alert → manual fix → replay
→ Outbox → atomic DB write, separate sender to Kafka, polling vs CDC
→ Decaton → concurrent within partition, watermark commit, 1M+ tasks/sec

---
