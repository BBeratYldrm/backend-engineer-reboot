# [SD-01] System Design — URL Shortener

## Keywords
url-shortener · base62 · cqrs · sharding · hot-spot · hash-based-sharding ·
redis-cache · 302-redirect · read-heavy · capacity-estimation · api-gateway

---

## Step 1 — Clarify Requirements

Questions to ask interviewer:
→ How many URLs shortened per day?
→ How many redirects per day?
→ How long are shortened URLs valid?
→ Custom short URLs needed?
→ Analytics needed?

Assumptions:
→ 1 million URLs shortened per day (write)
→ 100 million redirects per day (read)
→ Read/write ratio: 100:1 → read-heavy system
→ URLs valid indefinitely
→ Analytics needed → track clicks

---

## Step 2 — Capacity Estimation

Write QPS: 1M / 86400 = ~12 writes/second
Read QPS:  100M / 86400 = ~1160 reads/second

Storage:
Each URL record ~500 bytes
1M URLs/day x 365 days x 10 years = 3.65 billion records
3.65B x 500 bytes = ~1.8 TB

Cache:
80/20 rule — 20% of URLs get 80% of traffic
Cache top 20% → ~20M entries

---

## Step 3 — High-Level Design

Client
↓
API Gateway / Load Balancer
├──► Write Service (URL shortening)
└──► Read Service (redirect)

Write Service:
→ Receives long URL
→ Auto-increment ID from DB
→ Base62 encode → short code (6-7 chars)
→ Save to DB
→ Cache in Redis
→ Return short URL to user

Read Service:
→ Receives short code
→ Check Redis (key: short code, value: original URL)
HIT  → 302 redirect
MISS → Query DB → cache result → 302 redirect

---

## Step 4 — Key Design Decisions

### Base62 Encoding

Characters: 0-9, a-z, A-Z = 62 characters
6 characters → 62^6 = ~56 billion unique URLs
7 characters → 62^7 = ~3.5 trillion unique URLs

Why not UUID?
UUID is 36 characters — too long for a short URL.
Base62 gives short, URL-safe codes.

Why not Base64?
Base64 includes + and / — problematic in URLs.
Base62 is URL-safe.

Encoding flow:
Auto-increment DB ID (e.g. 11157)
→ Base62 encode → "abc12"
→ bit.ly/abc12

### 301 vs 302 Redirect

301 Permanent:
→ Browser caches the redirect
→ Next request goes directly to original URL
→ Bit.ly never sees the request → no analytics

302 Temporary:
→ Browser does not cache
→ Every request goes through Bit.ly
→ Click tracking, analytics, metrics possible
→ Correct choice for URL shorteners

### CQRS — Read/Write Separation

Read traffic (100x) >> Write traffic (1x)
Separate services → scale independently

Write Service → fewer instances, handles DB writes
Read Service  → many instances, handles Redis + DB reads
Failure isolation — one does not affect the other (Bulkhead)

### Redis Cache

Key:   short code ("abc12")
Value: original URL ("https://amazon.com/...")
TTL:   24 hours

Cache-Aside pattern:
Read Service checks Redis first.
Miss → DB → write to Redis → return.

---

## Step 5 — Database and Sharding

Single DB not enough for billions of records.

Range-based sharding — hot spot problem:
Shard 1: ID 1-1M     → no new writes, sitting idle
Shard 2: ID 1M-2M    → no new writes, sitting idle
Shard 3: ID 2M-3M    → ALL new writes → hot spot

Hash-based sharding — solution:
shard = hash(short_code) % number_of_shards
Traffic distributed evenly across all shards.
No hot spot.

---

## Step 6 — Full Architecture

Client
↓
API Gateway (rate limiting, auth)
↓
Load Balancer
├──► Write Service (few instances)
│        ↓
│    DB (hash-sharded MySQL)
│        ↓
│    Redis (write on create)
│
└──► Read Service (many instances)
↓
Redis Cache
├── HIT  → 302 redirect
└── MISS → DB → cache → 302 redirect

---

## Step 7 — Additional Constraints

### Custom URLs

