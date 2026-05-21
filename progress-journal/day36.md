# Day 36 — May 21, 2026

## What I learned

### System Design — SD-06 Search Autocomplete
- Two services: Query Service and Data Collection Service
- Debounce on client side — wait 300ms before sending request
- Redis Sorted Set for suggestions — score = search frequency
- Trie — prefix tree, each node a letter, O(p) search time
- Bloom Filter inside Query Service — filters nonsense prefixes before hitting Redis
- Kafka for data collection — buffers high-volume search events, decouples services
- Weekly batch job rebuilds Trie from DB, repopulates Redis
- Note added: notes/system-design/SD-06-search-autocomplete.md

### Java Interview Crash Course — Module 4 & 5 completed
- LRU Cache — LinkedHashMap with accessOrder=true, removeEldestEntry
- Producer-Consumer — BlockingQueue, put() blocks when full, take() blocks when empty
- Immutable Class — final class, private final fields, defensive copy for mutable fields
- SQL — 2nd highest salary, WHERE vs HAVING, INNER JOIN vs LEFT JOIN, duplicates, department max salary

### Senior Roadmap Gap Analysis
- Full roadmap reviewed phase by phase
- Key gaps identified: Virtual Threads, N+1 problem, Optimistic/Pessimistic locking, Self-invocation trap, API versioning

## How I feel
Heavy day. Technically productive but emotionally draining.
Carrying a lot at once — job uncertainty, financial pressure, being alone in a foreign country.
The streak is holding. That matters.
Some days just getting through is enough.

## Next
- Streams coding problems
- @Transactional internals, N+1 problem
- Algorithm practice — evening sessions
- Mock interview when interview date is confirmed