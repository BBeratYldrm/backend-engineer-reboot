# [SD-02] System Design — Rate Limiter

## Keywords
rate-limiter · token-bucket · fixed-window · sliding-window · redis ·
lua-script · atomic · 429 · api-gateway · user-based · incr · ttl ·
retry-after · burst · refill · distributed-rate-limiter

---

## Step 1 — Why Rate Limiter

Two purposes:
1. Security — prevent bots, DDoS, brute force attacks
2. System protection — prevent overload from excessive requests

Real scenarios:
→ Bot sending 10,000 messages/second on LINE
→ Developer's buggy code in infinite loop hammering the API
→ Malicious user trying to brute force login

---

## Step 2 — Clarify Requirements

Questions to ask:
→ What is the limit? (requests per second/minute/hour)
→ Who is limited? (user, IP, API key, device)
→ Hard or soft limit? (block or throttle)
→ Distributed system? (multiple servers)
→ What happens when limit exceeded? (429, queue, drop)

For LINE:
→ User-based rate limiting
→ Hard limit — block when exceeded
→ Distributed — multiple servers
→ 429 Too Many Requests when exceeded

---

## Step 3 — Where Does Rate Limiter Live
Client
↓
API Gateway     ← coarse filter (IP-based, DDoS protection)
↓
Rate Limiter    ← fine-grained filter (user-based, plan-based)
↓
Load Balancer
↓
Services

Two-layer approach:
Layer 1 — API Gateway: IP-based, infrastructure level, bot protection
Layer 2 — Redis Rate Limiter: user-based, application level, granular control

---

## Step 4 — Algorithms

### Fixed Window

Time divided into fixed windows (e.g. 60 seconds).
Counter resets at window boundary.

Problem — burst at window reset:
User sends 100 requests at 00:59 → limit reached
Window resets at 01:00 → 100 more requests
→ 200 requests passed in 2 seconds

### Sliding Window

Window continuously moves. "How many requests in last 60 seconds?"
No burst at reset — always looking at last N seconds.

Implementation: Redis Sorted Set
→ Each request stored with timestamp as score
→ Remove entries older than window
→ Count remaining → compare to limit

More memory usage than Fixed Window.
Fairer — no burst vulnerability.

### Token Bucket — Most Common

User has a bucket with tokens.
Each request consumes 1 token.
Tokens refill at constant rate (e.g. 1 token/second).
Bucket has max capacity.

Bucket full → burst allowed (spend all tokens at once)
Bucket empty → 429

LINE example:
Bucket capacity: 50 tokens
Refill rate: 1 token/second

User wakes up → bucket full (50 tokens)
→ Can send 50 messages in burst (normal morning behavior)
→ Then limited to 1 message/second
→ Natural usage pattern respected

Why Token Bucket for LINE:
+ Allows natural burst (morning message flood)
+ Smooth rate over time
+ Bot sending 1000/second → blocked immediately
+ Most widely used in production

---

## Step 5 — Redis Implementation

Key structure:
Key:   "rate:userId:123"
Value: request count (integer)
TTL:   window duration (e.g. 60 seconds)

Flow:
Request arrives
→ INCR "rate:userId:123"
→ If first request (value == 1) → set TTL 60s
→ If count > limit → return 429
→ Else → allow request

Atomicity problem:
INCR runs → value incremented
EXPIRE fails → TTL never set
→ Key lives forever → user permanently blocked

Fix — Lua script (atomic):
local current = redis.call('incr', KEYS[1])
if current == 1 then
redis.call('expire', KEYS[1], ARGV[1])
end
return current

Lua script runs as single atomic operation.
Either both execute or neither. No partial state.

---

## Step 6 — Response When Limited

HTTP 429 Too Many Requests

Headers:
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1620000060
Retry-After: 60

Retry-After tells client when to try again.
Client can implement backoff instead of hammering the server.

---

## Step 7 — Distributed Rate Limiter

Problem:
3 server instances, each with local counter.
User sends 90 requests → each server sees 30 → all allow
→ 90 requests passed, limit was 100 but effectively not enforced properly

Solution: centralized Redis
All servers share same Redis instance.
Counter is global, not per-server.
Server 1 ──┐
Server 2 ──┼──► Redis (single source of truth)
Server 3 ──┘

Redis cluster for high availability:
Single Redis → single point of failure
Redis Cluster → multiple nodes, automatic failover

---

## Step 8 — Full Architecture

Client
↓
API Gateway (IP-based coarse filter)
↓
Rate Limiter Service
→ check Redis: "rate:userId:X"
→ INCR + TTL via Lua script
→ count > limit → 429 + Retry-After header
→ count <= limit → forward request
↓
Load Balancer
↓
Services

---

## Trade-offs

Fixed Window vs Sliding Window:
+ Fixed: simple, less memory
- Fixed: burst at window boundary
+ Sliding: no burst, fairer
- Sliding: more memory (Sorted Set per user)

Token Bucket vs Sliding Window:
+ Token Bucket: burst-friendly, natural usage patterns
+ Token Bucket: simple to implement
- Token Bucket: burst can still overwhelm downstream
+ Sliding Window: strict, no burst possible
- Sliding Window: higher memory usage

Centralized Redis vs Local counter:
+ Centralized: accurate across all servers
- Centralized: Redis becomes bottleneck, single point of failure
+ Local: fast, no network hop
- Local: inaccurate in distributed system

---

## Interview Checklist
→ Why rate limiter? → security + system protection
→ Where in architecture? → after API Gateway, before LB
→ Who to limit? → user-based most granular
→ Three algorithms? → Fixed Window (simple), Sliding Window (fair), Token Bucket (burst-friendly)
→ Why Token Bucket for LINE? → natural burst behavior, most widely used
→ Redis key structure? → "rate:userId:X", INCR, TTL
→ Why Lua script? → atomicity, INCR + EXPIRE as single operation
→ 429 response? → include Retry-After header
→ Distributed problem? → centralized Redis, not local counters