# [1.1] Java Core

## HashMap — How It Works

Key-value store. Most used methods: get(), put(), getOrDefault(), containsKey().

Default capacity: 16 buckets.
Load factor: 0.75 — when 75% full (12 elements) → capacity doubles → rehashing.

Why 0.75?
Too low → resizes too early → memory waste
Too high → too many collisions → slow
0.75 is the sweet spot.

How it stores:
1. hashCode(key) is calculated
2. bucket index = hashCode & (capacity - 1) — bitwise AND, faster than %
3. key-value stored in that bucket

Collision:
Two different keys → same hashCode → same bucket.
This is normal — hashCode is not unique.

Java 7 and before: LinkedList inside bucket — O(n) lookup
Java 8+: if bucket has 8+ elements → Red-Black Tree — O(log n) lookup

Contract:
equals() true → hashCode() must be same (required)
hashCode() same → equals() may NOT be true (this is collision — expected)

If you use an object as a HashMap key → always override equals() AND hashCode().
If you don't → two "equal" objects will go to different buckets → get() returns null.

Rehashing:
When 75% full → capacity doubles (16 → 32 → 64...)
All elements recalculated and redistributed — O(n) operation.
Tip: if you know the size upfront → new HashMap<>(initialCapacity) to avoid rehashing.

The question I ask myself:
"Why did get() return null even though I put the key in?"
→ Almost always: equals() or hashCode() not overridden.

## HashSet — How It Works

Stores unique elements only. No key-value — just keys.

Internally: HashSet is a wrapper around HashMap.
add("Berat") → map.put("Berat", PRESENT) — value is always a dummy object.
Uniqueness guaranteed because HashMap doesn't allow duplicate keys.

When to use:
+ "Does this element exist?" → contains() → O(1)
+ Need unique collection, no duplicates
+ Visited URLs, processed IDs, seen elements

HashSet vs HashMap:
HashSet → just keys, uniqueness, membership check
HashMap → key-value pairs, lookup by key

The question I ask myself:
"Do I need to store a value, or just check existence?"
+ Just existence → HashSet
+ Need associated value → HashMap

## ArrayList vs LinkedList

ArrayList:
→ Dynamic array — elements stored contiguously in memory
→ Read by index: O(1) — direct access
→ Add/remove middle: O(n) — shift required
→ CPU cache friendly — fast in practice

LinkedList:
→ Nodes connected by pointers
→ Read by index: O(n) — must traverse
→ Add/remove head/tail: O(1)
→ Add/remove middle: O(n) traverse + O(1) pointer change = O(n)
→ Extra memory per node (pointer overhead)

In practice: almost always use ArrayList.
LinkedList only when: frequent head/tail operations, used as Queue/Deque.

The question I ask myself:
"Will I mostly read by index, or mostly add/remove from ends?"
+ Mostly read → ArrayList
+ Mostly add/remove from head/tail → LinkedList (as Deque)

## ConcurrentHashMap — Thread Safety

Problem:
HashMap is not thread-safe.
Multiple threads writing simultaneously → race condition → data corruption.

Three options:
1. HashMap → fastest, single-threaded only, NOT thread-safe
2. SynchronizedMap → locks entire map → only one thread at a time → slow
3. ConcurrentHashMap → locks only the bucket, not the entire map → fast + safe

How ConcurrentHashMap works:
Thread 1 writes to bucket 2 → only bucket 2 locked
Thread 2 writes to bucket 7 → continues without waiting
→ Parallel writes possible → much faster than SynchronizedMap

Java 7: Segment locking (16 segments)
Java 8: Node locking — each bucket has its own lock → more granular → faster

Hashtable — legacy, never use:
All methods synchronized → entire map locked → very slow
Doesn't accept null key/value
Replaced by ConcurrentHashMap

Trade-offs:
+ Thread-safe, parallel reads/writes
+ Much faster than SynchronizedMap under contention
- Slightly more memory than HashMap
- Still slower than HashMap in single-thread

Real world:
Uber → Map<driverId, Location> → ConcurrentHashMap
thousands of drivers updating location simultaneously
Netflix → Map<userId, Session> → ConcurrentHashMap
millions of concurrent users

