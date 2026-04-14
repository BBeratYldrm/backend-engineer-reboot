# [3.1] SQL — Index & Performance

## What is an Index?
A data structure that speeds up data retrieval.
Like the index at the back of a book — instead of reading every page,
you go directly to what you need.

---

## Without Index — Full Table Scan
SELECT * FROM orders WHERE customer_id = 1;
10M rows → DB checks every single row → O(n) → slow
This is what caused the 8 second response time at Rakuten.

## With Index — B-Tree
DB navigates a tree structure → finds the row directly → O(log n)
10M rows → ~23 steps instead of 10M

How B-Tree works:
12
/    \
3        20
/ \      /  \
1   7   15    25

Looking for customer_id = 7:
→ 12: is 7 < 12? Yes → go left
→ 3: is 7 > 3? Yes → go right
→ 7 found 
Always O(log n) — doesn't matter if it's 7 or 7 million rows.

---

## Index Types

B-Tree (default):
→ Works for WHERE, ORDER BY, BETWEEN, >,
→ Most common, use this by default

Hash:
→ Only works for exact match (=)
→ WHERE email = 'x' → fast
→ WHERE email LIKE 'x%' → doesn't work
→ Rarely used

Partial:
→ Index only for rows matching a condition
→ CREATE INDEX idx_active ON users(email) WHERE status = 'ACTIVE'
→ Why: if 90% of rows are deleted, normal index wastes space
→ Only index the relevant subset

Covering:
→ Include all columns the query needs in the index itself
→ Avoids going back to the main table ("table lookup")
→ CREATE INDEX idx_covering ON users(customer_id, name, email)
→ SELECT name, email WHERE customer_id = 1 → everything in index → fastest

---

## When to Add Index
+ WHERE clauses — frequently filtered columns
+ JOIN columns
+ ORDER BY columns
+ Unique columns (email, username — these get unique indexes automatically)

## When NOT to Add Index
- Low cardinality columns (gender, status — few unique values, not worth it)
- High write tables (logs — every insert must update all index trees)
- Too many indexes on one table — writes slow down significantly

## The Trade-off
Index = faster reads, slower writes.

Every INSERT / UPDATE / DELETE:
→ Updates the row in the table
→ Updates EVERY index on that table
→ 10 indexes = 10 extra update operations per write

This applies to ALL index types — not just B-Tree.

---

## EXPLAIN — Reading the Output

EXPLAIN SELECT * FROM orders WHERE customer_id = 1;

type column (most important):
const  → single row via primary key → fastest 
ref    → index used → good 
range  → index used for range scan → acceptable
ALL    → full table scan → bad 

key column:
NULL         → no index used → bad 
idx_customer → this index was used → good 

rows column:
1          → scanned 1 row → great 
10000000   → scanned 10M rows → bad 

Extra column:
Using index      → covering index working, no table lookup → great 
Using filesort   → extra sort step needed → bad 
Using temporary  → temporary table created → bad 

Real usage at Rakuten:
Ran EXPLAIN on slow queries → found type: ALL, key: NULL
→ Added indexes → response time dropped from 8s to under 3s