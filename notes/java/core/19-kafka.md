# [5.1] Kafka

## Keywords
kafka · topic · partition · offset · producer · consumer · consumer-group ·
replication-factor · broker · leader · replica · outbox-pattern · cdc ·
retry-topic · dlq · at-least-once · exactly-once · ordering · partition-key ·
debezium · idempotent-consumer

---

## Why Kafka

ActiveMQ, RabbitMQ — memory-based queues.
Queue fills up → messages lost or broker crashes.
Not suitable for billions of messages.

Kafka differences:
→ Disk-based — messages written to disk, not memory
→ Messages not deleted after consumption — retained for configured period
→ Pull model — consumer controls the pace
→ Replay — reprocess past messages anytime
→ Horizontal scale — add partitions, add consumers

LINE processes 10 billion messages per day.
ActiveMQ would crash. Kafka handles it.

---

## Core Concepts

Producer:
Service that writes to Kafka.
Not the user — the backend service that receives user action and writes to Kafka.

Chat Service receives "hello" → writes to "chat-messages" topic → Chat Service is producer.

Topic:
Category of messages. Like a DB table but for events.
Created explicitly with partition count and replication factor.

kafka-topics.sh --create --topic chat-messages --partitions 6 --replication-factor 3

Partition:
Topic split into partitions for parallel processing.
Each partition is an ordered, immutable log.
More partitions → more consumers in parallel → higher throughput.

Offset:
Sequence number of each message within a partition.
Consumer tracks its offset — restarts from where it left off.
No message loss on restart.

Consumer:
Service that reads from Kafka. Pull model — consumer controls pace.
Commits offset after processing — tells Kafka "I processed up to here."

Consumer Group:
A service and its instances (replicas).
Kafka distributes partitions across instances in the same group.
Each partition assigned to exactly one instance within the group.

Different services → different groups → each group receives all messages independently.

---

## Partition Assignment

6 partitions, 3 instances in notification-group:
Instance 1 → Partition 0, 1
Instance 2 → Partition 2, 3
Instance 3 → Partition 4, 5

6 partitions, 6 instances → each instance gets 1 partition (maximum parallelism)
6 partitions, 9 instances → 3 instances idle (partition count is the ceiling)

Max parallelism = partition count.
Scale consumers beyond partition count → no benefit.

---

## Ordering Guarantee

Kafka guarantees order within a partition only.
Across partitions — no ordering guarantee.

Solution: partition key.
Same key → always same partition → order preserved.

producer.send(new ProducerRecord<>("chat-messages", userId, message));

userId as key → all messages from same user → same partition → ordered.
Different users → different partitions → processed in parallel, independent order.

---

## Replication Factor

Each partition replicated across multiple brokers.
Replication factor 3 → partition exists on 3 brokers.

One broker = leader (handles reads and writes)
Others = replicas (stay in sync)

Broker crashes → replica automatically becomes leader.
No data loss, no manual intervention.

Production standard: replication-factor = 3, min.insync.replicas = 2.

---

## Delivery Semantics

At-most-once:
→ Message may be lost, never duplicated
→ Use when: analytics, logs — loss tolerable

At-least-once:
→ Message never lost, may be duplicated
→ Most common choice
→ Requires idempotent consumer to handle duplicates safely

Exactly-once:
→ Never lost, never duplicated
→ Expensive — Kafka Transactions required
→ Use when: financial operations, critical data

At-least-once + idempotent consumer = safe and practical for most systems.

Idempotent consumer:
Each message has unique ID.
Consumer checks Redis before processing:
→ ID seen before → skip
→ ID not seen → process, save ID to Redis with TTL

---

## Outbox Pattern

Problem:
@Transactional
public void createOrder(Order order) {
orderRepository.save(order);         // DB write
kafkaTemplate.send("orders", order); // Kafka write
}

DB and Kafka are separate systems — cannot write to both atomically.
Service crashes between the two → inconsistency.

Solution — Outbox Pattern:
Write to DB only. A separate process reads from DB and writes to Kafka.

@Transactional
public void createOrder(Order order) {
orderRepository.save(order);
outboxRepository.save(new OutboxEvent("OrderCreated", serialize(order)));
// both in same DB transaction — atomic
}

Outbox Poller (separate process):
Reads unprocessed outbox events → sends to Kafka → marks as sent.
Service crash → event stays in outbox → sent when service recovers.

Two implementations:

Polling:
+ Simple implementation
- Continuous DB query overhead
- Up to polling interval latency (typically 1s)

CDC — Change Data Capture (Debezium):
Reads DB transaction log directly.
New outbox row → immediately forwarded to Kafka.
+ Real-time, no polling overhead
- Complex setup
  Netflix, Uber use CDC in production.

Outbox at-least-once risk:
Poller may send same event twice.
Solution: unique event ID + idempotent consumer.

---

## Retry Topic and DLQ

Consumer fails to process a message.
Retrying in place blocks the partition — all following messages wait.

Solution — Retry Topics:
chat-messages → consumer fails
→ chat-messages-retry-1 (retry after 1 min)
→ chat-messages-retry-2 (retry after 5 min)
→ chat-messages-retry-3 (retry after 30 min)
→ chat-messages-dlq     (human intervention)

Main topic never blocked.
Failed message goes to separate retry path with backoff.

@RetryableTopic(
attempts = "3",
backoff = @Backoff(delay = 1000, multiplier = 2)
)
@KafkaListener(topics = "chat-messages")
public void consume(String message) {
processMessage(message);
}

DLQ: all retries exhausted → alert fires → engineer investigates manually.

---

## Kafka vs RabbitMQ vs ActiveMQ

Kafka:
+ Very high throughput — millions/billions of messages
+ Message retention — replay past messages
+ Multiple consumer groups — same message to many services
+ Horizontal scale — add partitions
- Complex setup and operations
- Not for simple task queues
- Overkill for low volume

RabbitMQ:
+ Flexible routing — direct, topic, fanout exchanges
+ Low latency — sub-millisecond
+ Simple setup
+ Good for task queues, RPC patterns
- Messages deleted after consumption — no replay
- Harder to scale than Kafka

ActiveMQ:
+ JMS standard — Java enterprise standard
+ Simple, familiar for Java teams
+ Good for low-medium volume
- Memory-based by default — not for high volume
- Older, less modern than RabbitMQ/Kafka
- Rakuten used this for email/SMS notifications — appropriate for that scale

When to use what:
Kafka      → high volume, replay needed, event streaming, multiple consumers
RabbitMQ   → task queues, complex routing, low latency needed
ActiveMQ   → JMS required, existing Java enterprise system, low-medium volume

---

## Interview Checklist
→ Why Kafka over ActiveMQ at scale? → disk-based, retention, replay, horizontal scale
→ Topic vs partition? → category vs parallel processing unit
→ Partition key purpose? → same key same partition → ordering preserved
→ Consumer group? → service and its instances, Kafka distributes partitions among them
→ Different services same message? → different consumer groups, each gets all messages
→ Max parallelism? → partition count — more instances than partitions = idle instances
→ At-least-once + idempotency? → safe practical combination for most systems
→ Outbox pattern why? → DB and Kafka cannot be written atomically, outbox bridges them
→ CDC vs polling? → CDC real-time via transaction log, polling simpler but adds latency
→ Retry topic why? → avoid blocking partition, failed messages go to separate retry path
→ Kafka vs RabbitMQ? → volume/replay/streaming vs routing/task-queue/low-latency