# Day 47 — June 2, 2026

## What I studied

### Java Core — [2.1]

Went through all bullets sequentially.
OOP, abstract class vs interface, equals/hashCode contract, HashMap internals — solid.
ConcurrentHashMap bucket-level locking + CAS — connected to ForkJoinPool and AtomicInteger.
String pool, autoboxing, generics, sealed classes, pattern matching, serialization — new or weak areas, noted for review.
Stream API lazy evaluation, map vs flatMap, when NOT to use streams — noted for review.
Optional — use cases and anti-patterns — noted for review.
Comparable vs Comparator — clear.

### Java Concurrency — [2.2]

Thread vs Process — solid, Chrome tab analogy.
volatile vs synchronized vs ReentrantLock — solid.
volatile vs Atomic — CAS mechanism connection made.
Java Memory Model + happens-before — weak, noted for review.
double-checked locking + volatile — why reordering is prevented — solid after explanation.
CompletableFuture — parallel reads vs synchronous writes trade-off — solid.
CompletableFuture + @Transactional trap — new, noted for review.
CompletableFuture default ForkJoinPool.commonPool — new, noted for review.
ThreadPoolExecutor sizing formula — new, noted for review.
ForkJoinPool — work stealing, when to use — noted for review.
Producer-Consumer + BlockingQueue — solid.
CountDownLatch — solid, modern alternative is CompletableFuture.allOf().
Deadlock prevention — solid.

### JVM & GC — [2.3]

G1 vs ZGC vs Shenandoah — ZGC for low-latency messaging, sub-ms pauses.
Heap leak diagnosis — step-by-step process noted for review.
Heap vs Stack vs Metaspace — basics clear, details noted for review.
-Xmx = 75% of container memory limit — OOMKiller connection — noted for review.

### Virtual Threads — [2.4]

Java 21 virtual threads — ~1KB vs ~1MB platform thread.
I/O blocking → JVM unmounts, no platform thread wasted.
Java 24 JEP 491 — synchronized no longer pins virtual thread.

### Spring Boot — [4] partial

Auto-configuration — solid.
Bean lifecycle 6 steps — solid.
Constructor vs field injection — solid, SOLID connection made.
IoC vs DI distinction — solid after explanation.
@Autowired, @Primary, @Qualifier — solid, polymorphism connection made.
@Transactional — proxy, self-invocation trap, propagation — solid.
@RestController vs @Controller, PathVariable vs RequestParam — solid.
Hibernate Fetch Types, Profiles, Actuator, Armeria — not covered yet.

## How I feel

Long day with interruptions — recruiter meetings, new opportunity emerged, meals.
Could not finish the full repeat pass as planned.
Many weak spots surfaced in Java Core and Concurrency — expected for topics not used daily.
The important thing: gaps are now visible and listed. Tomorrow these get closed.

## Next

- Review weak spots list below
- Continue [4] Spring Boot — Hibernate, Profiles, Actuator, Armeria
- Move to [8] + [9] live coding practice