Rakuten connection:
URL Shortener — ConcurrentHashMap used
Multiple threads generating short URLs simultaneously → no data corruption

The question I ask myself:
"Will multiple threads access this map?"
+ Yes → ConcurrentHashMap, always
+ No → HashMap, faster

## Encapsulation

Make every field private by default.
Only expose what's necessary — and when you do, add validation.

Why private?
→ External code can't put the object in an invalid state
→ Rules live in one place — easy to change later
→ "I control my own state"

Three levels:
1. Public field → no encapsulation, anyone can break it
2. Private + blind getter/setter → weak encapsulation, no rules
3. Private + validation → real encapsulation, object always valid

When is validation needed?
→ If an invalid value is possible → add a rule
→ balance < 0? → validate
→ age > 150? → validate
→ name is any string? → private is enough, add rule later if needed

public static final → exception, constants can be public (they never change)

Interview tip:
Public field → "Encapsulation violation — why is this public?"
Private + no validation → "Weak encapsulation"
Private + validation → "Strong encapsulation"

Real world:
Rakuten — ReserveSaveDataService validated all incoming data before saving
Amazon — Order status can't be set directly, only through cancelOrder(), shipOrder()
Rules live inside the object, not scattered across services

The question I ask myself:
"Can this field be set to an invalid value from outside?"
+ Yes → make it private + add validation
+ No → private is still good practice — rules might come later

## Polymorphism

Same interface, different behavior.
The caller doesn't care HOW it's done — just WHAT it does.

How to recognize it:
→ Interface + multiple Impl classes
→ List liste = new ArrayList() — upper type, lower implementation
→ @Autowired in Spring — you see the interface, Spring injects the Impl
→ Long if/else chain → sign that polymorphism is missing

How Java decides at runtime:
Dynamic dispatch — at runtime, Java checks which object the reference
actually points to → calls that object's method.
Decided at runtime, not compile time.

Real world:
Rakuten — ShopSearchService (interface) → ShopSearchServiceImpl
Spring Data — JpaRepository interface → Spring generates the Impl
Collections — List = new ArrayList() everywhere

Interview tips:
"Wherever you see an interface with multiple implementations — that's polymorphism."
"Spring's DI system is built entirely on polymorphism."
"Long if/else chains are a sign polymorphism is missing — violates OCP."

The question I ask myself:
"If I need to add a new type/behavior, do I need to modify existing code?"
+ Yes → polymorphism is missing
+ No → polymorphism is working correctly

## Composition over Inheritance

Two ways to reuse code:
Inheritance (is-a) → Dog extends Animal
Composition (has-a) → Car has-a Motor, has-a Brake

Rule: prefer composition over inheritance.

Why inheritance is dangerous:
→ You inherit everything — wanted or not
→ Dog extends Animal → Dog gets fly() method → makes no sense
→ Tight coupling — parent changes, child breaks
→ Violates LSP if subclass can't replace parent

Why composition is better:
→ Take only what you need
→ Each piece has one job
→ Easy to swap — change the field, not the hierarchy

How to recognize in code:
extends → ask "is this really is-a?"
field inside a class → composition, normal and good

Real world:
Spring Services — UserService has-a UserRepository, has-a EmailService
Rakuten — ShopSearchService has-a Validator, has-a Formatter, has-a Repository
None of these extend each other — all composition

Connection to SOLID:
Inheritance misuse → LSP violation
Composition → naturally supports SRP and OCP

Interview tip:
"Favor composition over inheritance — inheritance creates tight coupling.
With composition, you include only what you need and swap easily."

The question I ask myself:
"Is this truly is-a, or is it has-a?"
+ is-a → inheritance OK (Dog is an Animal)
+ has-a → composition (Car has a Motor, not Car is a Motor)

## Immutability

An object is immutable if its state cannot change after creation.

4 rules to make a class immutable:
1. Make the class final — no subclassing
2. All fields private + final
3. Set values only through constructor
4. No setters — only getters

public final class Money {
private final double amount;
private final String currency;

