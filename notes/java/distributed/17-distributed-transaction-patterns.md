# [4.4] Distributed Transaction Patterns — 2PC vs SAGA

## Keywords
distributed-transaction · two-phase-commit · saga · strong-consistency ·
eventual-consistency · coordinator · blocking · compensating-transaction ·
atomic · non-blocking · 2pc-vs-saga

---

## The Problem

Single DB: one transaction, ACID guaranteed.
"Either everything commits or nothing does."

Multiple DBs (microservices): no shared transaction.

Bank transfer example:
DB-A: deduct 1000 from Account A
DB-B: add 1000 to Account B

Both must happen atomically.
One succeeds, other fails → money lost or doubled.

Two solutions to this problem:
2PC  → strong consistency, synchronous, blocking
SAGA → eventual consistency, asynchronous, non-blocking

---

## Two Phase Commit (2PC)

Strong consistency approach.
A coordinator manages all participants.
Either all commit or all rollback — no partial state.

Phase 1 — Prepare:
Coordinator → asks DB-A: "can you commit?"
Coordinator → asks DB-B: "can you commit?"

DB-A → "yes, ready" (acquires lock, waits)
DB-B → "yes, ready" (acquires lock, waits)

Phase 2 — Commit or Rollback:
Both said yes → Coordinator → "commit" → DB-A commits, DB-B commits
Any said no  → Coordinator → "rollback" → DB-A rollbacks, DB-B rollbacks

---

## 2PC — The Blocking Problem

Coordinator crashed after Phase 1 — both DBs said "ready" and are holding locks.

DB-A locked, waiting
DB-B locked, waiting
Coordinator is down — no one sends commit or rollback
Both DBs frozen until coordinator recovers

This is the blocking problem — the fatal flaw of 2PC.

Timeout does not fully solve it:
Timeout fires → DB-A rollbacks
Coordinator recovers → sends "commit" to DB-B
Result: DB-A rolled back, DB-B committed → inconsistency

---

## 2PC Trade-offs

+ Strong consistency — truly atomic across multiple DBs
+ Immediate consistency — not eventual
+ Simple mental model — all or nothing

- Blocking — coordinator failure freezes all participants
- Performance — locks held for entire duration of both phases
- Single point of failure — coordinator is critical
- Does not scale — more DBs means more coordination complexity
- Not suitable for microservices — cross-team, cross-network dependencies

When 2PC is used:
→ Same company, same network, few DBs
→ Traditional enterprise systems with shared infrastructure
→ When strong consistency is non-negotiable and scale is not a concern

---

## SAGA — The Microservice Alternative

Eventual consistency approach.
Break the distributed operation into local transactions.
Each step has a compensating transaction if something goes wrong.

Two outcomes only:
All steps succeed → operation complete
One step fails → previous steps compensated → clean state

No coordinator holding locks across services.
Each service commits locally and moves on.

Full SAGA details → see 14-microservices-saga.md

---

## 2PC vs SAGA — Side by Side

Consistency:
2PC  → strong consistency — all DBs updated simultaneously
SAGA → eventual consistency — updates propagate over time

Blocking:
2PC  → blocking — locks held, coordinator failure freezes system
SAGA → non-blocking — each service commits independently

Failure handling:
2PC  → coordinator manages rollback atomically
SAGA → compensating transactions — new operations that reverse effects

Scale:
2PC  → does not scale — coordination overhead grows with participants
SAGA → scales naturally — each service independent

Complexity:
2PC  → simple concept, complex failure modes
SAGA → more moving parts, but each part is simple and independent

Use when:
2PC  → few DBs, same network, strong consistency required, no microservices
SAGA → microservices, different teams, eventual consistency acceptable

---

## Pattern Category

Both are Distributed Transaction Patterns — different solutions to the same problem.

Distributed Transaction Patterns:
→ 2PC  — synchronous, strong consistency
→ SAGA — asynchronous, eventual consistency

Other pattern categories for reference:
Resilience Patterns    → Circuit Breaker, Bulkhead, Retry, Timeout
Data Patterns          → CQRS, Event Sourcing, Outbox Pattern
Messaging Patterns     → Outbox, Idempotent Consumer, DLQ

---

## Senior-Level Framing

Most candidates know either SAGA or 2PC.
Knowing both and comparing them is senior level.

In interviews:
"For distributed transactions there are two main patterns — 2PC and SAGA.
2PC gives strong consistency but has a blocking problem:
if the coordinator fails, all participants freeze holding locks.
It does not scale and is not suitable for microservices.
SAGA accepts eventual consistency but is non-blocking and scales naturally.
Each service commits locally, and compensating transactions handle failures.
In microservice architectures, SAGA is almost always the right choice.
2PC makes sense only in traditional systems with few DBs on the same network."

---

## Interview Checklist
→ What problem do both solve? → atomic operations across multiple DBs
→ 2PC phases? → Prepare (can you commit?) then Commit or Rollback
→ 2PC blocking problem? → coordinator failure freezes all participants holding locks
→ Why 2PC does not scale? → coordination overhead grows, locks held across network
→ SAGA vs 2PC consistency? → eventual vs strong
→ When to use 2PC? → same network, few DBs, strong consistency required
→ When to use SAGA? → microservices, different teams, scale required
→ Pattern category? → both are Distributed Transaction Patterns