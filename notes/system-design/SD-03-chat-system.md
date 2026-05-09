# [SD-03] System Design — Chat System

## Keywords
chat-system · websocket · fan-out · presence-detection · push-notification ·
cassandra · nosql · message-ordering · chat-id · snowflake-id ·
redis-sorted-set · polling · ap-system · eventual-consistency

---

## Step 1 — Clarify Requirements

Questions to ask:
→ 1-1 messaging only, or group chat too?
→ Max group size?
→ Message history — how long?
→ Multi-region needed?
→ RPM per user?
→ Online presence needed?
→ Multi-device sync?

Assumptions:
→ 1-1 and group chat (max 500 people)
→ Message history kept indefinitely
→ Multi-region
→ User-based rate limiting
→ Presence detection needed
→ AP system — availability over consistency

---

## Step 2 — CAP Decision

Chat system → AP (Availability + Partition Tolerance)

Message arriving 100ms late → acceptable
Application being down → not acceptable

Eventual consistency is sufficient for chat.
Strong consistency not needed — ordering within a chat is enough.

---

## Step 3 — Core Concepts

### WebSocket — Real-time Connection

Normal HTTP:
Client → request → Server → response → connection closed
Client must ask "any new messages?" repeatedly → polling → inefficient

WebSocket:
Client → connects → connection stays OPEN
Server can push messages anytime
Client can send anytime

App behavior:
App open → WebSocket connection established → messages arrive instantly
App closed → WebSocket disconnected → Push Notification takes over

Trade-offs:
+ Real-time, low latency
+ Server pushes without client asking
- Every user = one open connection → memory usage
- Millions of users = millions of open connections → needs careful scaling

Long Polling (alternative):
Client sends request → server holds it until message arrives → responds
+ Simpler than WebSocket
- Higher network overhead, not truly real-time

### Fan-out — One Message, Many Recipients

1-1 chat → message goes to 1 person → simple
Group chat (500 people) → message goes to 500 → fan-out problem

Strategy:
Small group / most online  → Push (deliver immediately to all)
Large group / most offline → Pull (store, deliver when user opens)

Hybrid approach — decide based on group size and online ratio.

### Presence Detection

User opens app → WebSocket connected
→ Redis: set "userId: online" with TTL

User closes app → WebSocket disconnected
→ Redis: set "userId: offline"

TTL on presence key:
If app crashes → user auto-marked offline after timeout
Prevents ghost "online" status

Redis presence check:
Online  → deliver via WebSocket immediately
Offline → send Push Notification

---

## Step 4 — High-Level Architecture

Client (mobile app):
↕ WebSocket (persistent, real-time)
↕ POST /messages (send message)
↕ GET /messages (load history)

Backend flow:

Client → POST /messages → Chat Service
↓
Cassandra (permanent storage)
Redis Sorted Set (recent messages cache)
Kafka ("new_message" event)
↓
Message Delivery Service (Kafka consumer)
↓
Redis: is recipient online?
↓                ↓
Online            Offline
↓                ↓
WebSocket        Push Notification
(instant)        (mobile push service)

---

## Step 5 — Kafka Design

Topic: "messages"
Partition key: chatId

Why chatId and not userId?
→ All messages in same chat → same partition → ordering guaranteed
→ userId would split conversation → messages arrive out of order

Message event:
{
"messageId": "snowflake-id",
"chatId": "chat-abc",
"senderId": "user-123",
"content": "hello",
"timestamp": 1715234567890,
"type": "TEXT"
}

messageId → Snowflake ID (distributed unique, time-sortable)

---

## Step 6 — Storage Design

### Redis — Cache Layer

Presence:
Key: "presence:userId"
Value: "online" / "offline"
TTL: 60 seconds (refreshed while app is open)

Recent messages (Sorted Set):
Key: "chat:chatId:messages"
Score: timestamp
Value: message content

App opens → fetch last 20 from Redis → instant
Scroll up → fetch older messages from DB → slight loading

### Cassandra — Permanent Storage

Why not SQL?
→ Billions of messages → single table too large
→ Write-heavy — messages always coming in, rarely updated
→ No complex JOIN needed
→ Horizontal scale difficult with SQL

Why Cassandra?
+ Write speed — optimized for high-volume writes
+ Horizontal scale — add nodes easily
+ Natural fit — chatId as partition key, timestamp as sort key
+ Time-series data — messages are timestamp-ordered

Schema:
Partition Key:   chat_id
Clustering Key:  timestamp DESC (newest first)

chat_id    | timestamp      | message_id | sender_id | content
chat-abc   | 1715234567890  | msg-001    | user-123  | "hello"
chat-abc   | 1715234567000  | msg-002    | user-456  | "hi"

Same chat → same partition → fast reads, fast writes

### When to use SQL vs NoSQL

SQL (MySQL, PostgreSQL):
→ Relational data (users, profiles, payments)
→ ACID critical (financial transactions)
→ Complex JOINs needed

NoSQL (Cassandra, HBase, MongoDB):
→ Very large data, horizontal scale needed
→ Write-heavy systems
→ Flexible schema
→ Time-series data (messages, logs, events)

Chat messages → NoSQL (Cassandra)
User profiles → SQL

---

## Step 7 — Multi-device Sync

User has phone and tablet. Message arrives on both.

Each device has its own WebSocket connection.
Message Delivery Service checks all active connections for a user.
Delivers to all connected devices simultaneously.

Offline device:
When device comes online → fetches missed messages from Cassandra
Cursor-based pagination — "give me messages after timestamp X"

---

## Step 8 — Scaling Considerations

WebSocket connections:
Millions of users = millions of open connections
Solution: dedicated WebSocket servers, horizontal scale
Load balancer routes user to same WebSocket server (sticky session)

Message volume:
High write throughput → Cassandra handles naturally
Kafka partitions → add partitions as volume grows

Presence at scale:
Redis cluster → distribute presence data
TTL-based → auto-cleanup, no manual management

---

## Trade-offs Summary

WebSocket vs Long Polling:
+ WebSocket: real-time, efficient
- WebSocket: connection management complexity at scale

Push vs Pull fan-out:
+ Push: instant delivery for online users
+ Pull: efficient for large groups with many offline users
- Hybrid adds complexity

Cassandra vs MySQL:
+ Cassandra: scale, write speed, natural chat schema
- Cassandra: no complex queries, no transactions

AP vs CP:
+ AP: always available, partition tolerant
- Slightly stale data possible (eventual consistency)

---

## Interview Checklist
→ WebSocket vs polling? → persistent connection vs repeated requests
→ Why chatId as partition key? → ordering within conversation guaranteed
→ Fan-out problem? → one message to many recipients, push vs pull strategy
→ Presence detection? → Redis with TTL, WebSocket connect/disconnect events
→ Why Cassandra? → write-heavy, horizontal scale, time-series natural fit
→ SQL vs NoSQL? → relational/ACID vs scale/write-heavy
→ Multi-device sync? → multiple WebSocket connections per user
→ CAP choice? → AP — availability over consistency for chat