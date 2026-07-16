# Day 88 — July 14, 2026

## What I did today

Continued interview preparation, focusing on turning theoretical concepts into narrative-style answers rather than plain definitions — the goal being to sound like someone who has actually used these patterns, not just studied them.

Worked through the Saga pattern in depth: the core distributed-transaction problem (why classic ACID/2PC doesn't scale across microservices), the compensating-transaction concept, and the distinction between choreography-based and orchestration-based Saga — with orchestration framed as the natural choice for payment flows where centralized visibility over the transaction state matters.

Went through the Outbox pattern as the companion piece to Saga — why writing to a database and publishing a message can't be a single atomic operation across two different systems, and how writing to an outbox table within the same DB transaction solves that. Compared the two relay mechanisms: polling (simple, but adds latency and DB load) versus CDC-based approaches like Debezium (lower latency, reads directly from the transaction log). Walked through a full request lifecycle end-to-end: request arrives at an orchestrator, first local transaction commits with an outbox entry, the event gets relayed and published, the next service consumes it and does its own local transaction, and either the flow completes or a compensating transaction unwinds the prior steps.

Rehearsed OAuth2 (Authorization Code Flow) and JWT explanations in interview-ready form — the reasoning behind using Authorization Code Flow specifically, the purpose of the state parameter for CSRF protection, JWT structure and why the payload is only encoded and not encrypted, and practical security considerations: token storage in HttpOnly cookies, short-lived access tokens paired with revocable refresh tokens, and enforcing HTTPS end-to-end.

Reviewed Kubernetes deployment reliability concepts in plain, confidence-building terms — the distinction between readiness probes (controls whether traffic is routed to a pod) and liveness probes (triggers automatic restart on deadlock/failure), rolling update parameters (maxUnavailable, maxSurge) for zero-downtime releases, and graceful shutdown via preStop hooks so in-flight requests finish before pod termination. Also reviewed the most common kubectl commands used day to day for monitoring and debugging.

Prepared a simple, honest way to talk about cloud infrastructure experience without overstating it — mapping internal cloud platform experience (compute, managed database, object storage, network isolation) to the equivalent public cloud concepts, so the underlying architectural reasoning comes through even without hands-on public cloud production experience.

## Next

- Final review of prepared material
- Rest before the interview