# Day 87 — July 13, 2026

## What I did today

Continued the independent review pass — worked through the full sequential Q&A list again, focusing on the payment correctness narrative end-to-end: idempotency → locking strategy → ledger integrity → reconciliation, spoken as one continuous story rather than four separate topics.

Drilled the optimistic locking mechanism until the explanation was tight — the `@Version` field, the WHERE-clause check at the SQL level, the zero-row-update case triggering `OptimisticLockException`, and how that compares to Doma's `SelectOptions.forUpdate()` for pessimistic locking. Paired each with the corresponding interview-ready English sentence.

Reviewed checked-exception rollback behavior under `@Transactional` again — confirmed the precise phrasing: unchecked exceptions trigger rollback by default, checked exceptions require explicit `rollbackFor`. Rehearsed this alongside the self-invocation proxy bypass explanation since these two tend to get chained together in questions.

Worked through the seven project narratives once more, tightening the delivery on the stress test story and the CI/CD plugin patch story — both flagged earlier as standout senior-level details worth leading with when given the chance.

Reviewed the self-introduction and the 30-second → 1-minute → project-story structure one more time for smooth delivery under time pressure.

## Next

- Final review pass — design patterns section and the daily-use pattern examples (Repository, Template Method, Adapter)
- Upcoming payment-system interview
- Rehearse payment correctness narrative out loud once more beforehand