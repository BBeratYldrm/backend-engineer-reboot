# Day 48 — June 3, 2026

## What I studied

### Spring Boot — [4] completion

[4] Hibernate Fetch Types.
Lazy → loads related data only when accessed.
Eager → loads everything upfront.
N+1 problem: iterating over a list and triggering a query per item.
Fix: JOIN FETCH in JPQL → single query.

[4] Profiles and Actuator.
Profiles → dev/test/prod config separation, spring.profiles.active.
Actuator → /actuator/health for K8s probes, /actuator/prometheus for Grafana.
Spring Boot Actuator provides endpoints automatically — no manual implementation.

[4] Armeria.
LINE's open source async HTTP/2 + gRPC framework built on Netty.
Non-blocking, reactive streams backpressure support.
Handles millions of requests with fewer threads than thread-per-request model.

### Codility Design Defense — [10.2]

Full defense session on the 1-to-1 messaging system design submitted in Codility.
Went through every gap systematically, mülakat format.

GAP 1 — Redis presence value.
Original: value = "connected" → insufficient.
Fixed: value = wsServerId → Delivery Service routes directly to correct WS Server.
Without wsServerId, delivery becomes broadcast (pub/sub) or blind HTTP.

GAP 2 — Cassandra clustering key.
Original: schema had no PRIMARY KEY or clustering order defined.
Fixed: PRIMARY KEY (chat_id, message_id) WITH CLUSTERING ORDER BY (message_id DESC).
chat_id → partition key, same chat on same node.
message_id DESC → newest messages physically first, LIMIT 20 = O(1).
SnowFlake ID is time-sortable → no reversed timestamp trick needed like HBase.

GAP 3 — Delivery failure flow.
Original: "update status to FAILED and notify sender" — mechanism unspecified.
Fixed: retry with exponential backoff + jitter → DLQ topic → Notification Service.
Notification Service checks Redis: sender online → WebSocket push, offline → Cassandra write → sync.
DLQ = business logic failure, not infrastructure bug. Developer alert not needed here.

GAP 4 — Client ACK mechanism.
Original: no ACK in design.
Fixed: client sends ACK via open WebSocket after receiving message.
Status flow: SENT → DELIVERED (after client ACK) → READ (after markAsRead).
markAsRead API already in design → can be reused for READ status.

GAP 5 — Sequence number.
Original: no gap detection mechanism.
Fixed: Message Service generates seq_id per chat using Redis INCR (atomic counter).
seq_id travels with message through Kafka → Delivery Service → client.
Client detects gap (e.g., received 1,2,4 but not 3) → requests sync for missing seq.

GAP 6 — Thundering herd.
Original: not addressed.
Fixed: exponential backoff + jitter on client reconnect → requests spread over time.
Redis cache layer absorbs sync storm: last 20-30 messages already in Redis → cache hit → Cassandra not hit.
Cache-aside pattern reduces Cassandra load dramatically on mass reconnect.

GAP 7 — Atomicity between Cassandra and Kafka.
Original: Message Service writes to Cassandra then publishes to Kafka — not atomic.
Crash between the two → message persisted but never delivered.
Fix: invert the flow. Message Service writes to Kafka only (acks=all → durable).
Two consumer groups: Persistence Consumer → Cassandra, Delivery Consumer → Delivery Service.
If Kafka write fails → Message Service returns FAILED → client retries with same clientMessageId → idempotent.
No Outbox Pattern or distributed transaction needed.

GAP 8 — markAsRead pipeline to sender.
Original: Read Receipts table updated but sender notification mechanism missing.
Fixed: markAsRead → Message Service updates Read Receipts → publishes read_receipt event to Kafka
→ Delivery Service reads event → looks up sender wsServerId in Redis → HTTP POST to sender's WS Server
→ sender's client receives push → shows ✓✓.
Same delivery pipeline reused. WebSocket already open — no separate mechanism needed.
markAsRead triggered by client when user scrolls to the bottom of chat, not on every scroll.

Additional topics discussed:
Hot partition in Cassandra for group chats.
Fix: composite partition key → PRIMARY KEY ((chat_id, bucket), message_id DESC).
bucket = week_of_year(timestamp). At most 2 active buckets with 2-week retention.

Device table for max 10 device enforcement.
MySQL table, count checked on registerDevice call.
Low frequency → no pressure on MySQL.
Server-side enforcement required — cannot trust client.

Pub/Sub vs HTTP POST for WS Server routing.
Pub/Sub → fire and forget, message lost if WS Server down.
HTTP POST → failure visible, retry possible, DLQ applicable.
HTTP POST preferred. Presence lookup (wsServerId from Redis) enables direct routing.

Loose coupling discussion — event-driven sync.
Considered: WS Server publishes "user_connected" event → Sync Service consumes.
Trade-off: extra latency + complexity, minimal gain.
Decision: direct HTTP call on connect is simpler and faster.

## How I feel

Two separate study sessions today. Morning covered Spring Boot completion.
Afternoon and evening went deep on Codility design defense.
Many gaps surfaced that were not obvious before — especially atomicity, sequence numbers, and markAsRead pipeline.
The defense format worked well — reasoning through each gap under interview pressure.
WS Server restart scenario still not covered — carry to tomorrow.

## Next

- WS Server restart scenario — GAP 9
- LINE tech specifics [14]
- Behavioral questions [13]
- Java concurrency coding practice [9.1]
- Final weak spots review