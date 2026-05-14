# Day 27 — May 12, 2026

## What I learned

### System Design — Chat System (deep dive)
- WebSocket vs polling — persistent connection vs repeated requests
- Fan-out problem — one message to many recipients
- Push vs pull strategy — 1-1 push, large group pull, hybrid
- Presence detection — Redis TTL + heartbeat, O(1) lookup
- Kafka partition key — chatId for ordering guarantee
- Cassandra for messages — write-heavy, time-series natural fit
- SQL vs NoSQL decision — when each fits
- CAP decision — AP for chat, eventual consistency acceptable
- Multi-device sync — multiple WebSocket connections per user

### Algorithm Practice
- Capitalize words — split, charAt, substring, String.join
- Edge cases — null input, blank string, empty words
- Clean naming, method decomposition

### Code Review Practice
- Applied universal code review checklist
- Identified: null check, resource leak, sensitive data in logs,
  DIP violation, SRP violation, encoding issue, exception handling
- PR-style feedback writing in plain English

## How I feel
Heavy personal day but pushed through.
Chat system clicked well — WebSocket and presence detection
finally connected to real usage patterns.
Code review under time pressure revealed gaps in checklist recall.

## Next
- Messaging system design practice
- Outbox pattern connection to real systems
- Algorithm practice continues