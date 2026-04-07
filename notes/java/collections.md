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