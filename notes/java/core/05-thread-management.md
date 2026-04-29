# [1.4] Thread Management

## Thread Lifecycle

A thread goes through defined states from creation to termination.

NEW → RUNNABLE → (BLOCKED / WAITING / TIMED_WAITING) → TERMINATED

NEW: Thread created, not yet started.
RUNNABLE: Running or ready to run — OS scheduler decides.
BLOCKED: Waiting to acquire a monitor lock (synchronized).
WAITING: Waiting indefinitely — Object.wait(), Thread.join(), LockSupport.park().
TIMED_WAITING: Waiting with a timeout — Thread.sleep(), wait(timeout), tryLock(timeout).
TERMINATED: Finished execution.

The difference between BLOCKED and WAITING:
BLOCKED → trying to enter a synchronized block, another thread holds the lock
WAITING → voluntarily waiting, will be woken by notify() or interrupt()

## Race Condition

Two threads reading and writing shared data simultaneously → inconsistent result.

Example:
balance = 1000
Thread 1: balance >= 500? yes → balance = 500
Thread 2: balance >= 500? yes (read before Thread 1 wrote) → balance = 500
Result: 1000 withdrawn, only 500 deducted

## Solutions

synchronized — method level lock:
public synchronized void withdraw(int amount) {
if (balance >= amount) balance -= amount;
}
One thread at a time → safe but slower.

AtomicInteger — lock-free, CPU-level atomic operation:
AtomicInteger balance = new AtomicInteger(1000);
→ Faster than synchronized for simple operations
→ No explicit locking
→ Uses CPU compare-and-swap (CAS) instruction

ConcurrentHashMap — for shared maps (covered in [1.1])

## volatile keyword

Guarantees visibility across threads — always read from main memory.
Does NOT guarantee atomicity.

volatile boolean running = true;
// Thread 1: running = false
// Thread 2: immediately sees running = false
// Without volatile: Thread 2 might see cached value

Use volatile for: simple flags, status variables — one writer, multiple readers
Use AtomicInteger for: counters, numeric operations — multiple writers
Use synchronized for: complex multi-step operations

## Deadlock

Two threads waiting for each other's lock → stuck forever.

Thread 1: holds lockA, waiting for lockB
Thread 2: holds lockB, waiting for lockA
→ Neither can proceed

Deadlock triad — three conditions that must ALL be present for deadlock:

1. Mutual exclusion:
   Only one thread can hold a resource at a time.
   (Locks are mutually exclusive by definition.)

2. Hold and wait:
   A thread holds at least one resource and waits to acquire more.
   Thread 1 holds lockA and waits for lockB.

3. Circular wait:
   Thread A waits for Thread B, Thread B waits for Thread A.
   (Or longer chain: A → B → C → A)

Remove any one condition → deadlock impossible.
In practice: break circular wait via lock ordering.

Solution 1 — Lock Ordering:
Always acquire locks in the same order across all threads.
If all threads always lock A before B → circular wait impossible.

// WRONG — different order in different threads:
// Thread 1: lock(account1) then lock(account2)
// Thread 2: lock(account2) then lock(account1)

// CORRECT — consistent order everywhere:
Lock first  = id1 < id2 ? lockA : lockB;
Lock second = id1 < id2 ? lockB : lockA;
first.lock();
second.lock();

Solution 2 — tryLock with timeout:
if (lock.tryLock(1, TimeUnit.SECONDS)) {
try { ... }
finally { lock.unlock(); }
} else {
// timeout — retry or abort, no deadlock
}

Detecting deadlocks in production:
→ Thread dump: jstack <pid> → look for "waiting to lock" cycles
→ JConsole / VisualVM → detect deadlocks visually
→ Deadlocked threads show BLOCKED state in thread dump

## Starvation

Starvation: a thread is perpetually denied access to a resource
because other threads always get priority.

Unlike deadlock — threads are not stuck waiting for each other.
The thread is runnable, but never gets scheduled or gets the lock.

Common causes:
→ Low-priority thread never gets CPU when high-priority threads are always present
→ Unfair lock — threads that arrive first are not guaranteed to get the lock first
Synchronized in Java is NOT fair — no ordering guarantee
→ Thread pool with bounded queue + high-priority tasks always filling it
Low-priority tasks queue indefinitely

Example:
ExecutorService pool = Executors.newFixedThreadPool(2);
// 10 high-priority tasks submitted continuously
// 1 low-priority task submitted → waits in queue forever
// Pool is always occupied by high-priority work

Solution — Fair lock:
ReentrantLock fairLock = new ReentrantLock(true); // fair = true
// Threads acquire lock in order of waiting — no starvation

Trade-off of fair locks:
+ No starvation
- Lower throughput — fairness has overhead
- Not appropriate for most cases — only when starvation is a proven problem

Solution — Separate queues:
High-priority and low-priority work in separate thread pools.
Each pool gets its own queue — low-priority pool always makes progress.

Starvation vs Deadlock:
Deadlock: threads are stuck, waiting for each other — no progress at all
Starvation: thread can run but never gets the chance — partial progress

