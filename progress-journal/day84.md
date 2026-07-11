# Day 84 — July 10, 2026

## What I did today

Reviewed a staffing-agency skills-matching form for a new contract role
application; clarified an ambiguous public-cloud experience item and
finalized honest phrasing distinguishing container orchestration
experience from major public cloud provider production experience.

Deep-dived idempotency key handling: reasoning through a combined
Redis+DB approach (fast-path cache vs. authoritative constraint),
corrected a consistency-model mix-up (eventual consistency vs. ACID),
and explored Bloom filters as a fast pre-check layer ahead of the
authoritative store.

Worked through optimistic locking end-to-end: the version-field
mechanism at the SQL level (version included in the WHERE clause,
a zero-row update triggering an exception), a concrete entity/
repository/service code walkthrough, and how a framework used at a
past employer implements the same concept independently of JPA.

Covered pessimistic locking (SELECT ... FOR UPDATE, framework-level
abstractions) and worked through trade-offs in depth: when to use which
locking strategy, lock duration discipline, timeout configuration, and
deadlock avoidance via consistent lock ordering.

Connected database-level locking concepts to JVM-level concurrency
(synchronized, volatile, deadlock parallels, blocking queues, Runnable
vs. Callable) to clarify where each layer's responsibility begins and
ends.

Had an initial exploratory conversation with a recruiter for an
unrelated opportunity in a different industry; reviewed the JD
afterward, confirmed a language/stack mismatch for current focus, and
deprioritized it.

Consolidated an active recruiting pipeline list and resolved a
duplicate-application issue.

Reaffirmed near-term strategy: prioritize the confirmed upcoming
interview, treat any contract offer as a financial bridge while
continuing to interview for permanent roles in parallel.

## Next

- Self-introduction draft + CV project walkthrough (still pending)
- A-tier review: REST API design, SQL optimization, cookie vs token,
  Linux commands, CI/CD
- Mock interview simulation