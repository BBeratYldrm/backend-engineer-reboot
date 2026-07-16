# Day 90 — July 16, 2026

## What I did today

Code review practice session. Reviewed a Spring Boot reservation service and worked through it systematically rather than just scanning for obvious mistakes.

Findings from the review: missing transaction boundary around a multi-step write operation, a Single Responsibility violation (shop lookup, entity creation, and notification sending all combined in one method), field injection instead of constructor injection, unguarded `Optional.get()` calls, and an N+1-style query pattern where the same lookup was repeated unnecessarily inside a loop instead of being pulled out once.

Refactored the service to address each finding — moved to constructor injection, replaced `.get()` with `orElseThrow()` and a proper exception, added the transaction boundary, eliminated the redundant query, and replaced magic string status values with an enum. The more interesting fix was around the notification step: rather than sending it synchronously inside the same transaction (which risks rolling back a valid reservation if the notification call fails, or holding a DB connection open during a slow external call), the notification was decoupled using the Outbox pattern — the event is persisted in the same transaction as the reservation, and a separate consumer handles delivery asynchronously.

Built a personal code review checklist afterward — a fixed five-step order to run through on every review: correctness and data integrity, responsibility and structure, performance and query efficiency, error handling and observability, and finally naming and readability. The ordering follows a deliberate severity logic — check what's most expensive to get wrong in production first, and leave the cheapest-to-fix issues for last.

## Next

- Continue applying the review checklist on future code review sessions
- Resume regular study rhythm