# Modular Monolith

## Keywords
modular-monolith · microservice · bounded-context · module-isolation ·
strangler-fig · package-structure · domain-boundaries · team-topology

---

## What Is Modular Monolith

Single deployable unit — but internally divided into isolated modules.
Not a big ball of mud. Not microservices either.

// Big ball of mud — everything mixed
com.company.service/
ServiceHandler.java  // bookings, payments, notifications all here

// Modular Monolith — clear boundaries
com.company.service/
booking/
BookingService.java
payment/
PaymentService.java
notification/
NotificationService.java

Modules do not directly call each other's internals.
Communication through defined interfaces — like microservices, but in-process.

---

## Real World Pattern

Multiple services (service-a, service-b, service-c) → separate repos, separate deploys.
This is microservice architecture at the macro level.

Inside each service → large codebase with multiple domains.
Each service itself can benefit from Modular Monolith structure internally.

Microservice level:  service-a ←→ service-b ←→ service-c
Modular Monolith:    service-a internally → booking/ payment/ notification/

---

## When to Use What

Modular Monolith:
→ Early stage — domain boundaries not yet clear
→ Small-medium team — microservice overhead not justified
→ Good stepping stone toward microservices
→ Domain boundaries exist but independent scaling not needed yet

Microservice:
→ Domain boundaries are clear and stable
→ Teams grew — need independent deployments
→ Different modules need different scaling
→ Organizational structure maps to services (Conway's Law)

---

## Benefits of Modular Monolith

+ Clear boundaries — payment code cannot leak into booking
+ Team division — one team owns one module
+ Testability — each module tested independently
+ Easier migration path — module already isolated, extract to service when ready
+ Simpler operations — single deploy, no distributed system complexity

---

## Strangler Fig Pattern

How to migrate from Big Ball of Mud to Modular Monolith or Microservice.
Do not rewrite everything at once — strangle the old system gradually.

Step 1: New features → new module with clean boundaries
Step 2: Old code → gradually migrate to modules
Step 3: When module is stable → optionally extract to microservice

Like a strangler fig tree — new system grows around the old one,
eventually replacing it without a big-bang rewrite.

---

## Hexagonal Architecture (Ports & Adapters)

Complements Modular Monolith well.

Business logic at the center — knows nothing about databases, APIs, or queues.
Ports: interfaces the core exposes or requires.
Adapters: implementations connecting core to the outside world.

          HTTP Request
               ↓
         [REST Adapter]
               ↓
         [Port: Input]
               ↓
        [Business Logic]  ← knows nothing about Spring, DB, Kafka
               ↓
        [Port: Output]
               ↓
      [DB Adapter / Kafka Adapter]

Benefit: swap adapters without touching business logic.
Test business logic without starting full framework context.

---

## Interview Checklist
→ Modular Monolith vs Microservice? → single deploy vs independent deploys
→ When Modular Monolith? → early stage, unclear boundaries, small team
→ Strangler Fig? → gradual migration, new code in modules, strangle old code
→ Hexagonal Architecture? → business logic isolated, ports and adapters
→ Real example? → each service internally modular, services together = microservices