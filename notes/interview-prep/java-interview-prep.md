# Java Interview Prep — Technical Questions

## Keywords
abstract-class · interface · hashmap · equals-hashcode · synchronized · volatile ·
deadlock · transactional · kafka · thread · executorservice · countdownlatch ·
reentrantlock · dependency-injection · bean-lifecycle

---

## Abstract Class vs Interface

Interface:
- Method signatures only (Java 8+ allows default/static methods)
- A class can implement multiple interfaces
- Use when: defining a capability — "can do"

Abstract Class:
- Can have both abstract and concrete methods
- Can have fields and constructors
- A class can extend only one abstract class
- Use when: defining a common base — "is a"

Key rule:
- Interface → implement
- Abstract class → extend

Example:
Animal abstract class → breathe(), eat() (common to all animals)
Flyable interface → fly() (not all animals fly — Bird implements it, Chicken doesn't)

SOLID connection: LSP and ISP naturally supported by this structure.

---

## HashMap Internals

1. put(key, value) → hashCode calculated → bucket found → equals check
2. If key not found → inserted
3. Collision = different keys, same hashCode → chained in same bucket
4. Before Java 8 → LinkedList
5. After Java 8 → if bucket exceeds 8 elements → Red-Black Tree (O(log n))
6. Load factor: 0.75 → at 12 elements (16 × 0.75) → rehash → capacity doubles to 32

---

## equals() and hashCode() Contract

- When equals() is true → hashCode must be same
- When hashCode is different → equals can be false
- If you override only equals → objects may end up in different buckets → never found in HashMap

---

## synchronized vs volatile

volatile:
- Guarantees visibility — threads always read from main memory, not CPU cache
- Does NOT guarantee atomicity
- Use for: flags, singleton instance

synchronized:
- Guarantees both visibility AND atomicity
- Only one thread executes the block at a time
- Slower than volatile

Thread-safe Singleton uses both:
- volatile → ensures all threads see latest instance
- synchronized → ensures only one thread creates the instance

---

## Deadlock

Two threads each hold a lock the other needs → wait forever.

How to prevent:
1. Fixed lock ordering — always acquire locks in same order
2. tryLock() — if cannot acquire, back off
3. Lock timeout — wait limited time, then exit

---

## @Transactional

Works via Spring proxy — proxy opens transaction, method runs, proxy commits or rollbacks.

Rollback rules:
- RuntimeException (unchecked) → automatic rollback
- Checked Exception → NO rollback by default
- Force rollback: @Transactional(rollbackFor = Exception.class)

Self-invocation trap:
- Calling @Transactional method from same class → bypasses proxy → annotation ignored
- Fix: extract to separate bean

---

## Kafka — Core Concepts

Topic → subject of messages (e.g. "orders")
Partition → splits topic for parallel processing
Consumer Group → group of consumers reading same topic
- One partition → one consumer per group
- Different groups → can read same partition independently (fan-out)

At-least-once delivery:
- Kafka guarantees minimum one delivery
- Same message may arrive twice on failure
- Fix: idempotency key — check if already processed before acting

---

## Process vs Thread

Process = running application, has its own memory space
Thread = unit of execution within a process, shares memory

Restaurant analogy:
- Restaurant = Process
- Chef, Waiter = Threads
- Order = Task/Job
- Kitchen = Shared Memory

---

## ExecutorService

Thread creation is expensive. ExecutorService manages a thread pool — threads are reused.

Types:
- newFixedThreadPool(n) → fixed n threads
- newCachedThreadPool() → grows as needed, removes idle threads
- newSingleThreadExecutor() → single thread, sequential

Restaurant analogy: Manager = ExecutorService (assigns orders to available chefs)

---

## CountDownLatch

Waits for multiple threads to finish before continuing.

```java
CountDownLatch latch = new CountDownLatch(3);
// each thread calls latch.countDown() when done
latch.await(); // wait until count reaches 0
```

Similar to CompletableFuture.allOf() — modern alternative.

Restaurant analogy: Waiter waits for all dishes to be ready before serving the table.

---

## ReentrantLock

More powerful than synchronized:
- tryLock() — attempt with timeout, prevents deadlock
- fairness option — longest waiting thread gets lock first
- Must manually unlock in finally block

```java
if (lock.tryLock(5, TimeUnit.SECONDS)) {
    try { // critical section }
    finally { lock.unlock(); }
}
```

---

## wait() and notify()

Low-level thread coordination. Always used inside synchronized block.

- wait() → thread sleeps, releases lock
- notify() → wakes up one waiting thread
- notifyAll() → wakes up all waiting threads

Modern alternative: BlockingQueue (uses wait/notify internally, cleaner API)

---

## Spring Dependency Injection

Without DI: you create dependencies manually with new → tightly coupled
With DI: Spring creates and injects dependencies → loosely coupled, testable

Constructor injection (preferred):
- Dependencies explicit
- Allows final fields — immutable, thread-safe
- Testable without Spring context

Field injection (avoid):
- Hidden dependencies
- Cannot use final
- Requires Spring context for testing

---

## Bean Lifecycle

1. Spring finds class (@Service, @Component, etc.)
2. Constructor runs — object created
3. Dependencies injected (@Autowired)
4. @PostConstruct runs — safe to use dependencies here
5. Bean is ready — in use
6. @PreDestroy runs — cleanup before shutdown
7. Bean destroyed

Why @PostConstruct and not constructor?
Constructor runs before DI — dependencies are null at that point.
@PostConstruct runs after DI — all dependencies are ready.

---

## Restaurant Analogy — Full Picture

Restaurant        = Process (application)
Chef, Waiter      = Thread
Order             = Task/Job
Manager           = ExecutorService (assigns tasks to threads)
Kitchen           = Shared Memory
Door lock         = synchronized / ReentrantLock
Waiting list      = BlockingQueue
"Order ready!"    = notify()
Chef waiting      = wait()
All orders ready  = CountDownLatch
volatile          = Menu board (everyone sees latest version)
Deadlock          = Two waiters each waiting for the other's tray