# [3.4] Messaging — ActiveMQ & Kafka

## Why Message Queue?

Problem: reservation saved → need to send email
Direct approach: send email inside the transaction → slow, risky

Solution: Message Queue
Producer → puts message in queue → returns immediately
Consumer → picks up message → sends email async

Three reasons to use MQ:
1. Performance → user doesn't wait, system returns instantly
2. Transactional independence → DB commit and email are separate
   Email cannot be rolled back — must be outside @Transactional
3. Durability → if server is down, messages wait in queue
   When server comes back → processes them in order

## Producer → Queue → Consumer

Producer: the service that puts message in queue
(tirebringin-front → ReserveResultService)

Queue: ActiveMQ — just a postman, doesn't read the message
message format at Rakuten: "1:reservationNum"
1 = mail type, reservationNum = reservation ID

Consumer: the service that reads and processes
(batch service → reads queue → builds and sends email)

ActiveMQ does NOT send emails — it only transports messages.
The consumer does the actual work.

## High Availability — Failover

spring.activemq.broker-url:
failover:(tcp://server1:61616, tcp://server2:61616)

If server1 goes down → automatically switch to server2
If both down → messages wait → processed when connection restored

## Retry Topic & Dead Letter Queue (DLQ)

Problem:
Consumer gets a message → processing fails → what now?
Infinite retry → system stuck → other messages not processed

Solution: Retry with limit + DLQ

Retry strategy:
Attempt 1 → fail → wait 30s → retry
Attempt 2 → fail → wait 60s → retry  
Attempt 3 → fail → wait 120s → retry
Still failing → send to DLQ

Why increasing wait time? (Exponential backoff)
→ Give the downstream service time to recover
→ Don't hammer a sick service immediately

Dead Letter Queue (DLQ):
→ Graveyard for messages that couldn't be processed
→ Alert sent to developers
→ Logs written — which message, which error, how many retries
→ Developer investigates — bug fix or manual reprocessing
→ Never silently discard a failed message

Kafka retry topic pattern (no built-in DLQ):
payment.debited          → normal topic
payment.debited.retry-1  → first retry, wait 30s
payment.debited.retry-2  → second retry, wait 60s
payment.debited.retry-3  → third retry, wait 120s
payment.debited.dlq      → dead letter queue

Poison message:
Message that can never be processed — corrupt data, null field, wrong format
Consumer crashes immediately, can't even retry
Solution: wrap in try-catch → if unparseable → skip retries → send directly to DLQ

Rakuten connection:
ActiveMQ had built-in DLQ — ActiveMQ.DLQ
monitor_queue script was watching it
If messages piled up → alarm triggered

Real world:
Uber → retry + DLQ for every critical topic
Netflix → video encoding failures → DLQ → team notified
Revolut → payment event in DLQ → compliance team investigates
DLQ messages in fintech = potential lost money → critical

The question I ask myself:
"What happens if this message can never be processed?"
→ Always have a DLQ
→ Always alert on DLQ messages
→ Never silently discard

## Outbox Pattern

Problem:
DB commit → then send Kafka event
If service crashes between these two → DB has record, Kafka has no event
Distributed transaction impossible — DB and Kafka can't share @Transactional

Solution: Write to outbox table in the SAME transaction as the main operation.

@Transactional
public void processPayment(Payment payment) {
paymentRepository.save(payment);           // main record
outboxRepository.save(new OutboxEvent(...)); // outbox record
// both commit or both rollback — atomically
}

Separate outbox reader sends events to Kafka.

Why it works:
+ DB commit → outbox commit → event eventually sent ✅
+ DB rollback → outbox rollback → no event sent ✅
+ Service crashes after commit → outbox reader still sends ✅

Two ways to read outbox:
1. Polling: scan outbox table every 1s, send PENDING events to Kafka
   + Simple
   - Extra DB load

2. CDC (Change Data Capture) — Debezium:
   Watch DB changes → auto send to Kafka when new outbox row appears
   + No extra DB load, real-time
   - Extra infrastructure (Debezium, Kafka Connect)

Outbox table structure:
id, event_type, aggregate_id, payload, status, created_at
status: PENDING → SENT

Real world:
Revolut → every financial operation uses outbox
"at least once event delivery" guarantee
Uber → trip events via outbox, "ride started" never lost
Amazon → order events atomically saved with order record

Rakuten connection:
Monolith — this problem didn't exist (same DB, same transaction)
If it were microservices:
ReserveSaveDataService → save reservation + outbox record
Outbox reader → send to ActiveMQ
Without outbox: crash between DB save and MQ send → lost email notification

The question I ask myself:
"What if my service crashes after DB commit but before sending the event?"
→ If the answer is 'data inconsistency' → use Outbox Pattern

## RabbitMQ

Traditional message broker — like ActiveMQ but more modern and widely used.

Key difference from Kafka:
RabbitMQ → message delivered → deleted (fire and forget)
Kafka    → message delivered → stored (event log)

RabbitMQ's killer feature — Exchange types:
Direct  → exact routing key match → goes to specific queue
Topic   → pattern matching → "payment.*" matches payment.success, payment.failed
Fanout  → broadcast → send to ALL queues → system-wide notifications

When to use RabbitMQ:
+ Complex routing needed
+ Message must be processed immediately, no need to store
+ Task queue — distribute work across workers
+ Low latency required
  → Email, SMS, notifications, background jobs

When to use Kafka instead:
+ High throughput (millions/sec)
+ Need event history — replay past events
+ Multiple consumers reading same event
+ Audit log, analytics, event sourcing
  → Financial transactions, user behavior tracking

Trade-offs:
RabbitMQ:
+ Easy setup, good UI, complex routing, low latency
- Messages lost after delivery — no replay
- Doesn't scale as well as Kafka

Kafka:
+ High throughput, message retention, replay
- More complex setup
- Ordering requires careful partition key design

Rakuten connection:
Email/SMS via ActiveMQ → correct choice ✅
RabbitMQ would work the same way
If analytics were needed → Kafka would be better

Real world:
Instagram → RabbitMQ for notifications
Zalando → RabbitMQ for order processing
Most companies → RabbitMQ for simple async tasks, Kafka for event streaming

## Redis + Kafka Together

Problem:
Redis cache is fast but gets stale when data changes.
Who invalidates the cache? Service directly? → tight coupling.

Solution: Event-driven cache invalidation via Kafka.

Product Service → updates DB
→ publishes "product.price.updated" to Kafka

Cache Service listens to Kafka:
→ receives "product.price.updated"
→ deletes or updates Redis key
→ next request gets fresh data

Why this combination:
Redis alone → fast reads but manual cache invalidation, tight coupling
Kafka alone → event-driven but slow reads (DB speed)
Redis + Kafka → fast reads + loosely coupled invalidation ✅

Connection to CQRS:
This IS the event-driven read model update we discussed in CQRS.
Write → Kafka event → Redis (read model) updated
CQRS + Redis + Kafka = complete read model implementation

Real world:
Twitter → tweet written → Kafka event → Redis timeline updated → fast reads
Amazon → stock updated → Kafka event → Redis cache invalidated
Netflix → user preference changed → Kafka → Redis recommendation cache updated

Rakuten connection:
ShopSearchService fetched shop data on every search.
With Redis + Kafka:
Shop updated → Kafka event → Redis cache invalidated
User searches → served from Redis instantly

The question I ask myself:
"Does this data change frequently but get read much more than it's written?"
+ Yes → Redis for reads + Kafka for cache invalidation
+ This is the classic read-heavy, write-sometimes pattern