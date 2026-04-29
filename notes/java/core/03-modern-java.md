# [1.2] Modern Java (17 → 21 → 25)

## Records

Problem: DTO classes require too much boilerplate.
3 fields → constructor + getters + equals + hashCode + toString = 50+ lines

Solution:
public record ShopSearchItem(
String shopName,
String address,
double distance
) {}

Auto-generated: constructor, getters (shopName() not getShopName()),
equals(), hashCode(), toString()

Records are immutable by default — fields are final.

When to use:
+ DTO — carry data, no business logic
+ Request/Response — API boundary
+ Value Object — immutable value
+ Projection — partial data from DB query

When NOT to use:
- JPA Entity — needs to be mutable, requires no-arg constructor
- Service class — has business logic
- Classes that need inheritance — records can't extend classes

Serialization considerations:
Jackson works with records out of the box from Jackson 2.12+.
No @JsonProperty needed if field names match JSON keys.
Custom constructor logic → use compact constructor:

public record Money(double amount, String currency) {
public Money {
// compact constructor — no parameter list repeated
if (amount < 0) throw new IllegalArgumentException("amount cannot be negative");
currency = currency.toUpperCase(); // normalize
}
}

Compact constructor runs before the record is created — ideal for validation.

Serialization pitfalls:
→ Jackson uses getter-style accessors: amount() not getAmount()
If your JSON field is "amount", this works automatically.
If the JSON key differs → use @JsonProperty on the component.
→ Records cannot have @JsonIgnore on fields — use @JsonIgnoreProperties at class level
→ Avoid records as JPA projections with Spring Data if using interface projections — use DTO projection instead

Rakuten connection:
ShopSearchItem, ShopSearchResponse → could have been Records
ZipCode entity → should NOT be a Record (JPA entity)

Interview tip:
"Records eliminate DTO boilerplate — constructor, getters, equals, hashCode,
toString all generated automatically. They're immutable by design,
making them perfect for Value Objects and API boundaries.
Compact constructor is the place for validation — runs before the object exists."

## var — Local Variable Type Inference

Java 10+. Compiler infers the type — less boilerplate.

var items = new ArrayList<String>(); // type is clear → OK
var user = userRepository.findById(id); // unclear what returns → avoid

When to use:
+ Type is obvious from right side → var items = new ArrayList<>()
+ Long generic types → var map = new HashMap<String, List<Integer>>()

When NOT to use:
- Return type of a method
- Field declaration
- When type is not obvious → confusing for readers
- Lambda parameters

The question I ask myself:
"Is the type obvious from the right side of the assignment?"
+ Yes → var is fine
+ No → write the type explicitly

## Sealed Classes

Restricts which classes can extend or implement a type.
"Only these specific classes are allowed."

public sealed class Shape permits Circle, Rectangle, Triangle {}
public final class Circle extends Shape { ... }
// No one else can extend Shape

Why useful:
→ Controlled, closed hierarchy
→ Compiler knows all possible subtypes
→ No else needed in switch — compiler verifies all cases covered
→ Eliminates "unknown subtype" bugs

With pattern matching:
switch (shape) {
case Circle c    -> Math.PI * c.radius() * c.radius();
case Rectangle r -> r.width() * r.height();
case Triangle t  -> 0.5 * t.base() * t.height();
// no default needed — compiler knows all cases
}

Bounded polymorphism:
Sealed classes are a tool for bounded polymorphism — polymorphism within a known, fixed set.
Regular inheritance → open-ended, anyone can extend
Sealed → closed set, compiler-enforced

When to use sealed vs open inheritance:
→ Sealed: the set of subtypes is finite and known at design time
(payment results, reservation statuses, HTTP response types)
→ Open inheritance: external code needs to extend
(framework extension points, plugins, third-party integrations)

State machine modelling:
Sealed classes are natural for domain state machines.
Each state is a subtype — impossible to have an invalid state.

public sealed interface ReservationStatus
permits Pending, Confirmed, Cancelled, Completed {}

public record Pending()   implements ReservationStatus {}
public record Confirmed() implements ReservationStatus {}
public record Cancelled(String reason) implements ReservationStatus {}
public record Completed(LocalDateTime at) implements ReservationStatus {}