    public Money(double amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public double getAmount() { return amount; }

    // Need a change? Return new object:
    public Money add(double extra) {
        return new Money(this.amount + extra, this.currency);
    }
}

Why immutable?
1. Thread-safe by default — can't be modified → no race condition
2. Safe as HashMap keys — hashCode never changes
3. Security — config values, passwords can't be tampered with

Java built-in immutables:
String, Integer, Long, Double, LocalDate, BigDecimal

Defensive copy — the hidden rule:
If a field is a mutable object (List, Date, array), just storing the reference is not enough.
External code can still mutate the object through that reference → immutability broken.

// WRONG — mutable field exposed:
public final class Schedule {
private final List<String> slots;

    public Schedule(List<String> slots) {
        this.slots = slots; // caller still holds reference → can mutate
    }

    public List<String> getSlots() {
        return slots; // returns internal reference → caller can mutate
    }
}

// CORRECT — defensive copy:
public final class Schedule {
private final List<String> slots;

    public Schedule(List<String> slots) {
        this.slots = new ArrayList<>(slots); // copy in constructor
    }

    public List<String> getSlots() {
        return Collections.unmodifiableList(slots); // copy on return
    }
}

Two places to apply defensive copy:
→ Constructor: copy mutable input, don't trust the caller
→ Getter: return unmodifiable view, don't expose internal state

Real world:
Rakuten — DB config loaded once, never changes
Revolut — Money object immutable, every operation returns new Money
Spring — @Value injected config fields should be final

Interview tip:
"Immutable objects are inherently thread-safe.
Since they can't be modified, no synchronization needed.
The subtle trap is mutable fields — without defensive copy,
the 4 rules alone are not enough."

The question I ask myself:
"Should this object ever change after creation?"
+ No → make it immutable
  "Does it have mutable fields?"
+ Yes → apply defensive copy in constructor and getter

## equals() / hashCode() / compareTo()

Three methods that define object identity and ordering.
All three have contracts — breaking them causes silent, hard-to-find bugs.

---

equals() — logical equality

Default (Object): compares references — same object in memory.
Override when: two different objects with same data should be "equal."

public class Money {
private final double amount;
private final String currency;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money m)) return false;
        return amount == m.amount && currency.equals(m.currency);
    }
}

equals() contract:
→ Reflexive: x.equals(x) must be true
→ Symmetric: x.equals(y) == y.equals(x)
→ Transitive: if x.equals(y) and y.equals(z) → x.equals(z)
→ Consistent: same result every time unless object changes
→ x.equals(null) must return false, never throw

---

hashCode() — bucket address

Rule: if equals() returns true → hashCode() must return same value.
If you override equals(), you MUST override hashCode(). Always. No exception.

Why?
HashMap uses hashCode() first to find the bucket, then equals() to find the key.
If two "equal" objects have different hashCodes → they go to different buckets
→ get() returns null even though the key "exists."

public class Money {
@Override
public int hashCode() {
return Objects.hash(amount, currency);
}
}

The contract ihlali bug:
class Order {
private String id;
// equals() overridden, hashCode() NOT overridden
}

Order o1 = new Order("123");
Order o2 = new Order("123");

o1.equals(o2) → true  (custom equals)
o1.hashCode() != o2.hashCode() → different buckets

Map<Order, String> map = new HashMap<>();
map.put(o1, "first");
map.get(o2) → null  // contract broken, bug impossible to trace

---

compareTo() — natural ordering

Used for sorting. Implement Comparable<T> when the class has a natural order.

public class Money implements Comparable<Money> {
@Override
public int compareTo(Money other) {
return Double.compare(this.amount, other.amount);
}
}

Return values:
negative → this < other
0        → this == other
positive → this > other

Used by: Collections.sort(), TreeMap, TreeSet, sorted streams.

---

Comparable vs Comparator

Comparable → natural order, defined inside the class (one order only)
Comparator → external order, defined outside (multiple orderings possible)

// Natural order — inside class:
class Employee implements Comparable<Employee> {
public int compareTo(Employee other) {
return this.name.compareTo(other.name); // alphabetical by default
}
}

// Custom order — outside class:
Comparator<Employee> bySalary = Comparator.comparingInt(Employee::getSalary);
Comparator<Employee> byAge    = Comparator.comparingInt(Employee::getAge);

list.sort(bySalary);
list.sort(byAge);

Rakuten connection:
ShopSearchService — shops sorted by distance:
.sorted(Comparator.comparingDouble(ShopSearchItem::getDistance))
→ Comparator used, not Comparable — because distance is not a "natural" property of a shop

