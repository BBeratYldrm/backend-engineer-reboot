# [1.4] Thread Management

## Race Condition

Two threads reading and writing shared data simultaneously → inconsistent result.

Example:
balance = 1000
Thread 1: balance >= 500? yes → balance = 500
Thread 2: balance >= 500? yes (read before Thread 1 wrote) → balance = 500
Result: 1000 withdrawn, only 500 deducted ❌

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

ConcurrentHashMap — for shared maps (covered in [1.1])

## Deadlock

Two threads waiting for each other's lock → stuck forever.

Thread 1: holds lockA, waiting for lockB
Thread 2: holds lockB, waiting for lockA
→ Neither can proceed ❌

Solution — Lock Ordering:
Always acquire locks in the same order.
If all threads lock A before B → deadlock impossible.

lock.lock() with ReentrantLock → supports timeout:
if (lock.tryLock(1, TimeUnit.SECONDS)) { ... }
→ Won't wait forever → deadlock avoided

## Thread Pool

Never create threads directly in production:
new Thread(() -> ...).start() → uncontrolled, system crash risk

Use ExecutorService:
Executors.newFixedThreadPool(10)    → max 10 threads, queue for rest
Executors.newCachedThreadPool()     → grows as needed, good for short IO tasks
Executors.newVirtualThreadPerTaskExecutor() → Java 21, unlimited

Fixed vs Cached:
Fixed → predictable, resource controlled, good for CPU-bound
Cached → flexible, good for IO-bound short tasks, can grow uncontrolled

ForkJoinPool (Work-Stealing):
→ CPU-bound parallel tasks
→ Idle threads steal work from busy threads
→ Used by parallel streams internally

## IO-bound vs CPU-bound

IO-bound (waiting on external resources):
DB queries, HTTP calls, file reads, message queues
→ Thread mostly idle → Virtual Threads ideal

CPU-bound (constant computation):
Math, image processing, encryption
→ Thread always working → Platform Thread + ForkJoinPool

## Backpressure

Producer faster than consumer → queue fills up → system overload.

Solutions:
Thread pool: queue fills → RejectedExecutionException → producer must slow down
Kafka: add more consumers to consumer group
Reactive: built-in backpressure signals

## volatile keyword

Guarantees visibility across threads — always read from main memory.
Does NOT guarantee atomicity.

volatile boolean running = true;
// Thread 1: running = false
// Thread 2: immediately sees running = false ✅
// Without volatile: Thread 2 might see cached value

Use volatile for: simple flags, status variables
Use AtomicInteger for: counters, numeric operations
Use synchronized for: complex multi-step operations

Rakuten connection:
Spring Boot Tomcat: default 200 thread pool
Each HTTP request → 1 thread → DB wait → thread idle
Virtual Threads would have solved this

ActiveMQ batch consumer:
Producer sends emails fast → consumer processes slowly
Queue builds up → backpressure

Real world:
Revolut → AtomicLong for balance operations, no locks
Netflix → thread pool per service, isolated failures
Uber → ForkJoinPool for route calculation (CPU-bound)

Interview tip:
"Race conditions occur when multiple threads access shared mutable state.
Solutions: synchronized (simple, slower), AtomicInteger (lock-free, faster),
ConcurrentHashMap (for maps). Deadlock prevention: always acquire locks
in the same order, or use tryLock with timeout."

The question I ask myself:
"Is shared mutable state accessed by multiple threads?"
+ Yes → synchronized or Atomic classes
  "Am I acquiring multiple locks?"
+ Yes → always same order → deadlock prevention
  "IO-bound or CPU-bound?"
+ IO → Virtual Threads
+ CPU → ForkJoinPool