# Spring Boot — Core Concepts

## IoC Container
Spring manages object creation and dependencies.
We don't new() our dependencies — Spring does it.

@Service, @Component, @Repository → "I am a Spring bean, manage me"

On startup Spring:
→ Scans all annotated classes
→ Creates instances
→ Injects dependencies via constructor

## Dependency Injection — 3 ways

→ Constructor injection (preferred)
private final PaymentService paymentService;
public OrderService(PaymentService p) { this.paymentService = p; }

→ Field injection (avoid)
@Autowired
private PaymentService paymentService;

→ Setter injection (rare, optional dependencies only)
@Autowired
public void setPaymentService(PaymentService p) { ... }

## Why constructor injection wins
→ Supports final fields — immutable
→ Testable without Spring — just pass mock in constructor
→ Dependencies are explicit — visible in constructor signature
→ Circular dependency detected at startup, not runtime

## The question I ask myself
"Can I test this class without starting Spring?"
→ Constructor injection: Yes
→ Field injection: No

## Why @Autowired exists but should be avoided

Historical context:
→ Created to replace verbose XML configuration
→ Was revolutionary in 2004-2010 era
→ Made DI much easier

Why avoid now:
→ Too easy to add dependencies — class grows silently
→ SRP violations hide easily — no one notices
→ Constructor injection makes pain visible — and that's good

Modern approach:
→ Spring 4.3+: single constructor auto-detected, no @Autowired needed
→ Lombok @RequiredArgsConstructor: generates constructor automatically
→ Result: clean, immutable, testable — zero boilerplate

## Bean Scopes

→ Singleton (default): one instance for entire application
All injections share the same object

→ Prototype: new instance every time it's injected
Rare — use when each caller needs own state

→ Request: new instance per HTTP request
Useful for request-scoped data (current user, request id)

## Critical rule — Singleton must be stateless
Singleton beans are shared across all threads.
Storing state in a Singleton = race condition waiting to happen.

- Wrong:
@Service // Singleton
public class OrderService {
private int count = 0; // shared across all threads!
}

+ Right:
@Service
public class OrderService {
// no state — just behavior
// if state needed → database or Redis
}

## The question I ask myself
"Does this class hold any instance variables that change?"
→ Yes → move state out (DB, cache, method params)
→ No → safe as Singleton