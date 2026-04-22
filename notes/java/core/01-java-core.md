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