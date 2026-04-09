# Phase 2 — Spring Ecosystem

---

# [2.1] Spring Core

## [2.1.1] IoC Container & Dependency Injection

Spring manages object creation and dependencies.
We don't new() our dependencies — Spring does it.

@Service, @Component, @Repository → "I am a Spring bean, manage me"

On startup Spring:
→ Scans all annotated classes
→ Creates instances
→ Injects dependencies via constructor

## DI — 3 ways

+ Constructor injection (preferred)
  private final PaymentService paymentService;
  public OrderService(PaymentService p) { this.paymentService = p; }

- Field injection (avoid)
  @Autowired
  private PaymentService paymentService;

- Setter injection (rare, optional dependencies only)
  @Autowired
  public void setPaymentService(PaymentService p) { ... }

## Why constructor injection wins
→ Supports final fields — immutable
→ Testable without Spring — just pass mock in constructor
→ Dependencies are explicit — visible in constructor signature
→ Circular dependency detected at startup, not runtime

The question I ask myself:
"Can I test this class without starting Spring?"
+ Constructor injection: Yes
- Field injection: No

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

---

## [2.1.2] Bean Scopes

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
  @Service
  public class OrderService {
  private int count = 0; // shared across all threads!
  }

+ Right:
  @Service
  public class OrderService {
  // no state — just behavior
  // if state needed → database or Redis
  }

The question I ask myself:
"Does this class hold any instance variables that change?"
→ Yes → move state out (DB, cache, method params)
→ No → safe as Singleton

---

## [2.1.3] Bean Lifecycle

Three stages:
→ Instantiation: Spring creates the object (new)
→ Initialization: @PostConstruct runs — dependencies are ready
→ Destruction: @PreDestroy runs — app is shutting down

@PostConstruct — use for:
+ Loading cache on startup
+ Validating configuration
+ Opening connections

@PreDestroy — use for:
+ Closing connections
+ Flushing cache
+ Releasing resources

The question I ask myself:
"Do I need something to happen right after the bean is ready?"
+ Yes → @PostConstruct
  "Do I need cleanup when app shuts down?"
+ Yes → @PreDestroy

---

## [2.1.4] Proxy Mechanism

Spring wraps beans in a proxy — caller never talks to bean directly.
Proxy adds behavior: transactions, security, logging.

Two types:

+ JDK Proxy — when bean implements an interface
  Proxy wraps only interface methods
  Preferred when interface exists

+ CGLIB Proxy — when no interface exists
  Proxy extends the class (creates subclass)
  Default in Spring Boot for @Service, @Component

Critical rule — CGLIB beans cannot be final:
CGLIB creates a subclass → final class cannot be extended → error

The question I ask myself:
"Why is my @Transactional not working?"
→ Is it a self invocation? (same class call)
→ Is the class final? (CGLIB cannot proxy it)
→ Is the method private? (proxy cannot override it)

---

## [2.1.5] AOP — Aspect Oriented Programming
Note: Know the concept, don't need to write it from scratch.

Problem it solves:
Cross-cutting concerns — logging, security, transactions —
repeated across many methods. AOP centralizes them.

How it works:
→ You define an Aspect (where and what)
→ Spring applies it automatically via proxy

Key terms:
→ Aspect: class containing the cross-cutting logic
→ Advice: what to do (@Before, @After, @Around)
→ Pointcut: which methods to target

Real usage:
+ Logging — log every service method automatically
+ Security — @PreAuthorize works via AOP
+ Transactions — @Transactional is AOP under the hood
+ Performance monitoring — measure method execution time

Anti-pattern:
- Never put business logic in an Aspect
- Aspects are for infrastructure concerns only

---

# @Transactional
Note: Technically belongs under [2.4] Spring Data, but covered
here because it directly builds on [2.1.4] Proxy Mechanism.

## What it does
Wraps method in a transaction.
Either everything succeeds (commit) or nothing happens (rollback).

