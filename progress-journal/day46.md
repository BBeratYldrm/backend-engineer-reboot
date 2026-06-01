# Day 46 — June 1, 2026

## What I studied

### Distributed Systems — [12]

[12.1] CAP theorem recap.
Messaging → AP: availability + partition tolerance, eventual consistency acceptable.
Banking → CP: strong consistency required, availability sacrificed during partition.
P is unavoidable in distributed systems → real choice is always C vs A.

[12.2] ACID vs BASE.
ACID → relational DB, strong consistency, single server.
BASE → distributed systems, eventual consistency, availability first.
Messaging = BASE. Financial systems = ACID.

[12.3] Exactly-once vs at-least-once end-to-end.
At-least-once + clientMessageId idempotency → practical choice for messaging.
Exactly-once → idempotent producer + transactional API → expensive, financials only.

[12.4] Consistent hashing + Raft/KRaft.
Consistent hashing → already covered in Redis Cluster and HBase.
Raft → leader election via majority vote, log replication with majority ack.
Kafka KRaft mode uses Raft since ZooKeeper removal.
Odd number of nodes (3, 5, 7) → prevents tie → prevents split-brain.

[12.5] Resilience patterns.
Circuit breaker → prevents cascade failure, Resilience4j manages automatically.
CLOSED → OPEN (failure rate exceeded) → HALF_OPEN (test) → CLOSED.
Bulkhead → resource isolation per service, named after ship compartments.
→ one slow service cannot consume all threads.
Timeout → free thread after N ms, prevents thread pool exhaustion.
→ timeout errors accumulate → circuit breaker opens.
Rate limiter → token bucket fits messaging best: allows natural burst.
→ fixed window rejects legitimate traffic at window boundaries.
All four patterns work together. Resilience4j implements all of them.

[12.6] Distributed tracing.
API Gateway assigns Trace ID → travels in HTTP headers service to service.
Every service logs Trace ID → search in Kibana → full request journey visible.
Span = time spent in each service for that request.
OpenTelemetry → vendor-neutral instrumentation, preferred over Jaeger/Zipkin directly.
ELK Stack for logs + Jaeger/Grafana Tempo for trace visualization.
RED metrics (Rate, Errors, Duration) → for services.
USE metrics (Utilization, Saturation, Errors) → for infrastructure.
Prometheus collects, Grafana visualizes.
Personal note: have hands-on ELK experience — set up Filebeat from scratch,
configured JSON log parsing, viewed logs in Kibana. This connects directly.

[12.7] Split-brain + reconciliation.
Split-brain: network partition → two sides each elect a leader → data diverges.
Quorum solution: majority vote → Broker 1 alone cannot become leader.
Kafka KRaft uses Raft consensus to prevent split-brain.
Reconciliation: partition heals → losing side discards writes → syncs from winner.
Lambda architecture: speed layer (Kafka, fast, approximate) +
batch layer (Spark/HBase, slow, accurate) → serving layer merges both.

## How I feel

Productive morning — got through most of [12] before afternoon meetings.
Two recruiter calls today interrupted the flow but opened new opportunities.
The resilience patterns section clicked well because of prior Spring Boot experience.
Distributed tracing connected directly to personal ELK Stack experience.
Split-brain and quorum made more sense after understanding Raft in the Kafka section.
Overall [12] is solid. Needs one more pass before interview.

## Next

- Repeat pass: Kafka [5.3] + [5.6], Redis Streams, HBase row key, K8s probes
- Codility design defense [10.2]
- LINE tech specifics [14]
- Java concurrency [2.2] deep dive
- Behavioral questions [13]