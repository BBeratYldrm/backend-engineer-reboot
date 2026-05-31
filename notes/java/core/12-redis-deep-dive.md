## [6.1] Redis Data Structures for Chat

Keywords
redis-streams · sorted-set · pub-sub · presence · ttl · heartbeat · server-id ·
unread-count · offline-queue · cache-aside · write-through · rdb · aof

---

Presence tracking:
key   → presence:{userId}:{deviceId}
value → {wsServerId}
TTL   → 30s, heartbeat refreshes every 10s
expire → device automatically goes offline

+ O(1) lookup
+ delivery service routes directly to correct WebSocket server
- node crash → presence lost → acceptable, heartbeat restores it (eventual consistency)

Unread count:
INCR unread:{userId}:{chatId}    → new message arrives
SET  unread:{userId}:{chatId} 0  → markAsRead
Only a badge counter. last_read_message_id → stored in HBase, not Redis.

Offline queue:
key   → offline_queue:{userId}
value → message_id
score → SnowFlake ID (timestamp embedded, sortable)
On reconnect → ZRANGE → ordered delivery → ZREM

User profile cache:
HSET user:{id} name "..."
HSET user:{id} avatar "..."
→ partial read/write per field, no full JSON serialize needed

Recent messages cache:
cache-aside for reads  → Redis miss → HBase → write to Redis + TTL
write-through for writes → new message → HBase + Redis simultaneously
→ reduces HBase load for active conversations, lower read latency

Redis is a performance layer. HBase is the source of truth.

---

## [6.2] Pub/Sub vs Streams

Pub/Sub:
+ low latency
- fire and forget, no persistence
- cluster mode: 1 publish → broadcast to ALL shards → bandwidth explosion at scale

Streams:
+ persistent, replayable
+ consumer group support
+ shards like a regular key → no bandwidth explosion
  → preferred for messaging at scale

---

## [6.3] Redis Cluster

Sharding  → scale, data distributed across nodes
Replica   → availability, same data copied
16384 hash slots → CRC16(key) % 16384 → determines node

Node crash:
→ presence loss acceptable (heartbeat restores)
→ message history loss not acceptable → stored in HBase

---

## [6.4] Cache Patterns

cache-aside (reads):
1. check Redis
2. hit → return
3. miss → fetch from HBase → write to Redis + TTL → return

write-through (writes):
new message → write to HBase + write to Redis simultaneously

Combined: write-through for writes, cache-aside for reads.

TTL prevents stale data. Inactive chat → TTL expires → cache miss on reopen → acceptable.

RDB → periodic snapshot, fast restart, possible data loss between snapshots
AOF → logs every write, more durable, larger files
Presence → RDB sufficient. Message queues → AOF or hybrid.