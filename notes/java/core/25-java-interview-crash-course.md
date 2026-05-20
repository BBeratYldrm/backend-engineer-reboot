# [J-25] Java Interview Crash Course

## Keywords
java-core · streams · comparable · comparator · hashmap-internal ·
concurrency · thread-safe · singleton · deadlock · completablefuture ·
concurrent-hashmap · spring-boot · exception-handling · retry · pagination ·
kafka-idempotency · checked-exception · unchecked-exception

---

## Module 1 — Java Core & Collections

### HashMap Internal Working

- put(key, value) → hashCode calculated → bucket found → equals check
- Same bucket occupied → Collision
- Before Java 8 → LinkedList (O(n) search)
- After Java 8 → exceeds 8 elements → converts to Red-Black Tree (O(log n))
- Default capacity: 16, Load factor: 0.75
- At 12 elements → rehash → capacity doubles to 32

Interview answer:
"hashCode determines the bucket. On collision, elements are chained in the
same bucket. Since Java 8, when a bucket exceeds 8 elements, the LinkedList
converts to a Red-Black Tree, improving search from O(n) to O(log n).
At 75% capacity, rehash occurs and capacity doubles."

---

### Comparable vs Comparator

Comparable:
- Implemented inside the class
- Defines one natural ordering
- compareTo() method

Comparator:
- Defined outside the class
- Multiple different orderings possible
- Used with lambda

```java
// Comparable
class Employee implements Comparable {
    public int compareTo(Employee other) {
        return this.salary - other.salary;
    }
}

// Comparator
employees.stream()
    .sorted(Comparator.comparingInt(Employee::getSalary))
    .collect(Collectors.toList());
```

When to use which?
- Comparable → natural ordering of the class (like String, Integer)
- Comparator → different sorting strategies needed at runtime

---

### map() vs flatMap()

Stream has 3 parts:
- Source → list.stream(), Arrays.stream()
- Intermediate → filter(), map(), sorted() — lazy, nothing runs without terminal
- Terminal → collect(), count(), forEach() — triggers execution

Lazy evaluation: nothing is processed until terminal is called → performance benefit
Stream is single-use — closes after terminal operation

map() → transforms each element, 1-to-1 relationship
flatMap() → transforms + flattens nested structures

```java
// map — stays nested
List<List> nested = users.stream()
    .map(user -> user.getOrders())
    .collect(Collectors.toList());

// flatMap — flattened
List allOrders = users.stream()
    .flatMap(user -> user.getOrders().stream())
    .collect(Collectors.toList());
```

---

## Module 2 — Concurrency

### Thread-safe Singleton

Singleton: only one instance of a class can exist.
Use cases: Logger, Config manager, DB connection pool

Problem: two threads can simultaneously see null and create two instances.

Double-Checked Locking — best approach:
```java
private static volatile Singleton instance;

public static Singleton getInstance() {
    if (instance == null) {
        synchronized (Singleton.class) {
            if (instance == null) {
                instance = new Singleton();
            }
        }
    }
    return instance;
}
```

Why volatile?
- Threads may cache the variable in CPU cache
- volatile → "do not cache, always read from main memory"
- volatile is a property of the variable, not the threads

---

### Deadlock

Two threads each hold a lock the other needs → wait forever

```java
// Thread 1: holds lockA, waiting for lockB
// Thread 2: holds lockB, waiting for lockA → Deadlock
```

How to prevent:
1. Fixed lock ordering → always acquire locks in the same order
2. tryLock → if cannot acquire, back off
3. Lock timeout → wait for a limited time, then exit

---

### CompletableFuture

Run independent operations in parallel.

```java
CompletableFuture user = CompletableFuture.supplyAsync(() -> getUser());
CompletableFuture orders = CompletableFuture.supplyAsync(() -> getOrders());

CompletableFuture.allOf(user, orders).join();
```

Trade-off: do not use async for transactional operations.
- Read-heavy, independent operations → parallelize
- Write operations requiring transactions → keep synchronous

Key methods:
- supplyAsync() → run in background, returns result
- thenApply() → transform result when ready
- allOf() → wait for all futures
- join() → get the result

---

### ConcurrentHashMap vs HashMap

HashMap → not thread-safe. Concurrent access during rehash
can cause data loss or infinite loops.

ConcurrentHashMap → locks only the relevant bucket, not the entire map.
Known as segment locking. Both safe and performant.

When to use ConcurrentHashMap:
- Shared state in multi-thread environment
- Short-lived in-memory state
- Request counters, active session tracking

For production caching, prefer Redis:
- Centralized, all instances share the same cache
- Survives restarts
- Supports TTL and eviction policies

---

### Multithreading Story

"I migrated a legacy Kotlin project to Java 21. The project was read-heavy —
fetching user profiles, transaction history, and preferences from separate services.

Initially everything was synchronous — each service call waited for the previous one.
Response times were high.

During migration, I used CompletableFuture to parallelize these independent read
operations. With allOf(), everything starts at once and we wait only as long as
the slowest call takes. Response time dropped significantly.

Write operations and anything requiring transactions remained synchronous —
parallelizing them would mean losing rollback control.

For shared state, I used ConcurrentHashMap instead of HashMap — thread safety
was critical under high concurrent read/write traffic."

---

## Module 3 — Spring Boot

### Exception Handling

Checked Exception → compiler enforces handling (IOException, SQLException)
Unchecked Exception → RuntimeException, optional handling (NullPointerException)

Custom exceptions in Spring extend RuntimeException — avoids polluting code with throws.

Global Exception Handler:
```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handleGeneral(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("Something went wrong"));
    }
}
```

Why @ControllerAdvice?
- Single place to manage all exceptions
- Consistent error response format
- Controllers stay clean — only business logic

---

### Retry Mechanism

When to use:
- External / third-party API calls
- Transient failures — timeouts, temporary downtime

```java
@Retryable(
    retryFor = {HttpServerErrorException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
)
public String callExternalService() {
    return restTemplate.getForObject(url, String.class);
}

@Recover
public String recover(HttpServerErrorException ex) {
    return "fallback response";
}
```

- maxAttempts = 3 → try at most 3 times
- backoff → 1s, 2s, 4s — exponential backoff
- @Recover → fallback when all attempts fail
- Jitter → add random delay to retry intervals to avoid thundering herd

Interview answer:
"Retry is useful when calling external or third-party APIs where transient
failures like timeouts or temporary downtime can occur. Instead of failing
immediately, we retry with exponential backoff to avoid overwhelming the
downstream service."

---

### Pagination

Why: fetching large datasets at once increases response time and memory usage.

```java
// Repository
Page findAll(Pageable pageable);

// Controller
@GetMapping("/users")
public Page getUsers(
    @RequestParam int page,
    @RequestParam int size) {

    return userRepository.findAll(
        PageRequest.of(page, size, Sort.by("name"))
    );
}
```

Response includes: content, totalElements, totalPages, currentPage, size

Offset-based vs Cursor-based:
- Offset-based (Spring Data) → simple, small-medium data
- Cursor-based → large data, infinite scroll, social media feeds

---

### Kafka Duplicate Handling

Problem: Kafka at-least-once guarantee — same message may arrive twice on failure.

Solution — Idempotency:
```java
if (processedOrders.contains(orderId)) {
    return; // duplicate detected, skip
}
processedOrders.add(orderId);
// process the message
```

Production approach:
- Store processed message IDs in Redis or DB
- On duplicate → unique key constraint → skip

Interview answer:
"I use an idempotency key — the unique message ID. Before processing,
I check Redis or DB whether this ID was already handled.
If yes, I skip. If no, I process and record the ID."