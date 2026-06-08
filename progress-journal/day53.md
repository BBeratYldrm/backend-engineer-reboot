# Day 53 — June 8, 2026

## What I learned

### Concurrent Implementations — Review and Refinement

- TaskManagerV2 rewritten from scratch in under 10 minutes — muscle memory confirmed
- Fixed null check bug: poll(500ms) returns null on timeout, must check before task.run()
- start() changed from private to public — SRP, constructor only initializes
- Two separate loops in shutdown() — interrupt all first, then join all — parallel shutdown
- Added named threads: "Worker-" + i — easier debugging in production

- LRUCache reviewed — LinkedHashMap accessOrder=true, removeEldestEntry auto-eviction
- LRUCacheThreadSafe implemented — ReentrantReadWriteLock
- Critical insight: accessOrder=true makes get() a structural modification — writeLock required for both get() and put(), not readLock

- Producer-Consumer implemented — Producer, Consumer, Main classes
- Shared BlockingQueue reference — same object, two classes both hold the reference
- Output intentionally out of order — two consumers run concurrently, expected behavior
- Same principle as Kafka: ordering guaranteed within partition, not across consumers

### Mock Interview — Round 1 Full Run

- Ran complete Round 1 mock: given proto file + SQL schema
- Self-introduction delivered naturally in English
- Identified stream keyword in proto immediately — gRPC streaming, no WebSocket needed
- Identified client_message_id as idempotency key immediately
- Explained full system flow: Subscribe → API Gateway (L7/Envoy) → Token Bucket → Message Service → Redis presence → Kafka → Delivery Service → gRPC push
- Hot partition → Decaton proposed correctly
- Outbox + Debezium CDC explained with trade-offs
- CAP theorem AP selection justified
- Snowflake ID three components explained
- Redis presence value = grpcServerId — caught own mistake, self-corrected

### Trade-off Drill

- Cassandra vs HBase: masterless simple ops vs strong consistency complex ops
- at-least-once + idempotency vs exactly-once: same result, fraction of the cost
- Token Bucket vs Leaky Bucket: burst allowed vs strict uniform rate
- Outbox + Debezium vs polling: zero memory overhead vs constant DB load
- L4 vs L7 for gRPC: L7 required, HTTP/2 framing must be understood
- Fan-out on write vs read: small groups write, large groups read

### Interview Tactics Learned

- Say one layer, stop, wait — don't dump everything at once
- Natural drop-in for advanced topics: "X works well, though at this scale Y can be a concern — Z handles that if needed"
- Collaborative mindset — treat it as a discussion, not a one-way exam
- Auth question if asked: "JWT token in gRPC metadata header, API Gateway validates"

## How I feel
Nervous. Tomorrow is the real thing.
But I know the system. I know the trade-offs. I know the tools.
The preparation is done. Now it's time to trust it.
