# Day 43 — May 29, 2026

## What I learned

### Java Core — Review & Quiz
- Abstract class vs interface — is-a vs can-do, default methods (Java 8+)
- HashMap internals — hashCode, bucket, treeify at 8, load factor 0.75, rehash at 12
- ConcurrentHashMap — bucket-level locking + CAS, not full map lock
- equals/hashCode contract — same equals must mean same hashCode
- Checked vs unchecked exceptions — compiler forces checked, RuntimeException unchecked
- Errors vs Exceptions — OutOfMemoryError is Error, not Exception
- Functional interfaces — Predicate, Function, Consumer, Supplier (4 types)
- map() vs flatMap() — 1-to-1 vs 1-to-many + flatten
- String pool — string literals share reference, new String() bypasses pool
- Autoboxing — Integer cache -128 to 127, always use equals() not ==

### Java Concurrency — Review & Quiz
- Thread vs Process — process has own memory, threads share memory
- volatile vs synchronized — visibility vs visibility+atomicity
- volatile count++ race condition — 3 steps, not atomic, use AtomicInteger
- AtomicInteger — CAS under the hood, same as ConcurrentHashMap
- Java Memory Model — happens-before rules (5 rules), CPU cache visibility
- ExecutorService vs CompletableFuture — blocking get() vs non-blocking chaining
- ThreadPoolExecutor — corePoolSize, maxPoolSize, queue, rejection policy
- CPU-bound vs IO-bound sizing — cores vs cores/(1-blocking_fraction)
- Deadlock — 4 conditions, lock ordering, tryLock timeout
- Producer-consumer — BlockingQueue, put() blocks when full, take() blocks when empty
- Thread-safe Singleton — double-checked locking + volatile
- Virtual threads — Java 21, ~1KB, millions possible, pinning caveat fixed in Java 24

### JVM & GC
- Heap/Stack/Metaspace — objects, method frames, class metadata
- G1 vs ZGC vs Shenandoah — predictable pauses vs sub-ms concurrent GC
- ZGC for low-latency messaging — concurrent, higher CPU trade-off
- IntelliJ Profiler = JFR under the hood — used in previous role
- -Xms = -Xmx for stability, -Xmx ~75% of container limit

### Spring Boot Internals — Review & Quiz
- Auto-configuration — @EnableAutoConfiguration, AutoConfiguration.imports, @Conditional*
- @ConditionalOnMissingBean — prevents default overriding custom bean
- Bean lifecycle — 8 steps, @PreDestroy for graceful Kafka shutdown
- Constructor vs field injection — testability, immutability, final fields, @RequiredArgsConstructor
- @Transactional — AOP proxy, self-invocation trap, checked vs unchecked rollback
- Propagation — REQUIRED (default), REQUIRES_NEW, NESTED
- @RestController vs @Controller — JSON vs HTML, @ResponseBody included
- Hibernate Fetch Types — LAZY default for @OneToMany, EAGER for @ManyToOne, N+1 problem
- Spring WebFlux vs MVC — event-loop vs thread-per-request
- Circuit breaker — CLOSED/OPEN/HALF_OPEN, Resilience4j, fallback method
- Armeria — LINE's async RPC framework, Netty-based, HTTP/2 + gRPC

## How I feel
Long and intense day. A lot of ground covered across Java core, concurrency, JVM, and Spring.
Most topics marked for review — knowledge is there but needs more activation reps.

## Next
- Morning review — all ⚠️ topics from today (String pool, autoboxing, JMM,
  virtual threads, auto-configuration, bean lifecycle, @Transactional,
  WebFlux, circuit breaker)
- Continue from [5] Kafka — strongest area, momentum expected
- [7] HBase — brand new, needs dedicated time
- Round 1 prep — API design, gRPC basics, messaging system API
- Round 2 prep — concurrent task manager implementation practice