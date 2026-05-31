# Day 45 — May 31, 2026

## What I studied

### Redis — [6.1] to [6.4]

[6.1] Data structures for chat.
Presence: String + TTL, value = wsServerId (not just "online").
→ delivery service does O(1) lookup, no broadcast needed.
Unread count: INCR/DECR — badge counter only.
→ last_read_message_id stored in HBase, not Redis.
Offline queue: Sorted Set, score = SnowFlake ID.
→ reconnect → ZRANGE → ordered delivery → ZREM.
User profile: Hash — partial field read/write, no full JSON needed.
Recent messages: cache-aside for reads + write-through for writes.
→ Redis is performance layer, HBase is source of truth.

[6.2] Pub/Sub vs Streams.
Pub/Sub: fire and forget, no persistence.
→ cluster mode: 1 publish → broadcasts to ALL shards → bandwidth explosion at scale.
Streams: persistent, replayable, consumer groups, shards like a regular key.
→ no broadcast problem → preferred for inter-server delivery.

[6.3] Redis Cluster.
Sharding → scale, data distributed across nodes.
Replica → availability, same data copied.
16384 hash slots → CRC16(key) % 16384 → determines node.
Node crash: presence loss acceptable (heartbeat restores), message history not acceptable → HBase.

[6.4] Cache patterns.
cache-aside: check Redis → hit → return, miss → HBase → write Redis + TTL → return.
write-through: new message → HBase + Redis simultaneously.
TTL prevents stale data. Inactive chat → TTL expires → cache miss on reopen → acceptable.
RDB → snapshots, fast restart, some loss possible. AOF → every write logged, more durable.
→ presence: RDB sufficient. message queues: AOF or hybrid.

### HBase — [7]

[7.1] Why HBase.
MySQL → single server, bottleneck under write-heavy workload.
HBase → distributed, horizontal scale, designed for time-series append-heavy data.
+ strong consistency, efficient range scan on sorted row keys.
- high operational complexity (HMaster, ZooKeeper, HDFS dependency).
- multi-DC replication is hard.

[7.2] Row key design.
chat_id alone → not unique. chat_id + timestamp → unique but two problems.
Problem 1: alphabetical sort → oldest first → must scan entire list for recent messages.
Fix: reversed timestamp = Long.MAX_VALUE - timestamp → newest message appears first.
Problem 2: sequential IDs → all land on same Region → hotspot.
Fix: salt prefix = hash(chat_id) % N.
Final row key: salt_chatId_reversedTimestamp.
+ hotspot avoided, recent messages first, same chat co-located.

[7.3] Write path.
Client → WAL (disk, sequential, durability) → MemStore (memory, fast) → HFile (disk, permanent).
Same pattern as Kafka and Cassandra. Sequential write → low latency cost.

[7.4] Read path.
MemStore → HFile → merge → return.
Bloom filter skips irrelevant HFiles → avoids unnecessary disk reads.

[7.5] Compaction.
Minor: merges small HFiles, fast, background.
Major: merges all HFiles, removes tombstones, slow → schedule off-peak.

[7.6] HBase vs Cassandra.
HBase: strong consistency, efficient range scans, complex ops, hard multi-DC.
Cassandra: eventual consistency, masterless, simple ops, multi-DC built-in.
→ both valid for messaging. trade-off depends on consistency requirements and ops capacity.

### Kubernetes — [11]

[11.1] Core concepts.
Pod → smallest unit, 1 container, runs Docker image, ephemeral.
Deployment → manages pods, guarantees replica count, rolling update + rollback.
Service → stable DNS endpoint, load balances across pods, pod IPs change freely.
Ingress → external traffic into cluster, routing rules.
Egress → cluster to outside world.
Inter-pod: communicate via Service name → K8s DNS resolves → correct pod.
→ no code change needed when pods restart or scale.

[11.2] Probes.
Liveness: "is pod alive?" → fail → restart.
Readiness: "is pod ready for traffic?" → fail → removed from Service, not restarted.
Spring Boot Actuator provides /actuator/health endpoints automatically.

[11.3] HPA.
Auto-scales pods based on metric.
CPU → wrong for Kafka consumers.
Consumer lag → correct: high lag = consumers can't keep up → add pods.
Max pods = partition count → 1 partition = 1 consumer rule.

[11.4] StatefulSet vs Deployment.
Deployment → stateless services, pods interchangeable.
StatefulSet → stateful services (Kafka brokers), unique stable identity + persistent storage.

[11.5] Rolling update.
New pod starts → readiness probe passes → SIGTERM to old pod → preStop hook → terminate.
preStop: commit Kafka offsets, drain connections, graceful shutdown.

## How I feel

Long day at a café. Redis, HBase, and Kubernetes all covered for the first time in depth.
HBase row key design clicked well — salt + reversed timestamp is an elegant pattern.
Kubernetes became clear once I understood that WebSocket server is just a Spring Boot app running in a pod.
The full architecture diagram helped connect all layers visually.
A lot of new material — needs repetition to activate properly.

## Next

- Distributed Systems [12] — CAP, consistency models
- Codility design defense [10.2]
- LINE tech specifics [14]
- Behavioral questions [13]
- Repeat pass: Kafka [5.3] rebalancing, [5.6] Decaton, Redis Streams