Real world:
TreeMap → uses compareTo() to maintain sorted keys
PriorityQueue → uses compareTo() or Comparator to determine priority
Elasticsearch result ranking → Comparator chain (score, then date, then id)

Interview tip:
"Override equals() and hashCode() together — always. Breaking one without the other
causes silent bugs in HashMap and HashSet that are extremely hard to trace.
Comparable is for natural order inside the class, Comparator is for external, flexible ordering."

The question I ask myself:
"Do I need to put this object in a HashMap or HashSet?"
+ Yes → override both equals() and hashCode()
  "Does this class have a single natural ordering?"
+ Yes → implement Comparable
  "Do I need multiple orderings, or ordering from outside?"
+ Yes → use Comparator

## Stream API

Pipeline of 3 parts:
1. Source → where data comes from (list, array, set)
2. Intermediate → what to do (lazy — nothing runs until terminal)
3. Terminal → how to finish (triggers execution)

list.stream()           // source
.filter(...)        // intermediate
.map(...)           // intermediate
.toList();          // terminal — execution starts here

Lazy evaluation:
Intermediate operations don't execute without a terminal operation.
JVM can optimize the pipeline before running.

Short-circuit:
Each element passes through the full pipeline one by one.
Not: all elements through filter, then all through map.
But: element 1 → filter → map, element 2 → filter → map...

Most used operations:
Intermediate: filter, map, flatMap, distinct, sorted, limit, skip
Terminal: toList, collect, count, findFirst, anyMatch, allMatch, reduce

flatMap:
Flattens nested structures.
List<List<T>> → flatMap → List<T>
Optional → flatMap(Optional::stream) → empty optionals dropped automatically

Parallel Stream:
.parallelStream() → splits list across multiple CPU cores → faster for large lists

Trade-offs:
+ Large lists (100k+) → significant speedup
- Small lists → overhead worse than benefit
- No ordering guarantee
- Not safe with stateful operations or shared mutable state

Primitive Streams — avoid boxing:
int/long/double → boxing to Integer/Long/Double → object creation → GC pressure
IntStream, LongStream, DoubleStream → no boxing → faster for numeric operations

int vs Integer:
int → primitive, stack, cannot be null, fast
Integer → object, heap, can be null, slower
Use Integer when: null needed, used in collections (List<Integer>)
Use int when: calculations, method variables

Custom Collector — when built-in is not enough:
Standard collectors (toList, groupingBy, joining) cover most cases.
But sometimes I need domain-specific aggregation → custom Collector.

// Problem: group shops by city and count active ones per city
// Built-in groupingBy + counting works here:
Map<String, Long> activePerCity = shops.stream()
.filter(Shop::isActive)
.collect(Collectors.groupingBy(Shop::getCity, Collectors.counting()));

// More complex: custom summary object per category
Map<String, ShopSummary> summary = shops.stream()
.collect(Collectors.groupingBy(
Shop::getCategory,
Collectors.collectingAndThen(
Collectors.toList(),
list -> new ShopSummary(list.size(), list.stream()
.mapToDouble(Shop::getRating).average().orElse(0.0))
)
));

Key Collector combinators:
Collectors.groupingBy(classifier)                → Map<K, List<T>>
Collectors.groupingBy(classifier, downstream)    → Map<K, R> with custom aggregation
Collectors.counting()                            → Long count per group
Collectors.summingInt(mapper)                    → sum per group
Collectors.averagingDouble(mapper)               → average per group
Collectors.collectingAndThen(downstream, finisher) → transform final result
Collectors.toUnmodifiableList()                  → immutable result

Real world:
Rakuten — ShopSearchService:
shops.stream()
.map(shop -> createShopSearchItem(shop, zipCode))
.sorted(Comparator.comparingDouble(ShopSearchItem::getDistance))
.toList()

Netflix → filter highly rated movies → map to genre → distinct genres
Amazon → filter pending orders → sort by date → limit 100 → process

Interview tips:
"Streams are lazy — nothing runs without a terminal operation."
"Parallel streams aren't always faster — benchmark first."
"Use IntStream for numeric operations to avoid boxing overhead."
"groupingBy + downstream collector is the key to complex aggregations."

