# [3.1] SQL Deep Dive

## Normalization vs Performance

Normalization: eliminate data repetition, each piece of data lives in one place.
Denormalization: duplicate data intentionally for read performance.

Normalized (3NF):
orders: id | customer_id | status
customers: id | name | city | email
→ JOIN needed, but data is consistent
→ Customer name changes → update one place

Denormalized:
orders: id | customer_id | customer_name | customer_city | status
→ No JOIN, faster reads
→ Customer name changes → update every order row

Trade-offs:
Normalize when:
+ Write-heavy systems — consistency critical
+ OLTP — online transaction processing
+ Financial, reservation systems — data integrity non-negotiable

Denormalize when:
+ Read-heavy systems — many reads, few writes
+ Reporting, analytics, dashboards
+ Elasticsearch index — always denormalized, optimized for reads

Rakuten connection:
Reservation table normalized — customer and shop in separate tables.
Consistency critical for booking data.
Reporting layer used denormalized view or Elasticsearch index.

## Constraints — DB-Level Data Protection

Validation at application layer is not enough.
DB constraints are the last line of defense — they work even if app is bypassed.

CREATE TABLE orders (
id          BIGINT PRIMARY KEY AUTO_INCREMENT,
customer_id BIGINT NOT NULL,
amount      DECIMAL(10,2) NOT NULL,
status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
email       VARCHAR(255) UNIQUE NOT NULL,
created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_amount CHECK (amount > 0),
    CONSTRAINT chk_status CHECK (status IN ('PENDING','CONFIRMED','CANCELLED','COMPLETED')),
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

NOT NULL     → null not allowed
UNIQUE       → no duplicates
CHECK        → custom rule — amount cannot be negative
FOREIGN KEY  → referential integrity — cannot reference non-existent customer
DEFAULT      → fallback value if not provided

Why constraints at DB level:
Application code can be bypassed — direct DB scripts, other services connecting directly.
DB constraints fire in every case, no exceptions.

## Index Types

Without index: DB does full table scan — reads entire table for every query.
Millions of rows → unacceptable for production.

B-Tree Index — default, general purpose:
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);

Works for:
WHERE status = 'PENDING'          → equality
WHERE created_at > '2024-01-01'   → range
ORDER BY created_at DESC          → sorting

Composite Index — multiple columns:
CREATE INDEX idx_orders_status_created ON orders(status, created_at);

Leftmost prefix rule:
WHERE status = 'PENDING'                           → uses index (1st column present)
WHERE status = 'PENDING' AND created_at > '2024'   → uses index (both columns)
WHERE created_at > '2024'                          → does NOT use index (1st column skipped)
WHERE status = 'PENDING' AND created_at > '2024'
(with customer_id skipped in a 3-column index) → uses index partially up to the gap

Column order matters — most frequently filtered column goes first.
A skipped column breaks the chain — everything to its right is ignored.

Partial Index — conditional:
CREATE INDEX idx_active_orders ON orders(customer_id) WHERE status = 'ACTIVE';

Only active orders in the index → smaller index → faster lookups.
Effective when most queries already filter on that condition.

Covering Index — everything from the index:
CREATE INDEX idx_covering ON orders(status, customer_id, amount);

SELECT customer_id, amount FROM orders WHERE status = 'PENDING';
→ All needed columns are in the index → DB never touches the table → very fast

Index trade-offs:
When to use:
+ Read-heavy tables — many queries, few writes
+ High cardinality columns — email, userId (many distinct values)
+ Columns used in WHERE, ORDER BY, JOIN
+ Large tables — millions of rows where full scan is unacceptable

When NOT to use:
- Write-heavy tables — every INSERT/UPDATE/DELETE updates the index → writes slow down
- Low cardinality — boolean, status with 3-4 values → full scan often faster
- Small tables — DB optimizer picks full scan anyway, index overhead wasted
- Too many indexes — maintenance overhead, storage grows, writes degrade

## Execution Plan — EXPLAIN

Rakuten: 8s → 3s query optimization. This is exactly how.

EXPLAIN SELECT o.id, c.name, o.amount
FROM orders o
JOIN customers c ON o.customer_id = c.id
WHERE o.status = 'PENDING'
ORDER BY o.created_at DESC;

What to look at:

