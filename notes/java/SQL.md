# [3.1] SQL — Index & Performance

## Without Index — Full Table Scan
SELECT * FROM orders WHERE customer_id = 1;
10M rows → DB checks every single row → O(n) → slow

## With Index — B-Tree
Index = like a book index
DB navigates the tree → finds the row directly → O(log n)
10M rows → ~23 steps

## EXPLAIN — How to detect problems
EXPLAIN SELECT * FROM orders WHERE customer_id = 1;

Key indicators:
type: ALL  → full table scan → BAD
type: ref  → index used → GOOD
type: const → single row, fastest → BEST

rows: 10000000 → scanned 10M rows → BAD
rows: 1        → scanned 1 row → GOOD

key: NULL         → no index → BAD
key: idx_customer → index used → GOOD

## When to add index
+ WHERE clauses (frequently filtered columns)
+ JOIN columns
+ ORDER BY columns
+ Unique columns (email, username)

## When NOT to add index
- Low cardinality columns (gender, status — few unique values)
- High write tables (logs — every insert updates the index tree)
- Too many indexes → writes slow down significantly

## Trade-off
Index → faster reads, slower writes
Every INSERT/UPDATE/DELETE must also update all index trees.