The question I ask myself:
"Am I transforming a collection?" → Stream
"Do I need parallel processing on large data?" → parallelStream (carefully)
"Am I doing numeric aggregation?" → IntStream/LongStream
"Do I need grouping + aggregation?" → groupingBy + downstream collector

## Stream — Quick Reference

I want to convert:
→ map() → Stream<T> → Stream<R>
→ mapToInt() → Stream<T> → IntStream (for numerical operations)
→ mapToLong() → Stream<T> → LongStream
→ mapToDouble() → Stream<T> → DoubleStream

I want to filter:
→ filter() → conditional filtering

I want to remove duplicates:
→ distinct()

I want to count:
→ count()

I want to sum:
→ mapToInt().sum()

I want an average:
→ mapToInt().average().orElse(0.0)

Largest/smallest:
→ mapToInt().max()
→ mapToInt().min()

To add to a list:
→ toList()

Sorting:
.sorted()                                        → alphabetical
.sorted(Comparator.comparingInt(String::length)) → by length
.sorted(Comparator.reverseOrder())               → reverse alphabetical
.sorted(Comparator.comparingInt(String::length).reversed()) → longest first

Finding max/min element (not the value):
.max(Comparator.comparingInt(String::length))    → longest String → Optional<String>
.min(Comparator.comparingInt(String::length))    → shortest String → Optional<String>

Matching:
.anyMatch(s -> s.equals("Berat"))  → at least one matches → boolean
.allMatch(s -> s.length() > 3)     → all match → boolean
.noneMatch(s -> s.isEmpty())       → none match → boolean

Joining:
.collect(Collectors.joining(", "))           → "Berat, Ali, Ayse"
.collect(Collectors.joining(", ", "[", "]")) → "[Berat, Ali, Ayse]"

Common mistakes:
s.length    → s.length()   — it's a method, not a field
!==         → !=           — not a valid operator
String::toUpperCase   — no parentheses in method reference
String::toUpperCase() — no parentheses allowed

Even/odd:
n % 2 == 0  → even
n % 2 != 0  → odd
n % 3 == 0  → divisible by 3

To find a single element:
→ findFirst()
→ findAny()

Does it all match:
→ allMatch()
→ anyMatch()
→ noneMatch()

## Optional

Designed for return types only — to express "this might not exist."
Replaces null returns from methods, especially repository calls.

// Correct usage — return type:
public Optional<User> findById(Long id) { ... }

// How to use it properly:
optional.orElse(defaultValue)           // return default if empty
optional.orElseThrow(() -> new Ex())    // throw if empty
optional.ifPresent(u -> process(u))     // run if present
optional.map(User::getName)             // transform if present
.orElse("Unknown")

// Most common mistake:
optional.get() // throws NoSuchElementException if empty
// never use get() without checking

Anti-patterns:
1. Field → never use Optional as a class field
   private Optional<String> phone; //
   → Not serializable, Jackson issues, memory overhead
   → Use String phone; // null is fine as a field

2. Method parameter → never pass Optional as parameter
   public void update(Optional<String> name) //
   → Use String name instead, null can be passed

3. Nested Optional → never
   Optional<Optional<String>> // always a design mistake

Real world:
Rakuten — zipCodeRepository.findByZipCode(zipCode) → Optional<ZipCode>
.orElseThrow(() -> new ValidationException("postal code does not exist"))
Spring Data — findById() returns Optional by default

Interview tip:
"Optional is for return types only — not fields or parameters.
Never call get() directly — always use orElse or orElseThrow."

The question I ask myself:
"Can this method return nothing?"
+ Yes → Optional return type
  "Is this a field or parameter?"
+ Yes → don't use Optional, use null or empty string

## Functional Interfaces

A functional interface has exactly one abstract method.
Lambda = short way to implement a functional interface.

4 core functional interfaces:

Predicate<T> → takes T, returns boolean (test something)
Predicate<String> longName = s -> s.length() > 5;
longName.test("Berat"); // false
→ Used in: stream.filter()

Function<T, R> → takes T, returns R (transform something)
Function<String, Integer> length = s -> s.length();
length.apply("Berat"); // 5
→ Used in: stream.map()

