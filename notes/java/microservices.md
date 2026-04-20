# [4.1] Microservices Patterns

## Circuit Breaker

Problem: Cascading failure
Service A depends on Service B.
Service B gets slow or down → Service A's threads pile up waiting
→ Service A runs out of threads → Service A also goes down
→ One sick service takes down the whole system

Solution: Circuit Breaker — like an electrical fuse
When too many failures detected → "open" the circuit
Stop sending requests to sick service → protect yourself
Give sick service time to recover

Three states:
CLOSED    → normal, requests flow through
OPEN      → too many failures, stop sending requests, return error immediately
HALF-OPEN → wait period over, send one test request
success → back to CLOSED
failure → back to OPEN

When to use:
+ Service depends on response from another service (payment, fraud detection)
+ Synchronous communication between services

When NOT to use:
- Fire and forget (email, notifications) → use MQ instead
- No dependency on response

Real world:
Revolut: Payment Service → Fraud Detection
If Fraud Detection is slow → Circuit Breaker opens
Payments continue (with cached result or fallback)
Fraud Detection recovers without extra load

Rakuten example:
Reservation → Payment → response needed → synchronous
Reservation → Email → no response needed → ActiveMQ (async)