Rakuten connection:
Notification tasks (email/SMS) in the same thread pool as reservation processing.
Under load, reservation tasks fill the pool → notification tasks starved.
Fix: separate pools (Bulkhead) — each has its own queue.

## Thread Pool

Never create threads directly in production:
new Thread(() -> ...).start() → uncontrolled, system crash risk

Use ExecutorService:
Executors.newFixedThreadPool(10)             → max 10 threads, queue for rest
Executors.newCachedThreadPool()              → grows as needed, good for short IO tasks
Executors.newVirtualThreadPerTaskExecutor()  → Java 21, unlimited virtual threads
Executors.newSingleThreadExecutor()          → sequential execution, 1 thread

Fixed vs Cached:
Fixed → predictable, resource controlled, good for CPU-bound
Cached → flexible, good for IO-bound short tasks, can grow uncontrolled

ForkJoinPool (Work-Stealing):
→ CPU-bound parallel tasks
→ Idle threads steal work from busy threads
→ Used by parallel streams internally
→ Default parallelism: number of CPU cores

Thread pool sizing rules of thumb:
CPU-bound: pool size = number of CPU cores (no idle waiting, max throughput)
IO-bound: pool size = higher (threads spend time waiting → more threads = more concurrency)
Formula often cited: threads = cores * (1 + wait_time / compute_time)

With Virtual Threads on Java 21:
IO-bound scaling → use virtual threads, not large platform thread pools.
Platform thread pools → only for CPU-bound work.

## IO-bound vs CPU-bound

IO-bound (waiting on external resources):
DB queries, HTTP calls, file reads, message queues
→ Thread mostly idle → Virtual Threads ideal

CPU-bound (constant computation):
Math, image processing, encryption
→ Thread always working → Platform Thread + ForkJoinPool

Mixed workload:
→ Separate the IO and CPU phases
→ Virtual Threads for IO phase, ForkJoinPool for CPU phase

## ThreadLocal

ThreadLocal stores a value per thread — each thread sees its own copy.
No sharing → no synchronization needed for the stored value.

ThreadLocal<String> requestId = new ThreadLocal<>();

// In request thread:
requestId.set(UUID.randomUUID().toString());

// Later in same thread (different class, different method):
String id = requestId.get(); // still the same value — bound to this thread

Common uses:
→ Request-scoped context: user ID, correlation ID, locale, transaction
→ SimpleDateFormat (not thread-safe) → one instance per thread
→ Database connection per thread in some frameworks

Always clean up after use:
requestId.remove(); // CRITICAL — prevents memory leaks in thread pools

Why remove() matters:
Thread pool reuses threads.
If ThreadLocal is not removed → next request on this thread sees previous request's data.
Worse: if the value is a large object → memory leak (thread lives forever, holds the object).

ThreadLocal and Virtual Threads — important shift:
Virtual Threads are not reused — each task gets a fresh virtual thread.
ThreadLocal values are safe from cross-request contamination.
BUT: millions of virtual threads → millions of potential ThreadLocal values → memory pressure.

Java 21 introduces ScopedValue as the modern alternative:
ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();

ScopedValue.where(REQUEST_ID, "abc-123").run(() -> {
// REQUEST_ID.get() returns "abc-123" within this scope
// Automatically cleaned up when scope exits
// Immutable — cannot be changed once bound
processRequest();
});

ThreadLocal vs ScopedValue:
ThreadLocal: mutable, manually managed, survives thread reuse
ScopedValue: immutable, automatically scoped, safe with virtual threads

For new code on Java 21+:
→ ScopedValue for request-scoped context
→ ThreadLocal only for legacy compatibility or mutable per-thread state

Rakuten connection:
Correlation ID for request tracing → ThreadLocal in Spring's MDC (Mapped Diagnostic Context)
MDC.put("requestId", id) → MDC uses ThreadLocal internally
MDC.clear() in finally block → always required in thread pool environments

## Backpressure

Producer faster than consumer → queue fills up → system overload.

This is not just a messaging problem — it's a system thinking problem.
Any time two parts of a system run at different speeds, backpressure must be handled.

Backpressure strategies:

1. Block the producer (thread pool + bounded queue):
   ExecutorService pool = new ThreadPoolExecutor(
   10, 10, 0L, TimeUnit.MILLISECONDS,
   new ArrayBlockingQueue<>(100) // bounded queue
   );
   // Queue full → RejectedExecutionException → producer slows down or retries

2. Drop messages (lossy):
   If message loss is acceptable → drop when queue full.
   Use case: metrics, analytics, non-critical events.
   Not acceptable for: financial transactions, reservations.

3. Add consumers (scale out):
   Kafka consumer group → add more consumers → more partitions consumed in parallel
   Works until partition count is the limit.

4. Reactive backpressure signals (Project Reactor, RxJava):
   Consumer signals demand to producer: "I can handle N more items."
   Producer only sends what consumer requested — no overflow.
   Built into the Reactive Streams specification.

   Flux.range(1, 1_000_000)
   .onBackpressureBuffer(100) // buffer 100, then signal upstream to slow down
   .subscribe(item -> slowConsumer(item));