Supplier<T> → takes nothing, returns T (produce something)
Supplier<String> greeting = () -> "Hello!";
greeting.get(); // "Hello!"
→ Used in: optional.orElseThrow(() -> new Exception())

Consumer<T> → takes T, returns nothing (consume something)
Consumer<String> print = s -> System.out.println(s);
print.accept("Berat");
→ Used in: stream.forEach()

Method reference — shorthand for lambda:
s -> s.toUpperCase()  ==  String::toUpperCase
s -> System.out.println(s)  ==  System.out::println
this::createTireSizeSafely  ==  s -> this.createTireSizeSafely(s)

@FunctionalInterface annotation:
Marks an interface as functional.
Compiler error if you add a second abstract method — safe contract.

@FunctionalInterface
public interface Calculator {
int calculate(int a, int b);
}
Calculator add      = (a, b) -> a + b;
Calculator multiply = (a, b) -> a * b;

Side-effect isolation:
// WRONG — mutable shared state in lambda:
List<String> results = new ArrayList<>();
list.stream().forEach(s -> results.add(s)); // not thread-safe

// CORRECT — no side effects:
List<String> results = list.stream().toList();

Rakuten connection:
.map(this::createTireSizeSafely)  → Function
.flatMap(Optional::stream)         → Function
.filter(shop -> shop.isActive())   → Predicate
.forEach(s -> process(s))          → Consumer
optional.orElseThrow(() -> new ValidationException()) → Supplier

Real world:
Spring @Bean methods → Supplier pattern
Stream pipelines → combination of all four
Event handlers → Consumer pattern

Interview tip:
"The four core functional interfaces cover every use case:
test something → Predicate
transform something → Function
produce something → Supplier
consume something → Consumer
Lambdas should be side-effect free — especially in parallel streams."

The question I ask myself:
"Does this lambda test, transform, produce, or consume?"
→ That tells me which functional interface I'm using.

## Specification Pattern

Problem:
Business rules pile up inside repository or service methods.
findByAgeGreaterThanAndCityAndActiveTrue() → fragile, unreadable, impossible to combine.

Solution:
Each rule becomes a self-contained object — a Specification.
Rules can be combined with and(), or(), not() → composable query logic.

public interface Specification<T> {
boolean isSatisfiedBy(T entity);

    default Specification<T> and(Specification<T> other) {
        return entity -> this.isSatisfiedBy(entity) && other.isSatisfiedBy(entity);
    }

    default Specification<T> or(Specification<T> other) {
        return entity -> this.isSatisfiedBy(entity) || other.isSatisfiedBy(entity);
    }

    default Specification<T> not() {
        return entity -> !this.isSatisfiedBy(entity);
    }
}

// Rules as objects:
Specification<Shop> isActive   = shop -> shop.isActive();
Specification<Shop> inTokyo    = shop -> shop.getCity().equals("Tokyo");
Specification<Shop> highRating = shop -> shop.getRating() >= 4.5;

// Composable — no new code needed:
Specification<Shop> premium = isActive.and(inTokyo).and(highRating);

List<Shop> result = shops.stream()
.filter(premium::isSatisfiedBy)
.toList();

Spring Data integration:
Spring Data JPA has its own Specification interface.
Specifications translate to JPA Criteria queries — dynamic SQL at the DB level.

shopRepository.findAll(isActive.and(inTokyo));

Trade-offs:
+ Rules are named, testable, reusable
+ Combination is flexible — no if/else needed
+ New rule = new class, nothing changes
- Overhead for simple cases — not worth it for 1-2 fixed filters
- JPA Criteria API is verbose → use QueryDSL or Spring Data Specification instead

Rakuten connection:
ShopSearchService had filter conditions scattered across the method.
A Specification chain would have made each condition a named, testable unit.

Interview tip:
"Specification pattern is domain logic as objects — each rule is a class,
rules compose naturally. It's OCP in action: add a new filter rule without touching existing code."

The question I ask myself:
"Are my filter/query conditions growing and mixing with infrastructure code?"
+ Yes → extract them into Specifications

## Strategy Pattern

Problem:
Business logic that varies by case lives inside if/else or switch blocks.
Every new case → modify the existing class → OCP violation.

Solution:
Each algorithm/behavior becomes its own class.
The context class holds a reference to the strategy interface, not a concrete class.

