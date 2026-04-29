# [3.2] Redis

## What Is Redis

Separate process, separate server — like MySQL but in-memory.
Connects over TCP, default port 6379.
Data lives in RAM → microsecond access vs millisecond disk access.

RAM access:  ~100 nanoseconds
Disk access: ~10 milliseconds
Redis is ~100x faster than disk-based storage.

On startup: Redis allocates RAM from OS, listens on TCP 6379.
All reads and writes happen in RAM — no disk involved.
Restart → RAM cleared → data gone (unless persistence configured with RDB/AOF).

Kubernetes: Redis runs as a separate pod.
Application pods connect to Redis pod over internal network.

Real world usage:
Twitter    → timeline cache, session management, rate limiting
YouTube    → video view counters, recommendation cache
GitHub     → API rate limiting (X-RateLimit-Remaining header comes from Redis)
Stripe     → idempotency keys, rate limiting
Rakuten    → session management, product cache, rate limiting

Data structures — each solves a different problem:
String     → cache, counter, session, distributed lock
Hash       → object storage (user profile fields)
List       → queue, feed, recent items
Set        → unique members (online users, tags)
Sorted Set → leaderboard, sliding window rate limiting
Stream     → persistent event log, consumer groups

## Cache-Aside (Lazy Loading)

Most common cache pattern. Application manages the cache manually.

Flow:
Request arrives
│
▼
Check Redis (GET shops:zipCode)
│
├── HIT → return from Redis ✅ (fast path)
│
└── MISS
│
▼
Check lock (GET lock:shops:zipCode)
│
├── LOCK EXISTS → sleep 50ms → retry from top (polling)
│
└── NO LOCK
│
▼
Set lock (SETNX lock:shops:zipCode, expire 5s)
│
▼
Query DB
│
▼
Write to Redis (SET shops:zipCode, TTL 1 hour)
│
▼
Delete lock (DEL lock:shops:zipCode)
│
▼
Return result ✅

Why lock expire (5s)?
If the process crashes after setting the lock → lock stays forever →
all requests see "lock exists" → poll forever → system frozen.
Expire ensures: worst case 5 seconds locked, then auto-released.

Why polling (sleep + retry)?
Waiting requests are not notified when lock is released.
They wake up, check cache, return if data is there.
If not → check lock → poll again.
Simple and sufficient for most systems.
More sophisticated: Redis Pub/Sub notify waiters when cache is written.

Trade-offs:
+ Only requested data is cached → memory efficient
+ DB can be down, cache still serves
+ Simple to implement
- First request always hits DB (cold start)
- Thundering herd: cache expires → many requests hit DB simultaneously
  → solved with distributed lock as shown above
- Stale data window between DB update and cache invalidation

Thundering herd without lock:
Cache expires → 500 requests check cache → all see MISS →
all go to DB simultaneously → DB overwhelmed → latency spikes

## TTL and Cache Invalidation

TTL — passive expiry:
redis.setex("shops:" + zipCode, 3600, data); // expires in 1 hour

Data becomes stale after TTL → next request fetches fresh data from DB.
Simple but data can be stale up to TTL duration.

Active invalidation — delete on change:
public void updateShop(Shop shop) {
shopRepository.save(shop);
redis.delete("shops:" + shop.getZipCode()); // cache deleted immediately
}
Next request → cache miss → fetches fresh data from DB.

Use both together:
Active invalidation → primary mechanism, instant consistency
TTL → safety net, handles cases where invalidation was missed

"Cache invalidation is one of the two hard problems in computer science."
Getting it right matters — stale cache causes hard-to-debug production issues.

## Write-Through

Every write goes to both DB and cache simultaneously.

Flow:
Write request → DB write → Cache write → return ✅

Trade-offs:
+ Cache always up to date — no stale data
+ Reads always served from cache — fast
- Every write hits two places → higher write latency
- Data that is never read still goes into cache → memory waste

When to use: read-heavy systems where cache freshness is critical.
Real world: leaderboards, product catalogs, config data.

