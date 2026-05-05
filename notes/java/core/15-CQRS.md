# [4.2] CQRS — Command Query Responsibility Segregation

## Keywords
cqrs · command · query · read-model · write-model · eventual-consistency ·
event-driven · read-replica · elasticsearch · strong-consistency · segregation

---

## The Problem

Same service handling both reads and writes:
- Heavy reads slow down writes (connection pool contention)
- Write model optimized for ACID, not for complex queries
- Read requirements differ from write requirements
  (filtering, sorting, full-text search)

Rakuten connection:
Reservation table — writes: insert/update per booking
— reads: shop search, filters, sorting, distance
Same DB, same table, competing for same connection pool.

## What CQRS Does

Separate the read model from the write model.

Write Side (Command):
→ Create reservation
→ Update reservation
→ Cancel reservation
→ Optimized for consistency and integrity

Read Side (Query):
→ Search shops
→ Get reservation details
→ List user reservations
→ Optimized for speed and flexibility

## Two DB Approach

Write DB: MySQL / PostgreSQL
→ ACID guaranteed
→ Source of truth
→ Normalized, consistent

Read DB: Elasticsearch / Redis / separate replica
→ Optimized for read patterns
→ Denormalized, fast
→ Can be rebuilt from write DB events

Synchronization via events:
Write DB updated
→ event published (Kafka)
→ Read Model Consumer listens
→ updates Read DB

## Eventual Consistency

Two DBs are not updated simultaneously.
Gap between write and read update — milliseconds to seconds.

t=0ms:   MySQL updated
t=50ms:  event published to Kafka
t=200ms: Elasticsearch updated

User searches at t=100ms → sees slightly stale data
User searches at t=300ms → sees fresh data

This is eventual consistency — not immediately consistent, but will be.

When acceptable:
+ Shop search → 200ms stale is fine
+ Social media likes → 1 second stale is fine
+ Product catalog → fine

When NOT acceptable:
- Bank balance → must be strong consistency
- Payment status → must be current
- Inventory (oversell risk) → must be current

## Trade-offs

Without CQRS:
+ Simple — one model, one DB
+ No synchronization complexity
- Read and write compete for same resources
- Hard to optimize for both simultaneously
- Complex queries slow down the whole system

With CQRS:
+ Read and write independently optimized
+ Read DB can be rebuilt anytime from events
+ Scale read and write sides independently
- Eventual consistency — stale reads possible
- More moving parts — two DBs, event pipeline
- Complexity increases significantly

## When to Use CQRS

Use when:
+ Read and write loads are very different
+ Complex read queries slowing down writes
+ Need different storage for reads (Elasticsearch for search)
+ High scale — read and write must scale independently

Do NOT use when:
- Simple CRUD application — massive overkill
- Small team, simple domain — complexity kills productivity
- Strong consistency required everywhere

## Connection to Other Patterns

CQRS + Event Sourcing:
Write side stores events, not current state.
Read side builds its model from those events.
Can rebuild read model at any time by replaying events.

CQRS + SAGA:
SAGA manages distributed transactions on write side.
CQRS separates read concerns from those transactions.

CQRS + Elasticsearch:
Classic combination — write to MySQL, read from Elasticsearch.
Rakuten: reservation data in MySQL, shop search via Elasticsearch.

## Interview Checklist
→ What is CQRS? → separate read and write models, each optimized differently
→ Why two DBs? → different read/write requirements, scale independently
→ How to sync? → events via Kafka, read model consumer updates read DB
→ Eventual consistency trade-off? → stale reads possible, acceptable for most reads
→ When not to use? → simple CRUD, small apps, strong consistency everywhere required
→ CQRS + Elasticsearch? → write MySQL, event to Kafka, consumer updates Elasticsearch