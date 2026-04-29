# Day 20 — April 29, 2026

## What I learned

### Phase 1 — Gap Fill (all core notes updated)
- equals/hashCode/compareTo — contract violations, Comparable vs Comparator
- Defensive copy — immutability not enough without it
- Specification, Strategy, Null Object patterns
- Pattern Matching — standalone section, guarded patterns, sealed combination
- CompletableFuture — why it falls short, 5 concrete problems
- Metaspace classloader leak — symptoms, fix
- Visibility vs atomicity — volatile vs AtomicInteger distinction
- Shenandoah GC — added alongside ZGC
- Starvation — vs deadlock, fair lock solution
- ThreadLocal + ScopedValue — virtual thread implications
- Producer-Consumer, Bulkhead patterns

### Phase 2 — Spring Ecosystem (completed)
- Spring Core — IoC container, proxy mechanism, self-invocation bug, AOP
- Spring Boot — auto-configuration, profiles, secret management, liveness vs readiness
- Spring MVC — thin controller, idempotency, REST semantics, global exception handling
- Spring Data — persistence context, dirty checking, N+1, transaction boundary, keyset pagination
- Spring Security — JWT lifecycle, OAuth2 flow, PKCE, client credentials, method-level security

### Phase 3 — Data Layer (partial)
- SQL — normalization trade-offs, composite index leftmost prefix rule, EXPLAIN, isolation levels, optimistic vs pessimistic locking
- Redis — Cache-Aside with thundering herd solution, TTL vs active invalidation, write strategies, rate limiting with Lua atomicity, fixed vs sliding window, two-layer rate limiting, distributed lock with heartbeat and idempotency

## How I feel
Heavy day — a lot of ground covered across multiple phases.
Proxy mechanism and self-invocation bug clicked properly.
Redis patterns took time but the flow diagrams helped everything land.
Distributed lock edge cases were the most interesting part of the day.

## Next
- Phase 3 — ELK Stack (Elasticsearch, Logstash, Kibana, Filebeat)
- Phase 8 — System Design scenarios