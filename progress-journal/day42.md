# Day 42 — May 28, 2026

## What I learned

### Algorithm — Two Pointer
- IsSubsequence (392) — left pointer on s, right scans t, advance left only on match, return left == s.length()
- Short-circuit left < s.length() before charAt to avoid index out of bounds
- ValidPalindrome II (680) — ends-to-middle, on mismatch try skipping left or right, helper isPalindrome(s, l, r)
- ValidParentheses — stack pattern, pop and compare on closing bracket

### System Design — Ad Click Aggregator
- Pre-aggregation pattern: count in Redis (INCR), flush to storage periodically, not one DB write per event
- Click flow: API Gateway → Click Tracking Service → redirect immediately + async to Kafka → Aggregator Service → Redis counter → periodic write to Cassandra/OLAP
- Query path: recent from Redis, historical from OLAP, combined by Query Service
- Duplicate prevention: rate limiter + Bloom filter / impression ID idempotency
- Compared with hellointerview solution: Kinesis = Kafka, Flink for stream aggregation, OLAP DB, S3 raw dump + Spark reconciliation (Lambda architecture)
- Key insight: same problem solved with different tools — pre-aggregation is the core

### System Design — deepened messaging concepts
- SnowFlake ID: 64-bit long, timestamp + machineId + sequence, time-sortable, distributed, sequence resets per ms
- Redis TTL + heartbeat presence (TTL = 3x heartbeat as buffer)
- WebSocket vs HTTP: send via HTTP through API Gateway, receive via WebSocket (bypasses gateway)
- mark-as-read pipeline: same flow as sendMessage, different Kafka topic
- WebSocket horizontal scale: store serverId in Redis presence so delivery service knows where to push

## How I feel
Long day, covered a lot of system design ground. The depth is starting to connect.

## Next
- Two Pointer list continues — 905. Sort Array By Parity
- Interview prep — Kafka deep dive, HBase, defending system design
- Live coding practice — concurrency