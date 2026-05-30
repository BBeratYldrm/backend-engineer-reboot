# Day 44 — May 30, 2026

## What I studied

### Kafka Deep Dive — [5.1] to [5.6]

Spent the full day on Kafka. This was the heaviest topic on the roadmap and needed dedicated time.

[5.1] Core concepts review — partition, offset, consumer group, ordering guarantee.
Confirmed: partition key routes same chat to same partition → ordering preserved.
Max parallelism = partition count. More consumers than partitions = idle consumers.

[5.2] Delivery semantics and durability.
At-most-once → loss tolerable, analytics/logs only.
At-least-once → loss never acceptable, duplicates handled via idempotency → LINE's choice.
Exactly-once → idempotent producer + transactional API → financial operations only.
Durability trio: acks=all + replication.factor=3 + min.insync.replicas=2.
Idempotent producer: producer ID + sequence number → broker rejects duplicates.

[5.3] Rebalancing — completely new material.
Triggers: consumer join/leave/crash, session timeout.
Eager rebalancing = stop-the-world → all consumers pause → unacceptable at scale.
Cooperative sticky rebalancing = only moving partitions reassigned → system keeps reading.
3x rule: session.timeout.ms = 3 × heartbeat.interval.ms — same pattern as Redis TTL.
Static membership: group.instance.id → K8s pod restarts without triggering rebalance.
Debug scenario: rebalance every 3 minutes → check max.poll.interval.ms and processing time.

[5.4] Advanced topics.
Back-pressure: pull-based model = natural back-pressure, consumer controls pace.
Log compaction: per-key retention → use for presence state, last read receipt, not message history.
Producer tuning: linger.ms=0 for messaging (latency priority), lz4 compression.
Unclean leader election: always false for messaging — durability over availability.
Kafka Streams vs raw Consumer API: raw API for simple delivery pipeline, Streams for analytics/aggregation.
Hot partition: salting breaks ordering → unacceptable → Decaton is the real solution.
Retry topics + DLQ: retry chain with backoff → DLQ → alert → manual fix → replay.

[5.5] Outbox pattern review.
DB + Kafka cannot be atomic → outbox table in same transaction → separate sender to Kafka.
Polling (simple, overhead) vs CDC/Debezium (real-time, complex).

[5.6] Decaton — LINE-specific library.
Problem: single partition → single consumer rule → hot partition cannot be scaled by adding consumers.
Decaton: concurrent processing within one consumer, ordering preserved via watermark offset commit.
Watermark tracker in memory → advances as leftmost completed message moves forward → commits to __consumer_offsets.
LINE: 1M+ I/O-intensive tasks/sec per stream with Decaton.

## How I feel

Long study day with a break in the afternoon. Kafka was genuinely dense — most of [5.3] and [5.6]
were completely new. Some concepts needed multiple passes (watermark commit, ISR interaction with acks).
Connecting patterns helped a lot: 3x rule appeared in Redis, WebSocket, and now Kafka.
The foundation is there. Needs repetition to activate.

## Next

- [6] Redis — data modeling for chat (Streams vs Sorted Sets vs Pub/Sub)
- [7] HBase — brand new, full day needed
- [5.3] + [5.6] need at least one more pass before interview
- Codility design defense — Redis presence gap (serverId), Cassandra clustering key