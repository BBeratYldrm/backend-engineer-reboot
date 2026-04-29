# [2.1] Spring Core

## Dependency Injection — The Problem First

Without DI:
public class OrderService {
private MySQLOrderRepository repository = new MySQLOrderRepository();
}
→ OrderService knows HOW the repository is created
→ Switch to PostgreSQL → open OrderService and change it
→ Write a unit test → need a real DB, can't mock

With DI:
public class OrderService {
private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }
}
→ OrderService only knows the interface
→ Spring decides which implementation to inject at runtime
→ Tests: inject a mock, no real DB needed

DI is the mechanism that makes DIP real.
DIP says "depend on abstractions" — DI is how you do it in practice.

## IoC Container

IoC = Inversion of Control.
Normal: you create objects, you manage them.
IoC: Spring creates objects, Spring manages them, Spring gives them to you.

How Spring builds the container on startup:
1. Scan all classes annotated with @Component, @Service, @Repository, @Controller
2. Register them as Bean definitions
3. Build a dependency graph — who needs what
4. Start from leaves — create beans with no dependencies first
5. Work up the graph — inject dependencies as beans become available
6. Store every singleton in an internal registry

Rakuten example:
@Repository PartsShopRepository  → no dependencies → created first
@Service ShopSearchService        → needs PartsShopRepository → created second, injected

The container is a box.
One PartsShopRepository instance inside.
One ShopSearchService instance inside, holding a reference to the repository.
Anyone who needs ShopSearchService gets the same instance from the box.

## Constructor vs Field Injection

Constructor injection — the right way:
@Service
@RequiredArgsConstructor
public class OrderService {
private final OrderRepository repository;
}

Field injection — avoid:
@Service
public class OrderService {
@Autowired
private OrderRepository repository;
}

Why constructor injection wins:

1. Testability without Spring:
   new OrderService(mockRepository) → works
   Field injection: Spring must be present to inject → slower, heavier tests

2. Dependencies are visible and explicit:
   Constructor shows exactly what this class needs
   Field injection: hidden — 10 @Autowired fields can pile up unnoticed

3. Immutability:
   final fields → cannot be reassigned after construction → thread-safe

4. SRP signal:
   Constructor with 6 parameters → obvious smell → class does too much
   Field injection hides this — easy to keep adding without noticing

@RequiredArgsConstructor (Lombok) generates the constructor for all final fields.
No boilerplate, clean code, same guarantees.

## Circular Dependency

A needs B, B needs A.

@Service public class A { public A(B b) { ... } }
@Service public class B { public B(A a) { ... } }

With constructor injection → Spring fails at startup. Good — caught early.
With field injection → Spring tries to resolve it, sometimes silently succeeds.
That silent success is dangerous — the problem hides.

Circular dependency is always a design problem.
Fix: extract shared logic into a third class, or reconsider responsibilities.

## Bean Lifecycle

1. Instantiation     → constructor runs
2. Dependency Injection → dependencies are injected
3. @PostConstruct    → initialization logic runs here
4. Ready             → bean is in use
5. @PreDestroy       → cleanup before shutdown
6. Destroyed         → bean removed

@Service
public class CacheService {

    @PostConstruct
    public void init() {
        // dependencies are already injected here
        // safe to use them
        cache = loadFromDatabase();
    }

    @PreDestroy
    public void cleanup() {
        // app is shutting down
        // close connections, clear resources
        cache.clear();
    }
}

Rakuten connection:
@PostConstruct → load zip code list into cache on startup
@PreDestroy → cleanly close ActiveMQ connections before shutdown

## Bean Scopes

Singleton — default:
@Service
public class OrderService { }
→ One instance for the entire application
→ Same instance given to everyone who needs it
→ Never store mutable state in a singleton bean — shared across all threads → race condition

Prototype:
@Scope("prototype")
@Component
public class ReportGenerator { }
→ New instance every time it is requested
→ For stateful objects that need a fresh start each use

Request (web):
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component
public class RequestContext { }
→ New instance per HTTP request
→ Destroyed when request completes

