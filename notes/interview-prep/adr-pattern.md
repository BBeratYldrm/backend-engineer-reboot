# Architecture Decision Records (ADR)

## Keywords
adr · architecture-decision · trade-off · context · alternatives · consequences

---

## What Is ADR

A short document capturing an important architectural decision.
Not just "what we decided" but "why we decided it and what we considered."

Format:
# ADR-XXX: Title

## Status
Proposed / Accepted / Deprecated

## Context
What problem are we solving? What constraints exist?

## Decision
What did we decide?

## Alternatives considered
What else did we evaluate and why did we reject it?

## Consequences
+ What becomes easier?
- What becomes harder or riskier?

---

## Why ADR Matters

Without ADR:
"Why are we using ActiveMQ?" → "I don't know, it was already there"

With ADR:
"Why ActiveMQ?" → "Small scale, Java ecosystem, team familiarity.
Kafka was overkill at the time. If scale grows, we'd revisit."

In interviews:
Every technology choice you made → think in ADR format.
Context → Decision → Alternatives → Trade-offs.

---

## Real Example — ActiveMQ Decision

Context:
Async messaging needed for email/SMS notifications.
Team is Java-focused, simple solution preferred, budget-conscious.

Decision: ActiveMQ

Alternatives considered:
Kafka → powerful but complex setup, overkill for this scale
RabbitMQ → good option but extra learning curve for team

Consequences:
+ Fast implementation, team already familiar
+ Open source, no licensing cost
- Not suitable if scale grows significantly
- Limited replay capability vs Kafka

---

## Interview Tip

When asked "why did you use X?":
Don't just say "because we used it."
Say: "Given our constraints (scale, team, timeline),
X was the right fit. We considered Y and Z but rejected them because..."

This is ADR thinking — and it's what separates senior engineers.