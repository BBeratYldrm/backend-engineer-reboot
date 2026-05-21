# Java Interview Crash Course

## Keywords
java-core · streams · comparable · comparator · hashmap-internal ·
concurrency · thread-safe · singleton · deadlock · completablefuture ·
concurrent-hashmap · spring-boot · exception-handling · retry · pagination ·
kafka-idempotency · checked-exception · unchecked-exception · lru-cache ·
producer-consumer · immutable-class · sql · joins · pagination-sql

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
class Employee implements Comparable<Employee> {
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
List<List<Order>> nested = users.stream()
    .map(user -> user.getOrders())
    .collect(Collectors.toList());

// flatMap — flattened
List<Order> allOrders = users.stream()
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
CompletableFuture<String> user = CompletableFuture.supplyAsync(() -> getUser());
CompletableFuture<String> orders = CompletableFuture.supplyAsync(() -> getOrders());

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
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
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
Page<User> findAll(Pageable pageable);

// Controller
@GetMapping("/users")
public Page<User> getUsers(
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

---

## Module 4 — Coding Problems

### LRU Cache

LRU = Least Recently Used — evicts the element that has not been used for the longest time.

Use case: in-memory cache with limited capacity.
Production: Redis handles this automatically with eviction policies.
Interview: tests understanding of HashMap + LinkedList combination.

```java
public class LRUCache {

    private final int capacity;
    private final LinkedHashMap<Integer, Integer> cache;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
                return size() > capacity;
            }
        };
    }

    public int get(int key) {
        return cache.getOrDefault(key, -1);
    }

    public void put(int key, int value) {
        cache.put(key, value);
    }
}
```

Why LinkedHashMap?
- accessOrder=true → every get/put moves that element to the tail
- Head = oldest (least recently used), Tail = newest
- removeEldestEntry → automatically evicts when size exceeds capacity

Why not just HashMap?
- HashMap has no ordering — cannot track which element was used least recently

Key insight:
- final on a field → reference cannot change
- final does not protect the content of mutable objects (List, Map)
- defensive copy needed for mutable fields

---

### Producer-Consumer

Classic multi-threading problem — producer generates data, consumer processes it
at a different speed. Coordination needed to avoid race conditions.

```java
private static final BlockingQueue<Integer> queue = new LinkedBlockingQueue<>(5);

static class Producer implements Runnable {
    public void run() {
        for (int i = 1; i <= 10; i++) {
            try {
                queue.put(i);       // blocks if queue is full
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

static class Consumer implements Runnable {
    public void run() {
        for (int i = 1; i <= 10; i++) {
            try {
                int item = queue.take(); // blocks if queue is empty
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
```

BlockingQueue:
- put() → blocks when full, waits for consumer to take
- take() → blocks when empty, waits for producer to add
- No manual wait/notify needed

Without BlockingQueue → manual synchronized + wait() + notify()

---

### Immutable Class

Immutable = once created, state cannot change.
Examples in Java: String, Integer, LocalDate

Why use it?
- Thread-safe by default — multiple threads can read without locking
- Safe as HashMap key — hash never changes
- Predictable — no unexpected state changes

4 rules to write an immutable class:
1. Class must be final — cannot be extended
2. Fields must be private final — cannot be reassigned
3. No setters
4. Defensive copy for mutable fields

```java
public final class ImmutableTeam {
    private final String name;
    private final List<String> members;

    public ImmutableTeam(String name, List<String> members) {
        this.name = name;
        this.members = new ArrayList<>(members); // defensive copy on input
    }

    public String getName() { return name; }

    public List<String> getMembers() {
        return Collections.unmodifiableList(members); // defensive copy on output
    }
}
```

Why defensive copy?
- final protects the reference, not the content
- Without it: getMembers().add("x") would mutate the internal list
- unmodifiableList → throws UnsupportedOperationException on modification

---

## Module 5 — SQL

### 2nd Highest Salary

```sql
-- Option 1: subquery
SELECT MAX(salary)
FROM employee
WHERE salary < (SELECT MAX(salary) FROM employee);

-- Option 2: cleaner
SELECT DISTINCT salary
FROM employee
ORDER BY salary DESC
LIMIT 1 OFFSET 1;
```

DISTINCT → eliminates duplicate salaries before ranking
OFFSET 1 → skip the first row (highest), return the second

---

### WHERE vs HAVING

WHERE → filters rows BEFORE GROUP BY — row level
HAVING → filters groups AFTER GROUP BY — group level

```sql
-- WHERE: filter individual rows
SELECT * FROM employee
WHERE salary > 3000;

-- HAVING: filter after aggregation
SELECT department, AVG(salary)
FROM employee
GROUP BY department
HAVING AVG(salary) > 5000;
```

Rule: aggregate functions (AVG, SUM, COUNT) in filter condition → HAVING

---

### INNER JOIN vs LEFT JOIN

```sql
-- INNER JOIN: only matching rows from both tables
SELECT e.name, d.name
FROM employee e
INNER JOIN department d ON e.dept_id = d.id;

-- LEFT JOIN: all rows from left table, NULL if no match on right
SELECT e.name, d.name
FROM employee e
LEFT JOIN department d ON e.dept_id = d.id;
```

- INNER JOIN → intersection, both sides must have matching value
- LEFT JOIN → all left rows included, right side NULL if no match

When to use:
- "Employees who have a department" → INNER JOIN
- "All employees, show department if exists" → LEFT JOIN

---

### Remove Duplicate Rows

```sql
-- Find duplicates
SELECT name, salary, COUNT(*)
FROM employee
GROUP BY name, salary
HAVING COUNT(*) > 1;

-- Delete duplicates, keep one
DELETE FROM employee
WHERE id NOT IN (
    SELECT MIN(id)
    FROM employee
    GROUP BY name, salary
);
```

Keep the row with the smallest id, delete the rest.

---

### Department-wise Max Salary

```sql
SELECT department, MAX(salary)
FROM employee
GROUP BY department;
```