## How Spring does it — Proxy
Spring puts a proxy between caller and your service.
Proxy opens transaction → your method runs → Proxy commits or rolls back.

## Pitfall 1 — Self Invocation
Calling @Transactional method from same class bypasses proxy.
Proxy never knows the call happened → no transaction.

+ Fix: move @Transactional method to a separate service

## Pitfall 2 — Checked vs Unchecked
Unchecked (RuntimeException) → automatic rollback
Checked (IOException, SQLException) → NO rollback by default

- Why: Checked means "you expected this" — Spring says "you handle it"
+ Fix: @Transactional(rollbackFor = Exception.class)

## Checked vs Unchecked — quick reference
Checked   → external problems (file, network, DB) — must catch or declare
Unchecked → programming errors (null, wrong index) — no forced handling

The question I ask myself:
"Can this method fail silently and leave data inconsistent?"
+ Yes → @Transactional
+ Is it called from same class? → move to separate service
+ Can it throw checked exception? → add rollbackFor

---

---

# [2.2] Spring Boot

## [2.2.1] Auto-configuration

Spring Boot automatically configures beans based on:
→ What's on the classpath (which starters you added)
→ What's in application.yml (which properties you set)

How it works — @ConditionalOnClass:
Spring Boot ships with hundreds of auto-configuration classes.
Each one checks conditions before activating.

Example — DataSource auto-configuration:
+ Is DataSource.class on classpath? (comes with JPA starter)
+ Is spring.datasource.url set in yml?
+ Both true → Spring auto-creates DataSource, EntityManager, TransactionManager

You never write this — Spring Boot does it.
You just add the starter + set properties.

Starters are bundles:
spring-boot-starter-data-jpa brings:
→ spring-data-jpa
→ hibernate
→ jdbc
→ HikariCP (connection pool)

How to debug auto-configuration:
Add to application.yml:
logging.level.org.springframework.boot.autoconfigure: DEBUG
Spring will log every condition check — what activated and why.

The question I ask myself:
"Why is my bean not being created automatically?"
→ Is the right starter in pom.xml?
→ Is the required property set in application.yml?
→ Turn on DEBUG logging to see what Spring is checking

---

## [2.2.2] Profiles & Config

Problem:
Different environments need different configurations.
Changing yml manually per environment = error-prone and risky.

Solution — profile-specific yml files:
application.yml          → shared config
application-local.yml    → local environment
application-stg.yml      → staging
application-prod.yml     → production

Activate:
spring.profiles.active: local   (in yml)
--spring.profiles.active=prod   (at startup)
SPRING_PROFILES_ACTIVE=prod     (environment variable — Kubernetes)

Security rule:
+ Local → hardcoded credentials acceptable
+ STG/Prod → always use environment variables

application-local.yml:
username: root
password: 123

application-prod.yml:
username: ${DB_USERNAME}
password: ${DB_PASSWORD}

The question I ask myself:
"Is this config value environment-specific?"
+ Yes → move to profile-specific yml
+ Is it a credential? → environment variable in non-local envs
---

## [2.2.3] Actuator

Problem:
How do you know if your production app is healthy?
Is the DB connected? Is memory running out?

Solution — Actuator exposes built-in endpoints:
/actuator/health     → is app healthy? DB, Redis, MQ connections
/actuator/prometheus → metrics for Prometheus/Grafana
/actuator/heapdump   → memory snapshot for debugging

Configure which endpoints to expose:
management:
endpoints:
web:
exposure:
include: health, prometheus, heapdump

Real usage at Rakuten:
Prometheus scraped /actuator/prometheus
Grafana visualized the metrics
Alerts fired when thresholds were breached

The question I ask myself:
"How do I monitor this service in production?"
+ Add Actuator + expose prometheus endpoint
+ Let Prometheus scrape it
+ Build Grafana dashboard on top