// Strategy interface:
public interface ShippingStrategy {
double calculate(Order order);
}

// Strategies:
public class StandardShipping implements ShippingStrategy {
public double calculate(Order order) {
return order.getWeight() * 1.5;
}
}

public class ExpressShipping implements ShippingStrategy {
public double calculate(Order order) {
return order.getWeight() * 3.0 + 10.0;
}
}

public class FreeShipping implements ShippingStrategy {
public double calculate(Order order) {
return 0.0;
}
}

// Context — doesn't know which strategy runs:
public class OrderService {
private final ShippingStrategy shippingStrategy;

    public OrderService(ShippingStrategy shippingStrategy) {
        this.shippingStrategy = shippingStrategy;
    }

    public double getShippingCost(Order order) {
        return shippingStrategy.calculate(order); // runtime decision
    }
}

// Injection decides which strategy runs:
OrderService service = new OrderService(new ExpressShipping());

Connection to DIP:
OrderService depends on ShippingStrategy (interface), not a concrete class.
Strategy + DIP = loose coupling + easy to test + easy to extend.

Connection to OCP:
New shipping type → new class, zero changes to OrderService.

Trade-offs:
+ Easy to add new behaviors without touching existing code
+ Each strategy independently testable
+ Clean swap at runtime or via config/injection
- More classes for simple cases
- Can be overkill if there are only 2 fixed behaviors

Rakuten connection:
Different notification channels (email, SMS, push) → NotificationStrategy
Each channel is a strategy — OrderService doesn't care which one runs.

Real world:
Payment processors → StripeStrategy, PayPalStrategy, BankTransferStrategy
Discount calculation → PercentageDiscount, FixedDiscount, MemberDiscount
Sorting algorithms → QuickSort, MergeSort, TimSort — same interface, different impl

Interview tip:
"Strategy pattern eliminates branching logic by turning behaviors into objects.
It's polymorphism applied deliberately — the context doesn't decide,
the injected strategy does."

The question I ask myself:
"Do I have an if/else or switch that selects a behavior at runtime?"
+ Yes → extract each branch into a Strategy class

## Null Object Pattern

Problem:
Code is full of null checks. Every caller must remember to check.
One forgotten null check → NullPointerException in production.

if (user != null) {
if (user.getAddress() != null) {
if (user.getAddress().getCity() != null) {
// finally do something
}
}
}

Solution:
Instead of returning null, return an object that does nothing (safe default).
The caller never needs to check — just calls the method.

// Interface:
public interface Logger {
void log(String message);
}

// Real implementation:
public class ConsoleLogger implements Logger {
public void log(String message) {
System.out.println(message);
}
}

// Null Object — does nothing, but safe to call:
public class NoOpLogger implements Logger {
public void log(String message) {
// intentionally empty — no logging
}
}

// Caller — no null check needed:
public class OrderService {
private final Logger logger;

    public OrderService(Logger logger) {
        this.logger = logger; // inject NoOpLogger if no logging needed
    }

    public void processOrder(Order order) {
        logger.log("Processing: " + order.getId()); // always safe
    }
}

Null Object vs Optional:
Optional → "this value might be absent, caller decides what to do"
Null Object → "there is always an object, it just does nothing"

Use Optional when: the absence itself is meaningful and the caller must handle it.
Use Null Object when: the caller shouldn't need to know or care about absence.

Real world:
Spring's EmptyResultDataAccessException vs returning empty list → empty list is a Null Object
Collections.emptyList() → Null Object for collections — never null, always iterable
Logging frameworks — disabled logger levels do nothing without null checks

Trade-offs:
+ Eliminates null checks at call sites
+ Simplifies callers — uniform interface always
- Hidden behavior — null object silently does nothing, might mask bugs
- Not appropriate when absence MUST be handled differently

Interview tip:
"Null Object replaces null with an object that has safe, do-nothing behavior.
The caller is always clean. The trade-off is that it can hide real absence —
so use it when the absent case truly has no meaningful behavior."

The question I ask myself:
"Is this null check repeated everywhere, always resulting in 'do nothing'?"
+ Yes → Null Object pattern
  "Does the caller need to know about absence?"
+ Yes → Optional is the better fit