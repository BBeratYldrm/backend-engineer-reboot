# [1.3] JVM & Memory

## Three Memory Areas

HEAP:
→ All objects live here (new User(), new ArrayList())
→ Managed by Garbage Collector
→ Large area — typically GBs
→ OutOfMemoryError when full and GC can't free space

STACK:
→ Method calls and local variables
→ Each thread has its own stack
→ New frame added per method call, removed when method returns
→ StackOverflowError when too deep (infinite recursion)

METASPACE (Java 8+, replaced PermGen):
→ Class definitions and metadata
→ Static variables
→ Method bytecode
→ Grows dynamically — no fixed cap by default

## Stack vs Heap

Stack:
int x = 5;          // stack — primitive, local variable
String s = "hello"; // s reference on stack, "hello" on heap

Heap:
new User()          // object on heap
new ArrayList<>()   // object on heap

## StackOverflowError vs OutOfMemoryError

StackOverflowError:
→ Too many nested method calls
→ Usually infinite recursion (missing base case)
→ Stack full

OutOfMemoryError:
→ Heap full
→ Too many objects, GC can't free enough
→ Usually a memory leak

## Metaspace Growth — Classloader Leak

Metaspace grows dynamically — this is normally fine.
The problem is classloader leaks.

How it happens:
→ Each ClassLoader holds references to the classes it loaded
→ When a ClassLoader is no longer needed, it should be GC'd
→ If something holds a reference to any class from that loader
→ the entire ClassLoader cannot be collected
→ all its classes stay in Metaspace forever

Common causes:
→ Web app redeployment in application servers (Tomcat, JBoss)
Old classloader from previous deployment is not collected
→ Metaspace grows with each redeploy
→ Libraries that cache class references (JDBC drivers, logging frameworks)
→ ThreadLocal holding a class reference across thread reuse

Symptoms:
→ Metaspace growing steadily over time
→ java.lang.OutOfMemoryError: Metaspace (not heap)
→ Only happens after many redeployments or long uptime

Fix:
→ Set -XX:MaxMetaspaceSize=256m → cap it, fail fast instead of growing forever
→ Diagnose with heap dump + MAT (Memory Analyzer Tool) → look for ClassLoader roots
→ Ensure ThreadLocal values are removed after use (ThreadLocal.remove())

Rakuten connection:
Kubernetes deployments restart the JVM → classloader leak resets.
In long-lived JVM processes (non-containerized), classloader leaks are production risks.

## JVM Memory Model — Happens-Before

CPUs and compilers can reorder instructions for performance.
What one thread writes may not be visible to another thread
unless a happens-before relationship is established.

Happens-before means:
Everything thread A did before the happens-before action
is visible to thread B after the corresponding happens-before action.

---

Visibility vs Atomicity — two separate guarantees:

Visibility: can thread B see what thread A wrote?
Atomicity: does the operation complete as a single, uninterruptible unit?

volatile → solves visibility, does NOT solve atomicity:

volatile int counter = 0;

// Thread 1:            // Thread 2:
counter++;              counter++;

counter++ is three steps: read → increment → write
volatile makes each read/write visible, but does not make all three steps atomic.
Result: race condition still possible → final value may be 1, not 2.

For atomicity → use AtomicInteger:
AtomicInteger counter = new AtomicInteger(0);
counter.incrementAndGet(); // read + increment + write as one atomic operation

Rule:
→ volatile: one thread writes, others only read → visibility is enough
→ AtomicInteger / synchronized: multiple threads write → atomicity needed

Happens-before actions in Java:
→ volatile write → volatile read of same variable
→ synchronized exit → synchronized entry on same monitor
→ Thread.start() → first action in new thread
→ Thread.join() → any action after join() in calling thread

Example — volatile flag:
volatile boolean running = true;

// Thread 1 (writer):
running = false; // volatile write → establishes happens-before

// Thread 2 (reader):
while (running) { // volatile read → sees Thread 1's write
process();
}
// This is correct use of volatile — one writer, one reader, no arithmetic

## GC Types

G1 GC (default Java 9+):
→ General purpose, good for most applications
→ Splits heap into equal-sized regions (not fixed young/old areas)
→ Collects regions with most garbage first (Garbage-First)
→ Predictable pause time target: -XX:MaxGCPauseMillis=200
→ Good balance: throughput + manageable pauses

