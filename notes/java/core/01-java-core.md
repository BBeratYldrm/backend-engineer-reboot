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
+ is-a → inheritance OK (Dog is an Animal ✅)
+ has-a → composition (Car has a Motor, not Car is a Motor ✅)