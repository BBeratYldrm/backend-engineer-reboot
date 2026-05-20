# Day 35 — May 20, 2026

## What I learned

### Java Interview Crash Course — Module 3 (continued)
- Exception Handling — @ControllerAdvice, checked vs unchecked
- Retry — @Retryable, exponential backoff, jitter, @Recover
- Pagination — Spring Data Pageable, offset vs cursor based
- Kafka Idempotency — at-least-once, idempotency key, Redis/DB deduplication

### Java Interview Crash Course — Module 4
- LRU Cache — Least Recently Used, evicts oldest element
- LinkedHashMap with accessOrder=true — accessed elements move to tail
- removeEldestEntry — automatic eviction when capacity exceeded
- In-memory vs Redis cache — production always Redis, LRU interview concept

### Key concepts
- LinkedHashMap = HashMap + doubly linked list
- accessOrder=true → get/put moves element to tail → head = oldest
- Cache lives in RAM, lost on restart — production needs Redis

## How I feel
Long and productive day. Recruiter call in the evening added some
positive energy. Java internals are coming back — most of it was
familiar once explained. LRU Cache clicked quickly once the
LinkedHashMap internals were clear. Need to let today's content sink in.

## Next
- Module 4 — Producer-Consumer, Immutable Class
- Module 5 — SQL
- SD-06 Search Autocomplete
- AWS Basics