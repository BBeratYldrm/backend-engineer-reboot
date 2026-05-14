# Day 28 — May 13, 2026

## What I learned

### System Design — Messaging System (full design)
Designed a complete 1-1 messaging system from scratch:

Architecture:
- API Gateway with Token Bucket rate limiting per user
- Message Service — sendMessage, markAsRead
- WebSocket Server — persistent connections, presence in Redis
- Sync Service — incremental sync with lastSyncTimestamp
- Delivery Service — Kafka consumer, checks presence, routes delivery
- Kafka — chat_id partition key, ordering preserved
- Cassandra — message storage, 2-week retention
- Redis — presence detection (TTL + heartbeat) + recent message cache
- MySQL — chat metadata

APIs designed:
- sendMessage with clientMessageId (idempotency key)
- markAsRead with lastReadMessageId
- sync (incremental, not full history)
- registerDevice / unregisterDevice

Key design decisions:
- Snowflake ID for message ordering — distributed, time-sortable
- Redis TTL presence detection — O(1), auto-expires on disconnect
- Consistent Hashing for WebSocket server scaling
- At-least-once delivery + clientMessageId idempotency
- Incremental sync leverages device local storage

### Connections made
- Outbox Pattern — Message Service cannot write to Cassandra
  and Kafka atomically. Outbox table bridges the gap.
- Fan-out Service — would be added for group chat scenarios.
  Hybrid approach: write fan-out for small groups,
  read fan-out for large groups.

## How I feel
Difficult personal day but completed a full system design.
Outbox Pattern and fan-out connections came naturally after the design.
The design felt real — not just theory.

## Next
- CV update — strengthen experience descriptions
- AWS basics
- Search Autocomplete system design
- Algorithm practice