// State transitions:
ReservationStatus next = switch (current) {
case Pending p    -> new Confirmed();
case Confirmed c  -> new Completed(LocalDateTime.now());
case Cancelled ca -> throw new IllegalStateException("cannot transition from Cancelled");
case Completed co -> throw new IllegalStateException("already completed");
};
// Compiler enforces: all states handled, no forgotten branch

Without sealed classes:
→ String status = "PENDING" — no type safety, any string is valid
→ Enum — fixed set but no data attached to each state
→ Sealed records — fixed set AND each state can carry its own data

Real world:
Payment results → Success(transactionId), Failure(errorCode), Pending(checkUrl)
Order status → Created, Confirmed, Shipped(trackingId), Delivered, Returned(reason)
API responses → sealed with all possible response types

Rakuten connection:
Reservation status modeled as sealed class —
only known statuses allowed, compiler enforces completeness,
each status can carry relevant data (Cancelled carries reason, Completed carries timestamp)

Interview tip:
"Sealed classes give you algebraic data types in Java —
you know exactly what subtypes exist, and each can carry its own data.
Combined with pattern matching, switch becomes exhaustive and safe.
It's the natural tool for state machines and closed domain hierarchies."

The question I ask myself:
"Is this a closed set of types that will never be extended externally?"
+ Yes → sealed class/interface
+ No → regular inheritance

## Pattern Matching

Pattern matching reduces boilerplate around type checking and casting.
Before Java 16, checking and using a type required two steps — check then cast.

Old way:
if (obj instanceof String) {
String s = (String) obj; // redundant cast
System.out.println(s.length());
}

Pattern matching (Java 16+):
if (obj instanceof String s) {
System.out.println(s.length()); // s is already String, no cast
}

The variable s is scoped — only available inside the if block.

Switch pattern matching (Java 21):
Combines pattern matching with switch — the most powerful form.

Object result = switch (response) {
case SuccessResponse s  -> s.getData();
case ErrorResponse e    -> "Error: " + e.getMessage();
case EmptyResponse emp  -> "No data";
default                 -> throw new IllegalStateException("unknown response");
};

With sealed classes → no default needed:
sealed interface ApiResponse permits SuccessResponse, ErrorResponse, EmptyResponse {}

Object result = switch (response) {
case SuccessResponse s -> s.getData();
case ErrorResponse e   -> "Error: " + e.getMessage();
case EmptyResponse emp -> "No data";
// no default — compiler knows all cases are covered
};

Guarded patterns — adding conditions inside the case:
switch (shape) {
case Circle c when c.radius() > 100 -> "large circle";
case Circle c                        -> "small circle";
case Rectangle r                     -> "rectangle";
}

Domain branching — readable business logic:
// Without pattern matching — nested instanceof chains:
if (event instanceof OrderCreated) {
OrderCreated e = (OrderCreated) event;
...
} else if (event instanceof OrderShipped) {
OrderShipped e = (OrderShipped) event;
...
}

// With pattern matching — clean domain branching:
switch (event) {
case OrderCreated e  -> handleCreated(e);
case OrderShipped e  -> handleShipped(e);
case OrderCancelled e -> handleCancelled(e);
}

Visitor pattern alternative:
Visitor pattern exists to add operations over a class hierarchy without modifying it.
With sealed + pattern matching → same goal, less ceremony.

// Old: Visitor interface + accept() on every class + implement visit() per type
// New: sealed + switch — no accept(), no visitor interface, just switch on type

Trade-offs vs Visitor:
+ Pattern matching: less code, more readable
+ Visitor: can be added to open hierarchies (unsealed), more extensible
- Pattern matching: only works well with sealed (otherwise needs default + can miss types)

record + sealed together — algebraic data types:
Records carry data. Sealed restricts the hierarchy.
Together they form algebraic data types — the most expressive combination in modern Java.

public sealed interface PaymentResult permits Success, Failure, Pending {}
public record Success(String transactionId, LocalDateTime at) implements PaymentResult {}
public record Failure(String errorCode, String message)        implements PaymentResult {}
public record Pending(String checkUrl)                         implements PaymentResult {}