5. Rate limiting (token bucket / leaky bucket):
   Producer is allowed N operations per second regardless of consumer speed.
   Consumer never overwhelmed — rate is controlled at the source.
   Redis + token bucket → distributed rate limiting.

System design backpressure thinking:
The question is not just "what happens when the queue fills up"
but "where in the system does the pressure propagate to?"

Backpressure propagates upstream — it must reach the source eventually.
If it doesn't → buffer overflow → data loss or OOM.

Rakuten connection:
ActiveMQ consumer: producer sends email/SMS notifications at burst speed.
Consumer processes slowly (external SMTP, SMS gateway).
Queue builds up → backpressure needed:
→ Short term: bounded queue + retry with exponential backoff
→ Long term: add consumers, or use Kafka with consumer groups

## Virtual vs Platform Thread — Decision Point

When to use Virtual Threads:
→ IO-bound: DB, HTTP, file, messaging
→ High concurrency: thousands of simultaneous requests
→ Simple, blocking-style code preferred
→ Java 21+, Spring Boot 3.2+

When Virtual Threads break:
→ CPU-bound work: no benefit from parking
→ synchronized + IO: pinning — park mechanism disabled
→ ThreadLocal overuse: memory pressure at scale
→ Native method calls inside blocking ops: pinning

When to stay with Platform Threads:
→ CPU-bound parallel computation → ForkJoinPool
→ Existing code with synchronized + IO (not yet refactored)
→ Work that must complete with OS-level thread guarantees

---

## Pattern Integration

### Producer-Consumer Pattern

Decouples the rate of production from the rate of consumption.
Producer puts work into a shared queue. Consumer takes from the queue.
Neither knows about the other — only the queue is shared.

// Shared queue:
BlockingQueue<Order> queue = new LinkedBlockingQueue<>(100); // bounded

// Producer thread:
void produce(Order order) throws InterruptedException {
queue.put(order); // blocks if queue full → natural backpressure
}

// Consumer thread:
void consume() throws InterruptedException {
while (true) {
Order order = queue.take(); // blocks if queue empty → waits for work
process(order);
}
}

Why BlockingQueue:
put() blocks when full → producer slows down automatically
take() blocks when empty → consumer waits without spinning
Thread-safe → no manual synchronization needed

Multiple producers, multiple consumers:
One queue, N producer threads, M consumer threads → all safe with BlockingQueue.
Scale consumers independently from producers.

Variants:
ArrayBlockingQueue(n)  → fixed size, fair or unfair
LinkedBlockingQueue(n) → linked nodes, typically higher throughput
PriorityBlockingQueue  → ordered by priority, unbounded (careful)
SynchronousQueue       → no buffer — producer waits until consumer ready (tight coupling)

Real world:
Rakuten → reservation events produced by API layer, consumed by notification service
Kafka → Producer-Consumer at system scale, durable, replayable
Thread pool task queue → internal Producer-Consumer

Interview tip:
"Producer-Consumer is about decoupling speed mismatches between two parts of a system.
BlockingQueue is the simplest Java implementation — put() and take() handle
backpressure and waiting automatically. For distributed systems, Kafka."

### Bulkhead — Thread Pool Isolation

See also: [1.2] Modern Java — Bulkhead section.

Separate thread pools for separate concerns.
One pool exhausted → only its feature degrades → others continue.

ExecutorService reservationPool    = Executors.newFixedThreadPool(20);
ExecutorService notificationPool   = Executors.newFixedThreadPool(5);
ExecutorService reportingPool      = Executors.newFixedThreadPool(3);

Notification burst → notificationPool fills → only notifications degrade.
Reservations unaffected — different pool, different queue.

With Virtual Threads → Bulkhead by Semaphore:
Semaphore reservationLimit = new Semaphore(200);

reservationLimit.acquire();
try {
processReservation(request);
} finally {
reservationLimit.release();
}

Rakuten connection:
Tirebringin — reservations and notifications shared the same thread resources.
Under load, notification storms competed with reservation processing.
Bulkhead would have isolated them completely.

Real world:
Revolut: payment processing pool vs. account statement generation pool
Netflix: streaming pool vs. recommendation computation pool
Separate failure domains → cascading failure prevented.

Interview tip:
"Race conditions occur when multiple threads access shared mutable state.
Solutions: synchronized (simple, slower), AtomicInteger (lock-free, faster).
Deadlock needs three conditions — break circular wait via consistent lock ordering.
Starvation is subtler — use fair locks or separate queues.
Producer-Consumer and Bulkhead are the two structural patterns
that make concurrent systems manageable."

The question I ask myself:
"Is shared mutable state accessed by multiple threads?"
+ Yes → synchronized or Atomic classes
  "Am I acquiring multiple locks?"
+ Yes → always same order → deadlock prevention
  "Is one thread never making progress?"
+ Yes → starvation, check lock fairness or pool sizing
  "IO-bound or CPU-bound?"
+ IO → Virtual Threads
+ CPU → ForkJoinPool
  "Are producer and consumer at different speeds?"
+ Yes → BlockingQueue or Kafka → Producer-Consumer pattern
  "Could one overloaded service take down others?"
+ Yes → separate thread pools → Bulkhead