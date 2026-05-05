# [4.1] Microservices — SAGA Pattern

## Keywords
saga · compensation · choreography · orchestration · idempotency ·
retry · exponential-backoff · dead-letter-queue · distributed-transaction ·
at-least-once-delivery · workflow-engine · temporal

---

## Why SAGA

Monolith: one transaction, ACID guaranteed.
Microservices: each service has its own DB, no shared transaction.

Problem:
Order created  → Payment taken  → Stock service crashes 
System is now inconsistent — order exists, payment taken, stock not updated.

SAGA solves this: break the distributed operation into a sequence of local transactions,
each with a compensating transaction if something goes wrong.

Two outcomes only — no partial state:
All steps succeed → operation complete 
One step fails → previous steps compensated → clean state 

## Compensating Transaction

Not a real rollback — cannot undo like a DB transaction.
Instead: a new operation that reverses the effect.

Forward:              Compensate:
─────────────         ──────────────────
Create order     →    Cancel order
Take payment     →    Issue refund
Reserve stock    →    Release stock
Send notification →   Send cancellation notice

Orchestrator runs compensations in reverse order when a step fails.

## Choreography

No central coordinator. Each service publishes an event when done.
Other services listen and react.

Flow:
Order Service    → publishes "OrderCreated"
Payment Service  → listens, charges, publishes "PaymentCompleted"
Stock Service    → listens, reserves, publishes "StockReserved"
Notification     → listens, sends confirmation

No one calls anyone directly. Everyone reacts to events.

If Notification crashes:
→ Kafka holds the message (durability)
→ Notification restarts → reads from last offset → processes 

Trade-offs:
+ Loosely coupled — services don't know about each other
+ No single point of failure
+ Scales independently
- Hard to track overall flow — where is the order right now?
- Hard to debug — which step failed?
- Compensations are complex — who triggers them?

Tools: Kafka, RabbitMQ — just the transport layer, no extra tooling needed.

## Orchestration

Central coordinator — Orchestrator — manages the entire flow.
Calls each service, waits for response, decides next step.

Flow:
Orchestrator → calls Payment Service → success
Orchestrator → calls Stock Service → success
Orchestrator → calls Notification Service → fails
Orchestrator → calls Stock Service: compensate (release stock)
Orchestrator → calls Payment Service: compensate (issue refund)
Orchestrator → cancels order

Everything visible in one place.

Trade-offs:
+ Flow is explicit and visible — easy to debug
+ Error handling is centralized
+ Easy to add/remove steps
- Orchestrator is a single point of failure
- Services become coupled to orchestrator
- Orchestrator can become a bottleneck

Tools:
Temporal        → most modern, Netflix and Uber use it
Apache Camunda  → enterprise, BPMN-based
AWS Step Functions → AWS ecosystem
Simple option   → one service acts as orchestrator (no extra tooling)

## Choreography vs Orchestration

Choreography:
→ Simple flows, few steps
→ Service independence is critical
→ Team owns each service end-to-end

Orchestration:
→ Complex flows, many steps
→ Error handling is critical
→ Fintech, payment systems — must know state at every moment

PayPay Card → Orchestration. Payment flow must be fully visible and controlled.

## Retry + Idempotency + DLQ — The Three Together

These three always go together. Each is incomplete without the others.

Retry:
Operation failed → might be temporary (network blip, service slow)
→ Try again. But not forever.

Exponential Backoff:
Attempt 1 → immediate
Attempt 2 → wait 1s
Attempt 3 → wait 2s
Attempt 4 → wait 4s
Attempt 5 → failed → send to DLQ

Backing off gives the struggling service time to recover.
Hammering a slow service makes it worse.

Idempotency:
Retry sends the same request multiple times.
The receiver must not process it twice.

Solution: unique ID per operation, stored in Redis.
receiver checks: "have I seen this ID before?"
→ Yes → skip
→ No → process, save ID

Redis with TTL (24h) → fast check, no DB hit, auto-cleanup.

Without idempotency → retry causes duplicate payments, duplicate stock changes.
Without retry → temporary failures become permanent failures.
They complete each other.

Dead Letter Queue:
All retries exhausted → message cannot be processed automatically.
→ Move to DLQ → trigger alert → engineer investigates manually.

Without DLQ → failed message is lost silently.
With DLQ → nothing is lost, human can reprocess when ready.

The full flow:
Operation fails
→ Retry with exponential backoff (automatic recovery)
→ Each retry protected by idempotency (no duplicates)
→ All retries exhausted → DLQ (human intervention)

## At-Least-Once Delivery

Kafka and most message systems guarantee at-least-once delivery.
A message may be delivered more than once — never zero times.

This is why idempotency is non-negotiable with Kafka.
Design every consumer to handle duplicate messages safely.

Exactly-once is possible but expensive — limited to specific scenarios.
In practice: at-least-once + idempotency = safe and practical.

## Interview Checklist
→ Why SAGA? → distributed transaction problem, no shared DB across services
→ Compensation vs rollback? → new operation that reverses effect, not a real undo
→ Choreography vs Orchestration? → event-driven autonomous vs central coordinator
→ When Orchestration? → complex flows, fintech, error handling critical
→ When Choreography? → simple flows, loose coupling critical
→ Retry without idempotency? → duplicate payments, dangerous
→ Idempotency how? → unique ID per operation, check Redis before processing
→ DLQ purpose? → nothing lost, human handles what automation cannot
→ At-least-once delivery? → Kafka default, idempotency mandatory
→ Temporal/Camunda? → workflow engines for orchestration, handle retry/state automatically