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