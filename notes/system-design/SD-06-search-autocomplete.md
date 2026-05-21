# [SD-06] System Design — Search Autocomplete

## Keywords
search-autocomplete · trie · redis-sorted-set · bloom-filter · debounce ·
data-collection · query-service · kafka · batch-job · prefix-search ·
frequency-based · read-heavy · cache-aside

---

## Step 1 — Clarify Requirements

Questions to ask:
→ How many daily active users?
→ How many search queries per day?
→ How many suggestions to show?
→ Personalized or same for everyone?
→ Real-time trending or delay acceptable?

Assumptions:
→ 10 million daily search queries
→ 5 suggestions per query
→ Not personalized — same for all users
→ Delay acceptable — weekly/daily refresh is fine
→ Read-heavy system — searches >> new content added

---

## Step 2 — Capacity Estimation

QPS: 10M / 86400 = ~115 QPS
Peak: ~300-350 QPS

Read-heavy → cache is critical
Delay acceptable → no real-time updates needed

---

## Step 3 — High-Level Design

Two core services:

**Query Service** — handles user queries, returns suggestions
**Data Collection Service** — tracks what users search, updates frequencies

---

## Step 4 — Key Design Decisions

### Query Service Flow
User types "jav"
↓
Query Service
↓
[1] Bloom Filter check — is this prefix in our system at all?
→ "definitely not" → return empty list immediately
→ "might exist" → continue
↓
[2] Redis check
→ HIT → return top 5 suggestions
→ MISS → query Trie → write to Redis → return top 5

Bloom Filter lives inside Query Service — it is not a separate service.
It is an in-memory bit array loaded at startup.
Filters out nonsense prefixes (e.g. "xzqwerty") before hitting Redis.
False positives possible — that is acceptable.
False negatives never happen — if Bloom Filter says "no", it is definitely not there.

### Debounce — Client Side

Every keystroke should not trigger a request.
Wait 300ms after user stops typing, then send request.
Reduces unnecessary load significantly.

### Redis — Sorted Set
Key:   "java"
Score: search frequency
Value: full search term
ZREVRANGE java 0 4 → top 5 suggestions by frequency

Fast O(log n) lookup, perfect for leaderboard-style ranking.

### Trie — Prefix Tree

Each node is a letter. Root to leaf = a word.
Optimized for prefix search — only traverse relevant branch.
root → j → a → v → a (★ "java", freq: 500000)
→ a → s → c → r → i → p → t (★ "javascript", freq: 300000)
→ r (★ "jar", freq: 50000)

User types "jav" → navigate to "v" node → collect all children → sort by frequency → top 5

Time complexity: O(p) where p = prefix length

Trade-off:
+ Fast prefix search
- High memory usage — each letter is a separate node
  → Do not query Trie on every request. Build Trie offline, cache results in Redis.

### Data Collection Service Flow
User completes a search
↓
Event published to Kafka
↓
Data Aggregator (Kafka consumer)
↓
DB — update frequency for search term
↓
Weekly/daily batch job
→ Rebuild Trie from DB
→ Repopulate Redis from Trie

Why Kafka and not direct DB write?
- 115 QPS average, peaks at 300+ — direct DB writes would overwhelm the DB
- Kafka buffers events, batch processed → DB load stays low
- Decouples Query Service from Data Collection — independent scaling

### DB Schema
search_term     | frequency
"java"          | 500000
"java tutorial" | 100000
"javascript"    | 300000

---

## Step 5 — Full Architecture
Client (browser/app)
↓ debounce 300ms
API Gateway (rate limiting)
↓
Query Service
[1] Bloom Filter — nonsense prefix? → empty list
[2] Redis Sorted Set — HIT → return
[3] Trie — MISS → compute → cache in Redis → return
--- background ---
User searches → Kafka event → Data Aggregator → DB
Weekly batch → Trie rebuild → Redis repopulated

---

## Bloom Filter — SOV Injection

Bloom Filter = probabilistic data structure for membership testing.
Space: O(1) — tiny bit array regardless of dataset size.
"Definitely not" is always correct.
"Might exist" may be a false positive — acceptable here.

Use case here:
- Load all known prefixes into Bloom Filter at startup
- Every query → Bloom Filter first
- Nonsense prefixes rejected immediately, Redis never touched

---

## Trade-offs

Redis Sorted Set vs Trie (direct):
+ Redis: fast, scalable, no complex implementation
- Redis: needs periodic refresh from Trie/DB
+ Trie: prefix-optimized, real-time
- Trie: high memory, complex to scale

Real-time vs Batch:
+ Batch: simpler, less load, sufficient for most cases
- Batch: trending topics may take hours to appear

Bloom Filter:
+ Eliminates Redis calls for invalid prefixes
- Small false positive rate — acceptable trade-off

---

## Interview Checklist
→ Two services? → Query Service + Data Collection Service
→ Why Redis Sorted Set? → frequency-based ranking, fast lookup
→ What is Trie? → prefix tree, each node a letter, O(p) search
→ Why not query Trie directly? → high memory, cache in Redis instead
→ Bloom Filter? → inside Query Service, filters nonsense prefixes, O(1)
→ Why Kafka? → buffer high-volume search events, decouple services
→ Why debounce? → reduce unnecessary requests from client
→ Real-time? → not needed, weekly batch refresh sufficient
→ Capacity? → 115 QPS average, 300-350 peak, read-heavy