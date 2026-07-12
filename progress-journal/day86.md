# Day 86 — July 12, 2026

## What I did today

Spent an extended independent review session (3-4 hours) working through
all prepared reference material — the full sequential Q&A list, design
patterns, and personal explanations — to reinforce retention ahead of
the upcoming interview.

Ran a short 6-question mixed-topic quiz to check retention: 4/6 answered
correctly without gaps (async callback execution thread, optimistic
locking mechanism, Strategy pattern's SOLID connections, NoSQL
experience boundaries). Identified two gaps — the full payment
correctness narrative (missing the locking and ledger steps when
summarized quickly) and precise phrasing on checked-exception rollback
behavior under a transactional annotation.

Did a deep-dive discussion on locking necessity and scalability in
high-volume payment systems: why an append-only ledger design avoids
most lock contention, why locking should be scoped to the mutable
balance only and kept as short-lived as possible, and how hot-account/
high-fan-out scenarios are typically handled via serialized processing
rather than fighting over a database lock.

Clarified, with concrete scenarios, how genuinely concurrent requests
to the same account arise in practice (horizontal scaling across
instances, multi-threading within an instance, client-side retries,
shared-account access) — moving the concept from abstract to intuitive.

Confirmed the boundary between database-level locking and JVM-level
concurrency primitives (synchronized/volatile/ReentrantLock) — for a
stateless, database-backed service, DB-level locking alone is
sufficient, and deep JVM-level concurrency mechanics are not needed for
this context.

## Next

- Continue independent review of prepared material
- Prep meeting with recruiter tomorrow ahead of Wednesday's interview
- Rehearse the full payment-correctness narrative end-to-end (identified
  gap from today's quiz)