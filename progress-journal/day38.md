# Day 38 — May 24, 2026

## What I learned

### Java Interview Prep — Technical Questions
- Abstract class vs Interface — is-a vs can-do, extend vs implement, SOLID connection
- HashMap internals — hashCode, bucket, collision, LinkedList → Red-Black Tree at 8 elements, rehash at 75%
- equals() and hashCode() contract — when equals is true, hashCode must be same
- synchronized vs volatile — atomicity vs visibility, both needed for thread-safe Singleton
- Deadlock — fixed lock ordering, tryLock, timeout
- @Transactional — proxy mechanism, self-invocation trap, unchecked → auto rollback, checked → no rollback by default, rollbackFor
- Kafka — topics, partitions, consumer groups, at-least-once, idempotency key
- Kafka partition rule — one partition per consumer within a group, multiple groups can read same partition independently

### Streams & Functional Interfaces
- Predicate, Function, Consumer, Supplier — reviewed with code
- Stream pipeline — filter, map, sorted, limit, collect
- groupingBy, toMap — coded with Employee examples

## How I feel
Sunday evening, winding down. A lot covered today.
Knowledge is there — just needs activation.

## Next
- Java interview prep continues — Multithreading, Spring DI, Bean Lifecycle
- Two Pointer algorithm practice
- Behavioral prep when interview date confirmed