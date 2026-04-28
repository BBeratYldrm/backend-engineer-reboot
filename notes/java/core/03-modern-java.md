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

When NOT to use:
- JPA Entity — needs to be mutable, requires no-arg constructor
- Service class — has business logic
- Classes that need inheritance — records can't extend classes

Rakuten connection:
ShopSearchItem, ShopSearchResponse → could have been Records
ZipCode entity → should NOT be a Record (JPA entity)

Interview tip:
"Records eliminate DTO boilerplate — constructor, getters, equals, hashCode,
toString all generated automatically. They're immutable by design,
making them perfect for Value Objects and API boundaries."

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
case Circle c    → Math.PI * c.radius() * c.radius()
case Rectangle r → r.width() * r.height()
case Triangle t  → 0.5 * t.base() * t.height()
// no default needed — compiler knows all cases
}

Real world:
Payment results → Success, Failure, Pending — sealed interface
Order status → Created, Confirmed, Shipped, Delivered — sealed class
API responses → sealed with all possible response types

Rakuten connection:
Reservation status could be modeled as sealed class —
only known statuses allowed, compiler enforces completeness

Interview tip:
"Sealed classes give you algebraic data types in Java —
you know exactly what subtypes exist.
Combined with pattern matching, switch becomes exhaustive and safe."

The question I ask myself:
"Is this a closed set of types that will never change?"
+ Yes → sealed class/interface
+ No → regular inheritance

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

Rakuten connection:
Tirebringin had DB queries, ActiveMQ, external API calls — all IO-bound
With Virtual Threads: same hardware, 10x more concurrent reservations

Real world:
Revolut — millions of concurrent financial operations → Virtual Threads ideal
Netflix — thousands of concurrent streaming requests → IO-bound, perfect fit
Uber — driver matching calls multiple services → all IO-bound

Interview tip:
"Virtual Threads give you the simplicity of blocking code
with the scalability of reactive programming.
The key pitfall is pinning — synchronized blocks with IO
prevent the park mechanism. Use ReentrantLock instead."

The question I ask myself:
"Is this IO-bound or CPU-bound?"
+ IO-bound → Virtual Threads → massive scalability gain
+ CPU-bound → Virtual Threads won't help, use ForkJoinPool