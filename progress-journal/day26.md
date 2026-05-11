# Day 26 — May 11, 2026

## What I learned

### System Design continued
- Notification System — event-driven vs scheduled, Kafka partition
  strategy with userId key, push notification services, workers,
  retry with exponential backoff, analytics, cron job for scheduled

- News Feed System — fan-out on write vs read, hybrid approach,
  hotspot problem, consistent hashing with virtual nodes,
  feed retrieval flow, recommendation algorithms overview

### Architecture Patterns
- ADR — Architecture Decision Record
  Context, Decision, Alternatives, Consequences
  Documents why, not just what

- Modular Monolith — single deploy, isolated modules internally
  Strangler Fig — gradual migration without big-bang rewrite
  Hexagonal Architecture — ports and adapters, business logic isolated

### Repository
- Reorganized notes into proper directory structure

## How I feel
Long productive day. Fan-out hybrid approach clicked well.
Modular Monolith connected directly to past experience.
ADR format will change how I answer technology choice questions.
Tired but satisfied.

## Next
- System Design scenarios — Search Autocomplete, YouTube
- AWS basics
- Mock interview preparation
- Algorithm practice