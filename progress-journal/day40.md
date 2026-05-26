# Day 40 — May 26, 2026

## What I learned

### Java & Spring — Quiz Session (no notes, no hints)

- @Transactional internals — proxy mechanism, self-invocation trap
    - Self-invocation: calling a @Transactional method within the same class bypasses the proxy
    - Solution: split into separate beans
    - Checked exceptions do not trigger rollback automatically — use rollbackFor
- synchronized vs volatile vs AtomicInteger
    - volatile: visibility + ordering, not atomicity
    - synchronized: visibility + atomicity + ordering, but slower
    - AtomicInteger: thread-safe counter via CAS, no locking
    - When to use: flag → volatile, single counter → AtomicInteger, complex shared state → synchronized
- Bean Lifecycle — 6 steps: instantiate, DI, Aware interfaces, BeanPostProcessor before, @PostConstruct, BeanPostProcessor after
    - @Transactional proxy created in BeanPostProcessor after step
    - Calling @Transactional method inside @PostConstruct is risky — proxy not fully ready
- Constructor vs field injection — constructor makes dependencies explicit, reveals SRP violations early
- @Component vs @Service vs @Repository — @Repository adds Spring exception translation (SQLException → DataAccessException)
- HTTP status codes — 200, 201, 400, 401, 403, 404, 500
    - 401 vs 403: authentication missing vs authorization denied
- WHERE vs HAVING — WHERE filters raw rows, HAVING filters after GROUP BY
- REST idempotency — idempotency key stored in Redis with TTL, checked before processing

### Kafka — Review
- Partition, consumer group, offset — solid
- Multiple consumer groups reading same topic independently — each tracks own offset
- At-least-once + idempotency = safe and practical combination
- Kafka vs RabbitMQ vs ActiveMQ — when to choose which
    - Kafka: high volume, replay, multiple consumers
    - RabbitMQ: complex routing, low latency, task queues
    - ActiveMQ: JMS standard, low-medium volume, existing Java enterprise systems
- Apache Pulsar — modern Kafka alternative, better multi-tenancy and geo-replication

### Microservices & Resilience — Deep Session

- Circuit Breaker — CLOSED / OPEN / HALF-OPEN states, automatic transitions via Resilience4j
    - Fallback: cached data, safe default, graceful degradation
- Bulkhead — isolated thread pools per dependency, prevents cascading failure
    - Ship compartment analogy: one section floods, ship survives
- Timeout — prevents thread exhaustion, connection timeout vs read timeout
- Rate Limiter — limits incoming traffic, different from Bulkhead (outgoing)
- All four work together: Rate Limiter → Bulkhead → Circuit Breaker → Timeout → Retry → Fallback
- Resilience4j — open source, Spring Boot integrated, industry standard after Hystrix deprecation

### CQRS
- Separate read and write models — different performance and consistency requirements
- Write: normalized DB, ACID, business logic
- Read: denormalized, Redis or Elasticsearch, optimized for queries
- Read model updated via Kafka events (event-driven) or polling/cron jobs
- Eventual consistency on read side — accepted trade-off for performance
- Outbox Pattern connection: ensures reliable event delivery from write side to Kafka → read model

### Distributed Transactions
- 2PC — two phases: Prepare and Commit
- Trade-offs: blocking (all services wait), single point of failure (coordinator down = everything locked), performance (round-trips, locks, waiting)
- Preferred alternative: SAGA pattern — local transactions + compensations, no blocking

### Algorithms
- TwoSum — solved again independently, 2 minutes. Muscle memory intact.

## How I feel
Long and productive day. Answered most questions without notes and in English.
Confidence came back a bit — realized I know more than I think.
Some gaps still exist but they are specific and fixable.

## Next
- Two Pointer list continues — 392. Is Subsequence
- Remaining topics: Spring Security, JVM basics, Docker/Kubernetes, SQL deep dive
- LinkedIn post questions review — select 5-6 unfamiliar ones