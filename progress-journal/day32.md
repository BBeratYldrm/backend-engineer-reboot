# Day 32 — May 17, 2026

## What I learned

### Rare Topics Review — Bloom Filter & HyperLogLog

Bloom Filter:
- Membership testing — "is this element in the set?"
- "No" is always correct — no false negatives
- "Yes" might be wrong — false positives possible
- O(1) lookup, very low memory
- Use case: Chrome malicious URL check, Search Autocomplete cache miss check

HyperLogLog:
- Unique element counting — "how many distinct elements?"
- Does not give exact count — approximation (~1% error)
- Memory: 12KB regardless of dataset size vs hundreds of MB for a Set
- Use case: YouTube unique view counts, unique visitor tracking
- Trade-off: same as Bloom Filter — speed + memory over exactness

### Write-Ahead Log (WAL)
- Log first, apply second — always
- Before any data hits disk, change is written to log
- Crash mid-write → replay log on restart → no corruption, no data loss
- Sequential append → faster than random disk writes
- Appears in: PostgreSQL/MySQL crash recovery, primary-replica replication,
  Kafka commit log, Outbox pattern
- Enables ACID durability (the D)
- Replication = WAL shipping — replicas replay primary's log to stay in sync
- Kafka calls itself a "distributed commit log" — same concept
- Note added: notes/distributed/24-wal.md

### Algorithm — MinSizeSubarraySum (carry-over from Day 31)
- Completed and tested in IntelliJ
- All three test cases passed

## How I feel
Low energy day. Stayed home, kept the streak alive.
Bloom Filter and HyperLogLog clicked quickly — good analogies helped.
WAL was satisfying to understand because it connects so many things
already covered: Kafka, Outbox, replication, ACID. It was not a new concept,
it was the missing name for something already known.
Tomorrow is a heavier day emotionally. Taking it easy tonight was the right call.

## Next
- System Design — SD-06 Search Autocomplete
- Algorithm pattern practice — mixed questions
- AWS basics
- Mock interview preparation