ZGC (Java 15+ production-ready, default in Java 21+):
→ Low-latency — sub-millisecond pauses regardless of heap size
→ Handles very large heaps (multi-GB to TB)
→ Most GC work done concurrently — app keeps running
→ Best for: fintech, trading, real-time systems where latency is critical

Shenandoah (OpenJDK, Red Hat maintained):
→ Similar goals to ZGC — low latency, concurrent collection
→ Available in OpenJDK, not in Oracle JDK
→ Evacuation (moving objects) is also concurrent — ZGC does this too
→ Trade-off: slightly higher CPU overhead than G1, lower pause than G1
→ Use when: low-latency needed but ZGC not available or not preferred

G1 vs ZGC vs Shenandoah:

G1:
+ Best throughput overall
+ Predictable pause time target
- Pauses can be 100-200ms under GC pressure

ZGC:
+ Sub-millisecond pauses
+ Very large heap support
- Slightly lower throughput vs G1
- Requires more memory overhead

Shenandoah:
+ Also low-latency like ZGC
+ Available in OpenJDK builds
- Not in Oracle JDK
- Higher CPU usage than G1

Stop-the-world — what triggers it:
Even low-latency collectors have some STW phases.

G1 STW triggers:
→ Young generation collection (minor GC) — short but frequent
→ Full GC — rare, triggered when concurrent collection falls behind
→ Humongous object allocation — object larger than half a region

ZGC STW triggers:
→ Initial mark — very short (milliseconds)
→ Final mark — very short
→ Both are O(thread count), not O(heap size) → constant time

The most dangerous STW: Full GC
→ G1 falls back to serial Full GC when concurrent collection can't keep up
→ Triggered by: allocation faster than GC, or Metaspace exhausted
→ Symptom: sudden long pauses in logs, "GC overhead limit exceeded"
→ Fix: tune heap, fix allocation rate, fix memory leaks

## GC Tuning Basics

-Xms → initial heap size
-Xmx → max heap size
-Xms == -Xmx → avoid resizing overhead (production best practice)
Reason: if heap starts small and grows → JVM pauses to resize → latency spikes

Pause time targets:
-XX:MaxGCPauseMillis=200  → G1 target, soft goal — JVM tries but cannot guarantee
-XX:+UseZGC               → switch to ZGC when sub-ms pause needed
-XX:+UseShenandoahGC      → switch to Shenandoah

Setting pause time too low on G1:
→ G1 collects smaller regions to meet pause target
→ Less garbage collected per cycle → higher GC frequency
→ Trade-off: lower latency per pause, but more pauses overall

Rule of thumb for heap sizing:
→ Live set: how much memory the app actually needs at steady state
→ Heap should be 2-4x the live set → GC has room to work efficiently
→ Too small → GC runs constantly (high CPU, pauses)
→ Too large → waste, longer Full GC if it happens

## Allocation & Escape Analysis

Allocation rate:
Every new object created goes to the heap → GC must collect it eventually.
High allocation rate → GC runs more frequently → more CPU, more pauses.

Allocation rate and GC pressure:
Low allocation rate  → GC rarely triggered → stable latency
High allocation rate → GC triggered often → latency spikes at each collection

Common high-allocation patterns:
→ Creating large lists inside loops → new ArrayList<>() per iteration
→ String concatenation with + in a loop → StringBuilder is better
→ Short-lived intermediate objects → streams with heavy boxing
→ Logging with string formatting when log level is disabled
log.debug("value: " + expensiveObject.toString()) → always allocates
log.debug("value: {}", expensiveObject) → allocates only if debug is enabled

Escape Analysis:
JVM optimization — if the JVM can prove an object doesn't "escape" the method
(no reference stored in a field, not passed to another thread),
it may allocate that object on the stack instead of the heap.

public int sum(int a, int b) {
Point p = new Point(a, b); // may be stack-allocated → no GC needed
return p.x + p.y;
}

Stack allocation expectations — manage carefully:
Escape analysis is a JIT optimization — not guaranteed.
→ Enabled by default: -XX:+DoEscapeAnalysis
→ Works best for small, simple objects
→ Does NOT apply to objects that escape (stored in fields, returned, passed across threads)

Do not design your code around escape analysis.
It is a bonus optimization, not a contract.
If you need to avoid allocation pressure → measure first, optimize after.

How to measure allocation rate:
→ JVM flags: -Xlog:gc* → GC logs show allocation rate
→ JFR (Java Flight Recorder) → allocation profiling per class
→ Async-profiler → allocation hotspot flamegraph