// At the call site — exhaustive, typed, data-carrying:
double fee = switch (result) {
case Success s  -> calculateFee(s.transactionId());
case Failure f  -> 0.0;
case Pending p  -> -1.0; // not yet decided
};

Rakuten connection:
Shop search results → sealed interface with Available(shopData), Unavailable(reason)
Each branch carries exactly the data it needs — no null fields, no casting

Interview tip:
"Pattern matching eliminates redundant casts and makes type-based branching
explicit and safe. Combined with sealed classes, the compiler enforces
that all cases are handled — no forgotten branches, no runtime surprises.
It's the modern replacement for instanceof chains and a lighter alternative to Visitor."

The question I ask myself:
"Am I checking a type and then immediately using it?"
+ Yes → pattern matching removes the cast
  "Is the type hierarchy closed?"
+ Yes → sealed + switch → exhaustive, compiler-verified branching

## CompletableFuture — Why It Falls Short

Before Virtual Threads and Structured Concurrency, CompletableFuture was the
standard tool for running tasks in parallel without blocking threads.

How it works:
CompletableFuture<User>    userFuture  = CompletableFuture.supplyAsync(() -> getUser(id));
CompletableFuture<List<Order>> orderFuture = CompletableFuture.supplyAsync(() -> getOrders(id));

CompletableFuture.allOf(userFuture, orderFuture).join();

User user     = userFuture.get();
List<Order> orders = orderFuture.get();

Problems at scale:

1. Error handling is fragile:
   If one future fails → others keep running → resources wasted
   thenCompose, exceptionally, handle → callback chains → hard to read and reason about

2. Cancellation is best-effort:
   cancel() sets the state but does not interrupt the underlying thread
   The actual work may keep running — no guaranteed cleanup

3. Stack traces are useless:
   Exception happens in a thread pool thread
   Stack trace shows the pool internals, not where the original call was made
   Debugging async issues is significantly harder

4. Structured relationship is implicit:
   No clear parent-child relationship between tasks
   Lifecycle management falls entirely on the developer
   Easy to leak threads on partial failure

5. Code reads inside-out:
   .thenApply().thenCompose().exceptionally() — callback pyramid
   Not natural reading order — maintenance burden grows fast

CompletableFuture is still valid when:
+ Integrating with legacy async APIs that already return CompletableFuture
+ Simple one-off async task where structured concurrency is overkill
+ Java version < 21

But for new code on Java 21+:
→ Structured Concurrency for parallel subtasks
→ Virtual Threads for IO-bound scaling
Both together give everything CompletableFuture tried to provide, with far less complexity.

## Virtual Threads (Java 21) — Critical

Problem:
Every HTTP request → 1 platform thread
Thread blocks on DB/IO → sits idle, wasting memory
Platform thread: ~1MB each
1000 concurrent requests → ~1GB just for waiting threads
Thread pool limit reached → new requests queue up → slow

Previous solutions:
Reactive (WebFlux, RxJava) → solves scaling but complex code
CompletableFuture → complex, hard to debug
Both require thinking differently — steep learning curve

Virtual Thread solution:
Write blocking code → JVM handles the rest

Platform Thread (OS-managed): heavy, ~1MB, limited to thousands
Virtual Thread (JVM-managed): lightweight, ~few KB, millions possible

Park mechanism:
Virtual Thread blocks on IO →
JVM parks it (suspends, keeps in memory) →
Same platform thread picks up another Virtual Thread →
IO completes → Virtual Thread resumes where it left off

Result: blocking code style + reactive scalability

How to use:
// Explicit:
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

// Spring Boot 3.2+:
spring.threads.virtual.enabled: true
// That's it — entire app uses Virtual Threads

When Virtual Threads shine (IO-bound):
+ DB queries
+ HTTP calls to external APIs
+ Message queue operations (Kafka, ActiveMQ)
+ File operations
  → Thread spends most time waiting → park mechanism works perfectly

When Virtual Threads don't help (CPU-bound):
- Mathematical calculations
- Image/video processing
- Encryption
  → Thread always working, never waiting → nothing to park
  → Use Platform Thread + ForkJoinPool instead

Pinned Virtual Thread — main pitfall:
synchronized block + IO → Virtual Thread CANNOT be parked
Platform Thread blocks → benefit lost

