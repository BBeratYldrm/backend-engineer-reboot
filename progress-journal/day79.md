# Day 79 — June 5, 2026

## What I did today

Code review practice — analyzed a broken OrderController:
- Constructor injection vs field injection
- SRP violation — controller orchestrating too many responsibilities
- Transaction gap — recognized missing transaction boundary, extended into why Outbox Pattern is the correct fix over wrapping an external payment call in @Transactional
- Entity leak — API should return DTOs, not domain entities
- Missing exception/null handling on downstream calls

Covered SLI/SLO/SLA from scratch:
- SLI as the measured metric, SLO as the internal target, SLA as the contractual version
- Error budget concept and its use in balancing release velocity vs stability
- PromQL basics — error rate query example using rate() over a counter metric

## Next

- SD-08 review (Ad Click Aggregation)
- Resume Two Pointer algorithm track — 392. Is Subsequence
- Mock interview when interview date confirmed