Rakuten connection:
Tirebringin under load → many objects created per reservation
GC pauses visible in Gatling p95 response times
Heap sizing: -Xms and -Xmx should be equal in production
If GC logs showed high allocation from a specific class → fix that class first

Real world:
Revolut → ZGC for low-latency financial operations
Netflix → G1 GC with tuned heap for streaming
Uber → monitors GC pause time as a key SLA metric
All three: -Xms == -Xmx in production — no dynamic resizing

Interview tip:
"The JVM has three main memory areas: Heap for objects,
Stack for method frames and local variables, Metaspace for class definitions.
OutOfMemoryError means heap is full — usually a memory leak.
StackOverflowError means too many nested calls — usually infinite recursion.
Metaspace OOM is rarer — usually a classloader leak."

The question I ask myself:
"Is this OutOfMemoryError or StackOverflowError?"
OutOfMemory → check for memory leaks, large object retention, classloader leaks
StackOverflow → check for infinite recursion, missing base case

"Is GC pressure high?"
→ Measure allocation rate first
→ Find the class causing most allocations
→ Reduce short-lived object creation there

---

## Pattern Integration

### Object Pooling — Usually an Anti-Pattern

Object pooling: maintain a pool of pre-created objects, reuse them instead of creating new.

Why it used to make sense:
→ Object creation was expensive in early Java
→ DB connections, threads — creation cost is genuinely high

Why it is an anti-pattern for most objects today:
→ Modern JIT + generational GC handles short-lived object allocation very efficiently
→ Young generation collection is fast — short-lived objects have near-zero GC cost
→ Object pools add complexity: lifecycle management, thread-safety, return discipline
→ Pooling the wrong objects actually increases GC cost
(long-lived pool objects survive into old generation → slower collection)

When object pooling IS appropriate (narrow exceptions):
→ DB connections — creation is expensive (TCP handshake, auth, negotiation)
→ HikariCP, c3p0 — connection pools
→ Thread pools — thread creation is expensive (OS-level)
→ ExecutorService
→ Objects with truly expensive initialization AND high reuse rate
→ SSL contexts, compiled regex patterns

When NOT to pool:
→ Plain domain objects (Order, User, Shop) — allocation is cheap, GC handles them
→ DTOs, records — immutable, throw away after use
→ Anything where the pool adds more complexity than the allocation cost saves

The question I ask myself:
"Is object creation the proven bottleneck here?"
+ No → don't pool, let GC do its job
+ Yes → measure, then consider pooling only if creation is genuinely expensive

### Flyweight Pattern — Shared Immutable Instances

Flyweight reduces memory by sharing common, immutable data across many objects
instead of duplicating it.

Problem:
10,000 shop objects, each carrying a full copy of its city name, prefecture, country.
Same city name stored 10,000 times → memory waste.

Solution:
Extract the shared, immutable part into a shared Flyweight object.
Each shop holds a reference to the shared object, not a copy.

// Without Flyweight:
class Shop {
private String city;        // "Tokyo" duplicated across 5000 shops
private String prefecture;  // "Tokyo-to" duplicated across 5000 shops
}

// With Flyweight:
class Location {  // shared, immutable flyweight
private final String city;
private final String prefecture;

    private static final Map<String, Location> cache = new HashMap<>();

    public static Location of(String city, String prefecture) {
        return cache.computeIfAbsent(city + prefecture, k -> new Location(city, prefecture));
    }
}

class Shop {
private final Location location; // reference to shared instance
}

Java has Flyweight built in:
→ Integer.valueOf(-128 to 127) → cached, same instance every time
→ String pool → String literals are interned (shared)
→ Boolean.TRUE / Boolean.FALSE — singleton instances

Trade-offs:
+ Significant memory reduction when many objects share common state
- Shared state must be immutable — mutable shared state causes bugs
- Cache/registry adds complexity, must be managed carefully
- Not worth it for small object counts — premature optimization

Rakuten connection:
ShopSearchService returned many ShopSearchItems.
If all shops in the same city shared a Location flyweight → less heap pressure
under peak load (1380+ reservations/hour → many objects created).

Interview tip:
"Flyweight is about sharing immutable state across many objects.
Java's Integer cache and String pool are classic examples built into the JDK.
The key requirement is immutability — shared mutable state is a race condition."