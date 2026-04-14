# Day 07 — April 14, 2026

## What I learned

### OAuth2 — Deep Dive
- Authorization Code Flow — step by step
- State parameter → CSRF protection in OAuth2
- Client ID (app identity) vs Client Secret (app password)
- Authorization code → single use, short lived, safe to pass in URL
- Token exchange → server-to-server, never in URL
- Drew my own diagram ✅

### JWT — Deep Dive
- JWT = Access Token — same thing
- Structure: Header.Payload.Signature
- Payload is Base64 encoded — NOT encrypted, never store passwords
- Access Token → short lived (15 min), stateless, in Authorization header
- Refresh Token → long lived (30 days), stored in DB/Redis
- Logout → delete refresh token from DB
- Multi-device → each device has its own refresh token
- Drew my own diagram ✅

### [3.1] SQL — Complete
- Index — why, when, when not
- B-Tree — how it works, O(log n)
- Index types — B-Tree, Hash, Partial, Covering
- EXPLAIN — type, key, rows, Extra
- Transaction Isolation — Dirty Read, Non-Repeatable Read, Phantom Read
- Isolation levels — Read Committed, Repeatable Read, Serializable
- ACID — Atomicity, Consistency, Isolation, Durability
- Optimistic vs Pessimistic Locking — when to use which
- DDD — Rich Domain Model vs Anemic Model

## How I feel
Tired but solid.
Drew my own diagrams — things are really clicking now.
Starting to connect the dots between topics.

## Tomorrow
- [3.2] Redis — cache strategies, Redlock, rate limiting
- Algorithm practice