type column — access method:
ALL    → full table scan → no index → bad
index  → full index scan → slightly better
range  → index range scan → good
ref    → index lookup → good
const  → single row, primary key → best

rows column:
estimated rows to read → high number → potential problem

Extra column:
Using filesort   → extra sort pass for ORDER BY → add index
Using temporary  → temporary table created → optimize
Using index      → covering index used → best case

Rakuten optimization process:
1. EXPLAIN showed type: ALL → full table scan
2. status column had no index
3. Added index → type changed to ref
4. Using filesort present → added index on created_at
5. Result: 8s → 3s

## Transaction Isolation Levels

Concurrent transactions can interfere with each other in three ways:

Dirty Read:
Transaction A wrote but not committed.
Transaction B read A's uncommitted value.
Transaction A rolled back.
Transaction B processed data that never existed.

Non-Repeatable Read:
Transaction A reads same row twice — sees different value.
Between reads, Transaction B updated and committed that row.
Same transaction, same row, inconsistent result.

Phantom Read:
Transaction A runs same query twice — sees different row count.
Between queries, Transaction B inserted or deleted matching rows.
Same query, same transaction, different number of rows — phantom rows appeared.

Isolation levels:
READ UNCOMMITTED → dirty read possible, never use
READ COMMITTED   → no dirty read, non-repeatable read possible (PostgreSQL default)
REPEATABLE READ  → no dirty read, no non-repeatable read, phantom possible (MySQL default)
SERIALIZABLE     → nothing — transactions run as if serial, slowest, safest

Trade-offs:
Higher isolation → fewer anomalies → more locking → lower throughput
Lower isolation → more anomalies → less locking → higher throughput

Choose based on what your system can tolerate.
Financial systems → SERIALIZABLE or REPEATABLE READ
Most web apps → READ COMMITTED is sufficient

## Locking

Optimistic Locking — conflict is rare:
@Entity
public class Order {
@Version
private Long version;
}

UPDATE orders SET status = 'SHIPPED', version = 6 WHERE id = 1 AND version = 5;
Version mismatch → someone else updated → OptimisticLockException → retry at app layer

Trade-offs:
+ No lock held during read → high throughput, great for read-heavy
+ Scales well under low contention
- Conflict → exception → retry needed at application layer
- Retry storm if contention grows — many threads retrying simultaneously
- Not suitable when conflicts are frequent

When to use: read-heavy, low write contention, eventual consistency acceptable.

Pessimistic Locking — conflict is frequent:
SELECT * FROM orders WHERE id = 1 FOR UPDATE;
Row locked until transaction ends. Other transactions wait.

Spring Data:
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<Order> findById(Long id);

Trade-offs:
+ Guarantees no conflict — strong consistency
+ Suitable for financial operations, critical writes
- Locks held during transaction → lower throughput
- Deadlock risk — two transactions waiting for each other's lock
- Lock timeout mandatory — never wait forever

SET innodb_lock_wait_timeout = 5;

When to use: write-heavy, consistency critical, fintech, reservation systems.

Deadlock risk with pessimistic locking:
Always acquire locks in consistent order across transactions.
Set lock timeout — fail fast, retry, do not wait forever.

## Join Algorithms — Awareness Level

DB optimizer chooses the strategy — you don't control it directly.
But you see it in EXPLAIN output — knowing what it means is senior-level.

Nested Loop Join:
For each row on left, scan the right table.
Good for small tables. O(n²) for large tables.

Hash Join:
Build hash map from smaller table, probe with larger table.
Good for large tables, uses memory.

Merge Join:
Both tables already sorted — merge them.
Very fast when sort order is available.

You don't tune this — DB optimizer decides.
Seeing it in EXPLAIN and understanding what's happening is what matters.

## Interview Checklist
→ Normalize vs denormalize? → consistency vs read performance, depends on read/write ratio
→ Why constraints at DB level? → app can be bypassed, DB is last line of defense
→ Leftmost prefix rule? → composite index usable from left, skipped column breaks the chain
→ When not to add index? → write-heavy, low cardinality, small tables, too many indexes
→ EXPLAIN type: ALL? → full table scan, index missing
→ Dirty vs non-repeatable vs phantom? → uncommitted read vs same row changes vs row count changes
→ Optimistic vs pessimistic? → read-heavy low conflict vs write-heavy high conflict
→ Pessimistic locking risk? → deadlock, always set lock timeout