Scope mismatch — the hidden trap:
Singleton bean depends on a prototype bean.
Prototype is injected once at startup → never refreshed.
Despite being @Scope("prototype"), always the same instance is used.

Fix: use ApplicationContext.getBean() each time, or @Lookup annotation.

## Proxy Mechanism — Critical

Spring does not give you the real object.
What you get from @Autowired is a proxy — a wrapper Spring created around your class.

Why proxy?
Spring needs to intercept method calls to apply:
@Transactional → start/commit/rollback transaction
@Cacheable     → check cache before running method
@Async         → run method in a different thread
@Retryable     → retry on failure

The call flow:
You → proxy.placeOrder() → [Spring intercepts here] → real OrderService.placeOrder()

Two proxy types:

JDK Dynamic Proxy:
→ Used when the bean implements an interface
→ Proxy implements the same interface

CGLIB Proxy:
→ Used when there is no interface (concrete class only)
→ Proxy extends the class (subclass)
→ Spring Boot uses CGLIB by default

## Self-Invocation Bug — Most Critical

@Service
public class OrderService {

    @Transactional
    public void placeOrder(Order order) {
        saveOrder(order);
        sendNotification(order); // calling own method
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendNotification(Order order) {
        // expected: runs in a separate transaction
        // actual: @Transactional does nothing here
    }
}

Why it fails:
Inside placeOrder(), the call is:
this.sendNotification(order)

"this" is the real OrderService object — not the proxy.
The proxy has no visibility into this internal call.
No proxy → no interception → @Transactional on sendNotification is ignored.

External call path:
proxy.placeOrder() → proxy intercepts → real.placeOrder()
↓
this.sendNotification() → no proxy → annotation dead

Fix — extract to a separate bean:
@Service
@RequiredArgsConstructor
public class OrderService {
private final NotificationService notificationService;

    @Transactional
    public void placeOrder(Order order) {
        saveOrder(order);
        notificationService.sendNotification(order); // external call → proxy works
    }
}

@Service
public class NotificationService {
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void sendNotification(Order order) {
// now runs in a separate transaction correctly
}
}

This is always the right fix.
It also fixes SRP — notification is now its own concern.

The question I ask myself:
"Is this @Transactional / @Cacheable / @Async not working despite the annotation being there?"
→ First thing to check: is this a self-invocation?

## AOP — Cross-Cutting Concerns

AOP = Aspect-Oriented Programming.
Goal: separate concerns that cut across many classes — logging, security, transaction, caching.
These have nothing to do with business logic but must run everywhere.

@Aspect
@Component
public class LoggingAspect {

    @Around("@annotation(Loggable)")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().getName();
        log.info("Starting: {}", method);
        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed(); // run the real method

        log.info("Finished: {} in {}ms", method, System.currentTimeMillis() - start);
        return result;
    }
}

AOP terms:
Aspect   → the cross-cutting concern itself (logging, transaction)
Advice   → what to do (@Around, @Before, @After, @AfterReturning, @AfterThrowing)
Pointcut → where to apply it (which methods, which annotations)
JoinPoint → the moment advice runs (a method call)

@Transactional is Spring's own AOP.
Spring creates a transaction Aspect, proxy intercepts every call, manages begin/commit/rollback.
You write zero transaction code — AOP handles it.

Anti-pattern — business logic in AOP:
@Around("execution(* OrderService.placeOrder(..))")
public Object applyDiscount(ProceedingJoinPoint jp) {
// business logic does not belong here
}
AOP is for infrastructure concerns only.
Business rules belong in the service, visible and testable.

Interview checklist:
→ What is IoC? → Control of object creation inverted to the container
→ Constructor vs field injection? → testability, visibility, immutability, SRP signal
→ What is a proxy? → wrapper Spring creates to intercept method calls
→ Why does @Transactional not work sometimes? → self-invocation, no proxy on internal calls
→ What is a singleton bean's biggest risk? → mutable state shared across threads
→ What is AOP for? → cross-cutting concerns, not business logic