User can request custom short code (e.g. bit.ly/berat-blog).

API parameter: customUrl=true

Flow:
customUrl=true  → use user-provided string as key
→ check DB: already taken?
YES → return 409 Conflict
NO  → save directly, skip Base62

customUrl=false → generate via Base62 (default flow)

Namespace separation to avoid collision:
Base62 generated → starts with digit:  "1abc2"
Custom URL       → starts with letter: "berat-blog"
Structurally impossible to collide — no DB check needed.

Trade-off:
+ No collision between custom and generated URLs
- Slightly reduces Base62 combination space (first char = digit only)
- 9 × 62^5 = ~8 billion combinations — still more than enough

### Security — Sequential ID Problem

Sequential DB IDs → sequential Base62 output → predictable URLs.
User gets bit.ly/2Td, can guess bit.ly/2Te is the next URL.

Solution: hash the ID before Base62 encoding.
ID 11157 → hash → random large number → Base62 → unpredictable output
ID 11158 → hash → completely different number → different output

In interviews: "I pass the ID through a hash function before Base62 encoding
to make outputs unpredictable."

### URL Expiration

URLs expire after N days (e.g. 30 days).

On creation:
→ DB: add expires_at column (created_at + 30 days)
→ Redis: set TTL = 30 days

On redirect:
→ Redis TTL expired → cache miss → goes to DB
→ DB: check expires_at → expired → return 410 Gone
→ 410 Gone vs 404 Not Found:
404 → never existed
410 → existed but permanently gone

Cleanup:
→ Nightly scheduled job (cron) → delete expired records from DB
→ Redis handles itself via TTL

### Snowflake ID — Distributed Unique ID Generation

Problem with auto-increment in sharded systems:
Shard 1: ID 1, 2, 3...
Shard 2: ID 1, 2, 3...  ← collision!
Shard 3: ID 1, 2, 3...  ← collision!

Each shard generates its own sequence → duplicates inevitable.

Snowflake solution (Twitter):
[41 bit timestamp][10 bit machine ID][12 bit sequence number]
= 63 bit → globally unique ID

41 bit timestamp  → milliseconds since epoch → ~69 years range
10 bit machine ID → which server/datacenter generated it
12 bit sequence   → up to 4096 IDs per millisecond per machine

Result:
Shard 1 (machine 001) → timestamp + 001 + seq → unique
Shard 2 (machine 002) → timestamp + 002 + seq → unique
Shard 3 (machine 003) → timestamp + 003 + seq → unique

Never collides — machine ID guarantees uniqueness across shards.

Bonus: Snowflake IDs are sortable by time.
Higher ID = more recent. No extra timestamp column needed.

When to use Snowflake:
+ Multiple DB shards generating IDs simultaneously
+ Very high throughput — millions of IDs per second
+ Distributed systems across datacenters

When auto-increment is enough:
- Single DB, low-medium scale
- No sharding needed
- Simplicity preferred

URL Shortener:
Small scale  → auto-increment + Base62 sufficient
Large scale + sharding → Snowflake + Base62

---

## Trade-offs

Base62 vs UUID:
+ Base62: short, URL-safe, human readable
- Sequential IDs predictable → use hash or random offset if security needed

301 vs 302:
+ 302: analytics possible, metrics trackable
- 302: more load on servers (no browser cache)

CQRS:
+ Independent scaling, fault isolation (Bulkhead)
- More complex architecture, eventual consistency

Hash sharding vs Range sharding:
+ Hash: even distribution, no hot spot
- Hash: range queries harder, resharding complex

---

## Interview Checklist
→ First questions to ask? → scale, read/write ratio, analytics needed?
→ Why Base62? → URL-safe, short, 56B combinations with 6 chars
→ Why 302 not 301? → analytics and click tracking
→ Why CQRS? → 100:1 read/write ratio, scale independently
→ Redis key-value? → key: short code, value: original URL
→ Hot spot problem? → range sharding causes it, hash sharding solves it
→ Capacity estimate? → 12 writes/s, 1160 reads/s, ~1.8TB in 10 years