// WRONG:
synchronized (lock) {
dbQuery(); // IO inside synchronized → pinned
}

// CORRECT:
lock.lock();
try {
dbQuery(); // Virtual Thread can be parked
} finally {
lock.unlock();
}

Other pinning causes:
→ Native method calls (JNI) inside a blocking operation
→ Some older third-party libraries that rely heavily on synchronized internally

How to detect pinning:
-Djdk.tracePinnedThreads=full → JVM logs pinned virtual thread events
Use this during load testing — not in production.

Rakuten connection:
Tirebringin had DB queries, ActiveMQ, external API calls — all IO-bound.
With Virtual Threads: same hardware, significantly more concurrent reservations.
The Gatling tests that showed 8s response times under load would have looked different.

Real world:
Revolut — millions of concurrent financial operations → Virtual Threads ideal
Netflix — thousands of concurrent streaming requests → IO-bound, perfect fit
Uber — driver matching calls multiple services → all IO-bound

Interview tip:
"Virtual Threads give you the simplicity of blocking code
with the scalability of reactive programming.
The key pitfall is pinning — synchronized blocks with IO
prevent the park mechanism. Use ReentrantLock instead.
Use -Djdk.tracePinnedThreads=full to detect pinning during testing."

The question I ask myself:
"Is this IO-bound or CPU-bound?"
+ IO-bound → Virtual Threads → massive scalability gain
+ CPU-bound → Virtual Threads won't help, use ForkJoinPool

## Structured Concurrency (Java 21)

Problem:
Multiple independent tasks needed for one response.
Sequential: 200ms + 300ms + 250ms = 750ms
Parallel: max(200ms, 300ms, 250ms) = 300ms — 2.5x faster

Old way — CompletableFuture:
Works but complex error handling, hard to cancel, messy stack traces.
If one fails → others keep running → resource waste.

Structured Concurrency solution:
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
var userTask  = scope.fork(() -> userService.getUser(id));
var orderTask = scope.fork(() -> orderService.getOrders(id));
var payTask   = scope.fork(() -> paymentService.get(id));

    scope.join();           // wait for all
    scope.throwIfFailed();  // propagate first failure

    return new Dashboard(userTask.get(), orderTask.get(), payTask.get());
}

ShutdownOnFailure:
One task fails → scope signals cancellation to all siblings → no resource waste.

Task scope types:
StructuredTaskScope.ShutdownOnFailure()
→ First failure cancels all others
→ Use when: all results are required — one missing = operation fails

StructuredTaskScope.ShutdownOnSuccess()
→ First success cancels all others
→ Use when: racing for the fastest result — hedged requests

Cancellation semantics:
Cancellation in Structured Concurrency is cooperative.
The scope sends a cancellation signal → forked tasks check Thread.currentThread().isInterrupted()
or blocking IO operations respond to interruption automatically.
A task that ignores interruption will run to completion regardless.

// Child task that respects cancellation:
var task = scope.fork(() -> {
// blocking IO → automatically interrupted when scope cancels
return httpClient.get(url);
});

// Child task that must check manually:
var task = scope.fork(() -> {
for (int i = 0; i < 1_000_000; i++) {
if (Thread.currentThread().isInterrupted()) return null; // cooperative
process(i);
}
});

try-with-resources:
Block exits → scope.close() called automatically →
all child tasks are waited on → no leaks, no orphan threads.
This is the key guarantee: child lifetime is bounded by parent scope.

Why better than CompletableFuture:
+ Clear parent-child relationship between threads
+ Automatic cancellation on failure
+ Clean stack traces — easy to debug
+ Code reads top-to-bottom — no callback chains
+ Thread leaks are structurally impossible

Rakuten connection:
ShopSearchService fetched ZipCode + PartsShops sequentially.
With Structured Concurrency → parallel → 2x faster.
If ZipCode lookup fails → shop lookup automatically cancelled → no wasted DB calls.

Real world:
Dashboard APIs — user + orders + payments in parallel
Search results — multiple data sources combined
Revolut — balance + transactions + limits fetched simultaneously
Hedged requests — send same request to 3 replicas, take first response

