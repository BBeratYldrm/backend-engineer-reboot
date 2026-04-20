# [4.1] Microservices Patterns

## Circuit Breaker

Problem: Cascading failure
Service A depends on Service B.
Service B gets slow or down → Service A's threads pile up waiting
→ Service A runs out of threads → Service A also goes down
→ One sick service takes down the whole system

The key insight: it's not just about Service A protecting itself.
It also gives Service B time to recover — no more incoming load.
Like redirecting ambulances to another hospital so the full one can breathe.

Solution: Circuit Breaker — like an electrical fuse
When too many failures detected → "open" the circuit
Stop sending requests to sick service → protect yourself
Give sick service time to recover

Three states:
CLOSED    → normal, requests flow through
OPEN      → too many failures, stop sending requests, return error immediately
→ return fallback or cached result instead of waiting
HALF-OPEN → wait period over, send one test request
success → back to CLOSED
failure → back to OPEN

When to use:
+ Service depends on response from another service
+ "This step cannot proceed without a response" — synchronous dependency
+ Payment, fraud detection, inventory check

When NOT to use:
- Fire and forget (email, notifications) → use MQ instead
- Response not needed to continue the flow

The question I ask myself:
"Can the next step start without waiting for a response?"
+ No → synchronous → protect with Circuit Breaker
+ Yes → async → use Message Queue

Real world:
Payment Service → Fraud Detection
If Fraud Detection is slow → Circuit Breaker opens
Payments continue (with cached result or fallback)
Fraud Detection recovers without extra load

Rakuten example:
Reservation → Payment → response needed → synchronous → Circuit Breaker would help
Reservation → Email → no response needed → ActiveMQ (async) → no Circuit Breaker needed

Implementation — Resilience4j (works with or without Spring)

With Spring:
@CircuitBreaker(name = "fraudDetection", fallbackMethod = "fallback")
public Result check(Payment p) { ... }

public Result fallback(Payment p, Exception e) {
return Result.PENDING; // cached or default
}

Without Spring (pure Java):
CircuitBreaker cb = CircuitBreakerRegistry.ofDefaults()
.circuitBreaker("fraudDetection");
Supplier<Result> supplier = CircuitBreaker.decorateSupplier(cb, () -> check(p));

Config:
- failureRateThreshold: 50%    → OPEN when 50% of requests fail
- waitDurationInOpenState: 30s → wait 30s before HALF-OPEN
- permittedCallsInHalfOpen: 1  → send 1 test request

---

## Saga Pattern

Problem:
@Transactional only works within one DB.
In microservices, each service has its own DB.
No global rollback possible across services.

The key insight:
"Is the next step dependent on this step completing?"
+ Yes → they are in the same Saga chain → need compensating transactions
+ No → decouple with async messaging (MQ/Kafka)

Solution: Saga
Break the transaction into steps.
Each step has a compensating transaction (undo operation).
If any step fails → run compensating transactions in reverse order.

Example — money transfer :
Step 1: debit Berat's account     → undo: refund Berat
Step 2: credit John's account     → undo: debit John
Step 3: create transfer record    → undo: delete record
Step 4: send notification         → undo: not needed (fire and forget)

If Step 3 fails:
→ undo Step 2 (debit John back)
→ undo Step 1 (refund Berat)
→ money returned, no inconsistency ✅

Real world examples:
Amazon order:
Stock reserve → Payment → Shipping → Email
Stock and Payment are dependent — Saga
Email is independent — just MQ, no Saga needed

Rakuten reservation (if it were microservices):
Payment → Reservation Record → Email
Payment and Record are dependent → Orchestration Saga
Email is independent → ActiveMQ, fire and forget

Two types:

Orchestration:
→ Central Saga Orchestrator coordinates everything
→ Tells each service: "do this, now do that"
→ If failure → orchestrator sends compensate commands in reverse
→ Easier to debug — single place to look
→ Risk: orchestrator becomes a single point of failure
→ Best for: complex flows, strict ordering, financial transactions

Choreography:
→ No central coordinator
→ Each service publishes events, others listen and react
→ "payment.completed" event → Transfer Service starts automatically
→ If failure → publish "payment.failed" → previous services compensate
→ Works naturally with Kafka (event streaming)
→ More scalable, truly independent services
→ Harder to debug — where did the event get lost?
→ Best for: loosely coupled services, high scalability needs

How Kafka fits in Choreography:
Payment Service    → publishes "payment.debited" to Kafka topic
Transfer Service   → listens to "payment.debited" → creates record
→ publishes "transfer.created"
Notification Svc   → listens to "transfer.created" → sends notification

If Transfer Service fails:
→ publishes "transfer.failed" to Kafka
Payment Service listens → refunds Berat → publishes "payment.refunded"

The question I ask myself:
"Do I need a central controller or can services react to events?"
+ Complex flow, strict order, financial → Orchestration
+ Independent services, scalability, event-driven → Choreography