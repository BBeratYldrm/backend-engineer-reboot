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

