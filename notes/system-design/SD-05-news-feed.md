# [SD-05] System Design — News Feed

## Keywords
news-feed · fan-out · fan-out-on-write · fan-out-on-read · hybrid-approach ·
hotspot · hotkey · consistent-hashing · virtual-nodes · feed-service ·
post-service · redis · kafka · workers · recommendation-engine ·
collaborative-filtering · content-based-filtering

---

## Step 1 — Clarify Requirements

Questions to ask:
→ Mobile, web, or both?
→ How many friends/followers max? (affects fan-out strategy)
→ Daily active users? (affects scale)
→ Sorted by time or ranking algorithm?
→ Media support — images, videos, text only?
→ Any celebrity accounts? (affects fan-out decision)

Assumptions:
→ 10 million DAU
→ Max 5000 friends per user
→ Reverse chronological order
→ Media supported
→ Celebrity accounts exist (hybrid fan-out needed)

---

## Step 2 — Two Core Flows

### Flow 1 — User Posts

User creates a post → system distributes it to followers' feeds.

### Flow 2 — User Opens Feed

User opens app → system assembles and returns their feed.

---

## Step 3 — Fan-out Strategy

Fan-out = distributing one post to many followers' feeds.

### Fan-out on Write (Push Model)

Post created → immediately update all followers' feed caches.

Trade-offs:
+ Feed ready instantly when user opens app
+ Read is fast — just fetch from cache
- Celebrity with 400M followers → 400M cache updates → system overload
- Wastes resources for inactive users

### Fan-out on Read (Pull Model)

User opens feed → fetch posts from followed users at that moment.

Trade-offs:
+ Write is lightweight — no fan-out on post creation
+ No wasted work for inactive users
- Feed assembly slow — must query many users' posts
- Higher read latency

### Hybrid Approach (Twitter/Instagram)

Best of both worlds:
Normal users (< ~10K followers) → fan-out on write
Celebrity users (> ~10K followers) → fan-out on read

When user opens feed:
→ Fetch pre-built cache (normal users' posts)
→ Fetch celebrity posts on demand
→ Merge and sort by timestamp
→ Return to user

---

## Step 4 — Hotspot / Hotkey Problem

Celebrity posts → massive fan-out → single point overload.
This is the hotkey problem — one entity causing disproportionate load.

Solution 1: Hybrid approach (fan-out on read for celebrities)
Solution 2: Consistent hashing to distribute load evenly

---

## Step 5 — Consistent Hashing

Normal sharding problem:
hash(userId) % 4 → 4 nodes
Add 5th node → hash % 5 → almost all data moves → massive resharding

Consistent hashing solution:
Nodes placed on a ring.
Data mapped to nearest node clockwise.
Adding a node → only neighbor's data moves → minimal resharding.

Virtual nodes:
Each physical node represented multiple times on the ring.
Load distributed more evenly.
Hotkey problem reduced — celebrity data spread across multiple physical nodes.

3-sentence interview version:
"Consistent hashing places nodes on a ring. Data maps to the nearest node,
so adding or removing a node only affects its neighbors — minimal resharding.
Virtual nodes improve load distribution, which also helps mitigate hotkey problems
where a single celebrity causes disproportionate load on one node."

---

## Step 6 — Feed Retrieval Flow

User opens feed:
↓
Feed Service
↓
Redis → fetch feed cache (list of post IDs)
↓
Post Service cache → fetch content for each post ID
↓
If cache miss → DB read (last resort)
↓
Merge + sort → return to user

Redis stores per user:
Key: "feed:userId:123"
Value: [postId1, postId2, postId3...] (sorted by timestamp)
+ like count, basic metadata

Full post content (text, image URL, author info) → Post Service cache

---

## Step 7 — Full Architecture

Post created:
User → Post Service → DB write + Post cache
→ Kafka ("new_post" event)
↓
Fanout Service
↓
Normal user? → update followers' feed cache (Redis)
Celebrity?   → skip, read on demand

Feed opened:
User → Feed Service
↓
Redis (feed cache) + Post Service cache
↓ (celebrity posts)
Fetch on demand from DB/cache
↓
Merge → return

---

## Step 8 — Recommendation Algorithms (Scope Note)

Real systems go beyond reverse chronological order.
Ranking algorithms determine what you see first.

Collaborative Filtering:
"Users similar to you liked these posts."
Based on behavior patterns across users.
Netflix, Spotify use this heavily.

Content-based Filtering:
"You liked posts about X, here are more X posts."
Based on content features of what you engaged with.

Hybrid Recommendation:
Most modern systems combine both.
TikTok "For You", Instagram "Explore" — hybrid recommendation engines.

Interview framing:
"Ranking and recommendation is a separate system sitting on top of the feed.
It involves collaborative filtering, content-based filtering, or hybrid approaches.
This would be a separate design discussion — out of scope for the core feed system."

---

## Trade-offs Summary

Fan-out on write:
+ Fast reads, cache ready
- Celebrity overload, inactive user waste

Fan-out on read:
+ Light writes
- Slow reads, high latency

Hybrid:
+ Balanced
- More complex logic

---

## Interview Checklist
→ Two flows? → post creation (fan-out) and feed retrieval
→ Fan-out on write vs read? → push vs pull, trade-offs
→ Hybrid approach? → write for normal users, read for celebrities
→ Hotspot/hotkey problem? → celebrity causing overload
→ Consistent hashing? → ring, minimal resharding, virtual nodes
→ What's in Redis? → post ID list per user, basic metadata
→ Full post content where? → Post Service cache, then DB
→ Recommendation algorithms? → separate system, out of scope
→ Collaborative vs content-based filtering? → behavior-based vs content-based