## Write-Behind (Write-Back)

Write to cache first, return to user, write to DB asynchronously.

Flow:
Write request → Cache write → return ✅ (fast)
└── background → DB write

Trade-offs:
+ Fastest write path — user does not wait for DB
- Cache crash before DB write → data lost permanently
- Data loss risk → never use for financial, reservation, critical data
- Complex to implement reliably

When to use: data loss is tolerable.
Examples: social media like counts, YouTube view counters, analytics events.

## Strategy Comparison

Cache-Aside:   read optimized, write goes to DB then cache on next read
Write-Through: both directions simultaneously → consistent but slower writes
Write-Behind:  cache first, DB async → fastest writes, data loss risk

## Rate Limiting

Two layers — always use both together:

Layer 1 — Infrastructure (Nginx / API Gateway):
→ IP-based, coarse filter, bot protection
→ Config file, no application code
→ Rakuten: Apache/Nginx config, IP blocking, path-based rules

Layer 2 — Application (Redis):
→ User-based, plan-based, granular
→ Free: 100 req/min, Premium: 1000 req/min, Admin: unlimited
→ GitHub X-RateLimit-Remaining header comes from Redis

Redis counter approach:
public boolean isAllowed(String userId) {
String key = "rate:" + userId;
long current = redis.incr(key);
if (current == 1) {
redis.expire(key, 60); // start 60-second window on first request
}
return current <= 100;
}

Problem: incr and expire are two separate commands.
Redis crashes between them → key never expires → user permanently blocked.

Fix — Lua script (atomic):
local current = redis.call('incr', KEYS[1])
if current == 1 then
redis.call('expire', KEYS[1], ARGV[1])
end
return current

Lua script runs atomically — either both commands execute or neither.
No partial state possible.

Fixed Window problem:
Window: 00:00 → 01:00 (60 seconds), limit: 100 requests

User sends 100 requests at 00:59 → limit reached
Window resets at 01:00 → user sends 100 more requests
Result: 200 requests passed in 2 seconds — system allowed it

Window reset creates a burst opportunity.

Sliding Window solution:
Window continuously moves. "How many requests in the last 60 seconds?"
Checked at every moment — no reset burst possible.

Redis Sorted Set implementation:
// Each request → add timestamp to Sorted Set
redis.zadd("rate:" + userId, now, UUID.randomUUID().toString());
// Remove entries older than 60 seconds
redis.zremrangeByScore("rate:" + userId, 0, now - 60000);
// Remaining count = requests in last 60 seconds
long count = redis.zcard("rate:" + userId);
return count <= 100;

Fixed Window vs Sliding Window:
Fixed Window:   simple, memory efficient, burst vulnerability at window reset
Sliding Window: no burst, fairer, higher memory usage per user

Most systems: fixed window is sufficient.
API gateways, critical rate limiting: sliding window — GitHub, Stripe use it.

Token Bucket vs Leaky Bucket:
Token Bucket → burst allowed, accumulate tokens, spend all at once
user-friendly, allows short bursts
Leaky Bucket → fixed rate, burst absorbed, processed at constant speed
backend protection, predictable load

Trade-offs:
Token Bucket: better UX, burst possible → backend sees spikes
Leaky Bucket: smooth backend load → user experience less flexible

## Distributed Lock

Prevent multiple application instances from executing the same operation simultaneously.

Problem:
3 Kubernetes pods running the same application.
Scheduled job fires at 09:00 — daily report generation.
All 3 pods trigger at the same time → 3 duplicate reports sent.

Solution — Redis distributed lock:
String lockKey = "lock:daily-report";
String lockValue = UUID.randomUUID().toString(); // unique per instance

boolean locked = redis.set(lockKey, lockValue,
SetParams.setParams().nx().ex(30)); // NX: only if not exists, EX: expire 30s

if (locked) {
try {
generateDailyReport(); // only one instance runs this
} finally {
if (lockValue.equals(redis.get(lockKey))) {
redis.delete(lockKey); // only delete own lock
}
}
}

