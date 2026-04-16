# [3.2] Redis

## What is Redis?
In-memory data store — much faster than DB.
Data lives in RAM, not disk → 10-100x faster than SQL queries.

Real usage at Rakuten:
→ Session storage (spring.session.redis)
→ Cache for frequently accessed, rarely changing data

---

## Cache-Aside Pattern (most common)

Request comes in:
1. Check Redis first
2. Found (cache hit) → return immediately, no DB call
3. Not found (cache miss) → query DB → save to Redis → return

Code:
String result = redis.get("shops:1234567");
if (result == null) {
result = db.query(...);
redis.set("shops:1234567", result, TTL_30_MIN);
}
return result;

---

## Cache Invalidation — The Hard Problem

Two strategies:

TTL (Time To Live):
→ Set expiry when caching: redis.set(key, value, 30 minutes)
→ After 30 min → auto deleted → next request goes to DB
→ Simple, works well for rarely changing data

Event-based invalidation:
→ When DB changes → immediately delete cache key
→ Next request → cache miss → DB → fresh data
→ More complex but more accurate

"Cache invalidation is one of the hardest problems in CS."
You can never be 100% sure cache matches DB — it's a trade-off.

The question I ask myself:
"How often does this data change?"
+ Rarely (shop list, config) → TTL is fine, 30-60 min
+ Frequently (stock, price) → short TTL or no cache

## Rate Limiting with Redis

Problem:
Someone sends 10,000 requests per second → DDoS or bot
Without limiting → server crashes

Solution — Redis counter:
Every request:
INCR user:123:count     → increment counter
EXPIRE user:123:count 60 → reset after 60 seconds

if count > 100:
return 429 Too Many Requests

Token Bucket pattern:
→ Each user has a bucket with max N tokens
→ Each request costs 1 token
→ Tokens refill at fixed rate (e.g. 10/second)
→ Bucket empty → request rejected

Why Redis?
→ In-memory → microsecond response
→ INCR is atomic → no race condition
→ Works across multiple servers (distributed)

Real world:
→ Payment APIs: max 10 transactions/minute per user
→ Login: max 5 failed attempts → lock account
→ Search: max 100 requests/minute per IP

## Why Redis for Rate Limiting — Atomic Operations

Race condition without atomicity:
Two servers read count=99 simultaneously
Both write 100 → only 1 request counted instead of 2

Redis INCR is atomic:
Single operation, no race condition possible
Works correctly across multiple servers

Fixed Window vs Sliding Window:
Fixed:   count resets every 60 seconds — boundary exploit possible
Sliding: "last 60 seconds" always — more accurate, more fair

Sliding window in Redis:
Store timestamp of each request in a sorted set
Count entries in last 60 seconds
Remove old entries
→ Always accurate, no boundary issues

## Distributed Locking — Redlock

Problem:
Multiple servers accessing same resource simultaneously.
DB-level locking works within one DB connection.
Across multiple servers → need distributed lock.

Basic Redis Lock:
SET lock:account:123 "server1" NX EX 30
→ NX: only set if not exists (atomic)
→ EX: auto-expire after 30 seconds (safety net if server crashes)

Flow:
Server 1 → acquires lock → does work → releases lock
Server 2 → tries to lock → already exists → wait or fail fast

Why single Redis isn't enough:
Redis crashes → lock disappears → two servers think they own the lock

Redlock — 5 Redis nodes:
Need majority (3/5) to acquire lock
1-2 nodes can fail → still works
3+ nodes fail → cannot acquire lock → safe failure

When to use:
+ Payment processing — one transaction at a time per account
+ Inventory — prevent overselling
+ Scheduled jobs — prevent duplicate execution

When NOT to use:
- High frequency operations — too slow
- Use DB optimistic locking instead for simple cases

The question I ask myself:
"Can two servers do this same operation simultaneously and cause damage?"
+ Yes → Distributed Lock
+ No → optimistic locking or no lock needed
