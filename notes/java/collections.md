# Java Collections

## ArrayList
- Dynamic array
- Fast access (O(1))
- Slow insert/delete (O(n))

## LinkedList
- Node based structure
- Fast insert/delete
- Slow access
- 
## ArrayList vs LinkedList — Real World

| Operation | ArrayList | LinkedList |
|-----------|-----------|------------|
| get(index) | O(1) fast | O(n) slow |
| add(end) | O(1) amortized | O(1) |
| add(beginning) | O(n) slow | O(1) fast |

**Real world: Always use ArrayList.**
LinkedList only when used as Queue/Deque
(add/remove from both ends only).

**Why ArrayList wins in practice:**
- Memory is contiguous → CPU cache friendly
- Random access is very common
- Middle insertions are rare

## HashMap
- Key-value structure
- Average O(1)
- Uses hashCode()

## HashSet
- No duplicates
- Backed by HashMap


## HashMap Internals

- 16 buckets by default
- put("key", value):
    1. hashCode("key") → which bucket?
    2. If bucket has elements → equals() to check duplicates
- get("key"):
    1. hashCode("key") → go directly to bucket → O(1)
    2. If collision → equals() check → O(n) for that bucket

## Collision
- Two keys → same bucket
- HashMap chains them (linked list in bucket)
- Too many collisions → O(1) becomes O(n)

## Load Factor (default 0.75)
- 16 buckets × 0.75 = 12 elements
- After 12 elements → resize to 32 buckets
- All elements redistributed (rehashing)

## Interview answer:
"HashMap uses hashCode to find the bucket in O(1),
then equals to verify the key. Load factor controls
when resizing happens to avoid too many collisions."