Why unique value?
Instance A locks, job takes long, lock expires (30s passed).
Instance B locks.
Instance A finishes, tries to delete lock — but it is B's lock now.
Unique value check → A cannot delete B's lock. Safe.

Why NX (set if not exists)?
All 3 instances try to set the key simultaneously.
NX is atomic — only one succeeds. Others get null back → skip the job.

When to use distributed lock:
→ Scheduled job that must run on exactly one instance
→ Inventory reservation — prevent overselling
→ Invoice generation — must not duplicate
→ Cache population — thundering herd protection

Redlock — multi-node distributed lock:
Single Redis node fails → lock lost → two instances enter critical section.
Redlock: write to 5 Redis nodes, lock acquired if 3+ accept (quorum).

Flow:
Instance A → tries to lock on node 1, 2, 3, 4, 5
→ 3 accept → lock acquired
→ job runs

Instance B → tries same
→ only 2 accept (3 already locked) → lock not acquired
→ skips job

Trade-offs:
+ More reliable than single node
- Still has edge cases: network partition, clock skew between nodes
- Martin Kleppmann vs Redis creator (Antirez) — famous debate in distributed systems
- For truly critical systems: ZooKeeper or etcd more reliable

Real world:
Uber → distributed lock for driver assignment — one driver, one ride
Stripe → idempotency keys prevent duplicate payment processing

## Pub/Sub

Publisher sends message to channel, subscribers receive it. Fire and forget.

// Publisher:
redis.publish("reservations", serialize(reservation));

// Subscriber:
redis.subscribe(new JedisPubSub() {
public void onMessage(String channel, String message) {
processReservation(deserialize(message));
}
}, "reservations");

Trade-offs:
+ Simple, fast, real-time
- Subscriber offline → message lost, no persistence
- No delivery guarantee
- No replay

Redis Pub/Sub vs Kafka:
Pub/Sub → real-time notifications, online users, low stakes, cache invalidation signals
Kafka   → event sourcing, guaranteed delivery, replay needed, audit log

## Redis Streams

Persistent Pub/Sub with consumer groups — Redis's answer to Kafka for lighter workloads.

// Produce:
redis.xadd("reservations", "*", "shopId", "123", "userId", "456");

// Consume with consumer group:
List<MapRecord> messages = redis.xreadgroup(
GroupName.of("notification-group"),
Consumer.from("notification-group", "consumer-1"),
StreamReadOptions.empty().count(10),
StreamOffset.create("reservations", ReadOffset.lastConsumed())
);

vs Pub/Sub: messages persisted, consumer groups, ACK mechanism.
vs Kafka:
Redis Streams → in-memory, faster, costlier storage, simpler setup
Kafka         → disk-based, cheaper at scale, replay TB of data, battle-tested

## Interview Checklist
→ What is Redis? → in-memory data store, separate process, TCP 6379, RAM-based
→ Cache-Aside flow? → check cache → miss → lock → DB → write cache → delete lock
→ Thundering herd? → cache expires, many requests hit DB simultaneously
→ Fix for thundering herd? → distributed lock, only one goes to DB, others poll
→ TTL vs active invalidation? → passive expiry vs delete on change, use both
→ Write-Through vs Write-Behind? → both sync vs cache first async DB, data loss risk
→ Why Lua script for rate limiting? → atomicity, two commands as one operation
→ Fixed vs sliding window? → burst at reset vs continuous window, sliding fairer
→ Two layers of rate limiting? → Nginx coarse filter + Redis granular per user
→ Distributed lock unique value? → prevent instance from deleting another instance's lock
→ Why NX is atomic? → only one instance wins even with simultaneous attempts
→ Redlock? → quorum across 5 nodes, more reliable but edge cases remain
→ Redis Pub/Sub vs Kafka? → fire and forget vs guaranteed delivery and replay
→ Redis Streams vs Kafka? → in-memory fast vs disk-based scalable