Interview tip:
"Structured Concurrency makes parallel tasks safe and readable.
If one subtask fails, all siblings are cancelled automatically.
Cancellation is cooperative — blocking IO responds automatically,
CPU-bound loops must check isInterrupted().
It's the structured programming equivalent for concurrent code — scope defines lifetime."

The question I ask myself:
"Are these tasks independent? Can they run in parallel?"
+ Yes → Structured Concurrency
  "Do I need all results?"
+ Yes → ShutdownOnFailure
  "Do I just need the fastest?"
+ Yes → ShutdownOnSuccess
  "Does one depend on the other's result?"
+ Yes → sequential is correct, no parallelism needed

---

## Pattern Integration

### State Pattern — with Sealed Classes

State pattern models behavior that changes based on the current state of an object.
Classic implementation: interface + one class per state + context holds current state.
Modern implementation: sealed interface + records + switch.

// Sealed states:
public sealed interface TrafficLight permits Red, Yellow, Green {}
public record Red()    implements TrafficLight {}
public record Yellow() implements TrafficLight {}
public record Green()  implements TrafficLight {}

// State transitions:
TrafficLight next(TrafficLight current) {
return switch (current) {
case Red r    -> new Green();
case Green g  -> new Yellow();
case Yellow y -> new Red();
};
}

Compiler enforces: all states handled.
No if/else, no instanceof, no forgotten branch.

### Pipeline Pattern — Stream + Mapping

A pipeline is a sequence of transformation steps applied to data in order.
Java Streams are the most natural pipeline implementation.

// Pipeline: raw shop data → filtered → enriched → sorted → response
List<ShopSearchResponse> pipeline = rawShops.stream()
.filter(ShopFilter::isActive)               // step 1: filter
.map(shop -> enrich(shop, zipCode))          // step 2: transform
.sorted(Comparator.comparingDouble(ShopSearchResponse::distance)) // step 3: sort
.limit(20)                                   // step 4: limit
.toList();                                   // terminal

Each step is a pure function — no side effects, easy to test in isolation.
Adding a step = adding one more intermediate operation, nothing else changes.

Pipeline with Function composition:
Function<List<Shop>, List<Shop>> activeOnly    = shops -> shops.stream().filter(Shop::isActive).toList();
Function<List<Shop>, List<Shop>> nearbyOnly    = shops -> shops.stream().filter(s -> s.distance() < 5.0).toList();
Function<List<Shop>, List<Shop>> sortedByDist  = shops -> shops.stream().sorted(...).toList();

Function<List<Shop>, List<Shop>> fullPipeline = activeOnly.andThen(nearbyOnly).andThen(sortedByDist);

List<Shop> result = fullPipeline.apply(allShops);

### Bulkhead — Concurrency Limits

Bulkhead isolates parts of a system so that a failure in one part
doesn't take down everything else.
Named after ship bulkheads — compartments that contain flooding.

Thread pool bulkhead:
Each service or feature gets its own thread pool.
If one pool is exhausted → only that feature degrades → others continue.

ExecutorService searchPool       = Executors.newFixedThreadPool(20);
ExecutorService notificationPool = Executors.newFixedThreadPool(5);
ExecutorService reportPool       = Executors.newFixedThreadPool(3);

// Search overloaded → searchPool fills up → RejectedExecutionException
// notificationPool and reportPool are unaffected

With Virtual Threads:
Bulkhead by semaphore — not by thread count (virtual threads are cheap):

Semaphore searchLimit = new Semaphore(200); // max 200 concurrent search operations

searchLimit.acquire();
try {
return performSearch(query);
} finally {
searchLimit.release();
}

Trade-offs:
+ Failure containment — one service's load doesn't kill others
+ Predictable degradation — affected service degrades gracefully
- More complex configuration — pool sizing requires tuning
- Resource allocation upfront — pools consume memory even when idle

Rakuten connection:
Tirebringin — if email/SMS notifications were backed by their own small thread pool,
a notification storm would not have affected the reservation processing thread pool.
Two separate pools = two separate failure domains.

Interview tip:
"Bulkhead is about failure isolation through resource partitioning.
With platform threads, partition by thread pool.
With virtual threads, partition by semaphore — virtual threads are cheap enough
that the limit is about logical concurrency, not resource cost."