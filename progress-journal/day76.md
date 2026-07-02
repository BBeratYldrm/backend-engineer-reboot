# Day 76 — July 2, 2026

## What I did today

Short code review session. Reviewed two Spring Boot code snippets:

- Identified hardcoded dependency (new Repository()) — replaced with constructor injection
- Refactored field injection to constructor injection
- Added Optional return type for null safety
- Identified SRP violation in OrderService — email notification separated from order saving
- Discussed @Transactional scope — save and email should not share same transaction
- Async event pattern via Kafka for email notification

## Next

- Daily code review practice — broken Spring Boot code format
- SD-07 and SD-08 review before upcoming interviews
- SLI/SLO concepts