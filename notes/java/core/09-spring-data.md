# [2.4] Spring Data

## Persistence Context

When a transaction begins, JPA opens a working space — the persistence context.
It has two responsibilities: identity map and change tracking.

Identity map:
Same entity fetched twice in the same transaction → DB hit only once.
Second call returns the same object from the context.

@Transactional
public void example(Long id) {
Order order1 = orderRepository.findById(id).orElseThrow(); // hits DB
Order order2 = orderRepository.findById(id).orElseThrow(); // returns from context
System.out.println(order1 == order2); // true — same instance
}

Change tracking:
When an entity enters the context, JPA takes a snapshot of its state.
At commit time: snapshot vs current state → if different → UPDATE is generated.
This is dirty checking.

Transaction begins
→ findById() → entity loaded from DB
→ snapshot taken: { status: "PENDING", amount: 100 }
→ order.setStatus("SHIPPED")
→ transaction commits
→ snapshot: { status: "PENDING" }
→ current:  { status: "SHIPPED" }
→ difference found → UPDATE written automatically

Persistence context is bound to the transaction.
Transaction ends → context closes → dirty checking fires → changes flushed.

## Dirty Checking

Dirty checking only works inside an active transaction.
No transaction → no persistence context → no tracking → no automatic UPDATE.

@Transactional
public void updateStatus(Long id) {
Order order = orderRepository.findById(id).orElseThrow();
order.setStatus("SHIPPED"); // no save() call
// transaction ends → dirty checking → UPDATE fires
}

public void updateStatus(Long id) {
Order order = orderRepository.findById(id).orElseThrow();
order.setStatus("SHIPPED");
// nothing happens — no transaction, no context, no tracking
}

Hidden danger:
@Transactional
public OrderResponse getOrder(Long id) {
Order order = orderRepository.findById(id).orElseThrow();
order.setLastViewedAt(LocalDateTime.now()); // just for logging?
return toResponse(order);
// transaction ends → dirty checking → unintended UPDATE fires
}

Fix: use @Transactional(readOnly = true) for read operations.
Dirty checking disabled → no accidental writes.
Also sends read-only hint to DB — some DBs route to replica.

## N+1 Problem

@Entity
public class Order {
@Id private Long id;
private String status;

    @OneToMany(fetch = FetchType.LAZY)
    private List<OrderItem> items;
}

@Entity
public class OrderItem {
@Id private Long id;
private String productName;
private Double price;
}

The problem:
List<Order> orders = orderRepository.findAll(); // 1 query to orders table

for (Order order : orders) {
order.getItems(); // 1 query per order to order_items table
}
// 100 orders → 101 queries total
// 1000 orders → 1001 queries total

LAZY fetch means: "don't load items now, load when accessed."
Every getItems() call goes to DB. This is the N+1 problem.

Rakuten connection:
Reservation list fetched, then shop detail loaded per reservation.
Under 1380+ reservations/hour this compounds fast.
Visible in Gatling results as p95 response time degradation.

Solution 1 — JOIN FETCH:
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.status = :status")
List<Order> findWithItems(@Param("status") String status);

JOIN → filters, but does not load the relation (still lazy)
JOIN FETCH → filters AND loads the relation in the same query

Solution 2 — @EntityGraph:
@EntityGraph(attributePaths = {"items"})
List<Order> findByStatus(String status);

No JPQL needed — declare which relations to load eagerly.

Solution 3 — DTO Projection (preferred for read operations):
public record OrderSummary(Long id, String status, String productName, Double price) {}

@Query("""
SELECT new com.example.OrderSummary(o.id, o.status, i.productName, i.price)
FROM Order o JOIN o.items i
WHERE o.status = :status
""")
List<OrderSummary> findOrderSummaries(@Param("status") String status);

Single query, only needed fields, no entity overhead.
N+1 cannot occur — no lazy relations on a record.

Detecting N+1:
spring:
jpa:
show-sql: true
Count the queries in logs. More than expected → N+1 suspect.
Do not leave show-sql enabled in production — log pressure.

## Transaction Boundary

@Transactional belongs on the service layer, not the repository layer.
A business operation may span multiple repository calls — all must be in one transaction.

@Service
public class OrderService {
@Transactional
public void createOrder(OrderRequest req) {
Order order = toEntity(req);
orderRepository.save(order);         // 1st repository
inventoryService.reserve(order);     // 2nd repository
notificationService.schedule(order); // 3rd repository
// all three in one transaction — all commit or all rollback
}
}

Without @Transactional:
First repository call succeeds.
Second throws exception.
First is already committed — no rollback.
System left in inconsistent state — partial update.

Propagation:
REQUIRED (default)   → join existing transaction, or start new one
REQUIRES_NEW         → always start new transaction, suspend current
SUPPORTS             → join if exists, run without if not
NOT_SUPPORTED        → run without transaction, suspend if exists
NEVER                → throw if transaction exists

REQUIRES_NEW use case:
@Transactional
public void placeOrder(Order order) {
orderRepository.save(order);
auditService.log(order); // must persist even if order rolls back
}

@Service
public class AuditService {
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void log(Order order) {
// own transaction — commits independently
auditRepository.save(new AuditLog(order));
}
}

Remember: AuditService must be a separate bean.
Calling this.log() from within the same class → self-invocation → proxy bypassed → REQUIRES_NEW ignored.

## Pagination

Offset-based:
Page<Order> page = orderRepository.findAll(PageRequest.of(pageNumber, 20));

Generated SQL:
SELECT * FROM orders ORDER BY created_at DESC LIMIT 20 OFFSET 98000

DB reads 98,000 rows, discards them, returns next 20.
Page number grows → rows discarded grows → query slows down.
This is the deep pagination problem.

Additional cost: Page<T> triggers a COUNT(*) query automatically.
Large tables → COUNT(*) is slow → every paginated request pays this cost.

Keyset pagination (cursor-based):
List<Order> findByIdGreaterThanOrderByIdAsc(Long lastSeenId, Pageable pageable);

SELECT * FROM orders WHERE id > :lastSeenId ORDER BY id LIMIT 20

Starts directly from the last seen position.
Page 1000 is as fast as page 1.

Trade-off:
+ Consistent performance regardless of page depth
- Cannot jump to arbitrary page number
- Only forward/backward navigation
  Suitable for: feeds, infinite scroll, most list UIs (Instagram, Twitter work this way)

## Specification — Dynamic Query

@Query with many optional filters → hard to maintain.
Specification pattern solves this cleanly.

public class OrderSpec {
public static Specification<Order> hasStatus(String status) {
return (root, query, cb) ->
status == null ? null : cb.equal(root.get("status"), status);
}

    public static Specification<Order> createdAfter(LocalDateTime date) {
        return (root, query, cb) ->
            date == null ? null : cb.greaterThan(root.get("createdAt"), date);
    }
}

Specification<Order> spec = Specification
.where(OrderSpec.hasStatus(filter.getStatus()))
.and(OrderSpec.createdAfter(filter.getFromDate()));

orderRepository.findAll(spec, pageable);

Null-safe — null filter → condition not added to query.
Composable — combine any filters without changing existing code.
Testable — each Specification tested in isolation.

## JPA vs Native Query

JPA — simple queries, domain-driven:
List<Order> findByStatusAndCreatedAtBefore(String status, LocalDateTime date);

Native query — complex aggregation, DB-specific features:
@Query(value = """
SELECT o.id, COUNT(oi.id) as item_count, SUM(oi.price) as total
FROM orders o
JOIN order_items oi ON o.id = oi.order_id
WHERE o.status = :status
GROUP BY o.id
HAVING SUM(oi.price) > :minTotal
""", nativeQuery = true)
List<Object[]> findHighValueOrders(@Param("status") String status,
@Param("minTotal") BigDecimal minTotal);

When to use native:
+ Window functions, complex aggregation
+ DB-specific features (full-text search, JSONB)
+ Performance critical path where ORM overhead is unacceptable

Trade-off:
+ Maximum SQL power, maximum performance
- DB-specific, harder to refactor, manual result mapping

## Interview Checklist
→ What is persistence context? → JPA's working space per transaction, identity map + change tracker
→ What is dirty checking? → automatic UPDATE on changed entities at transaction commit
→ What is N+1? → 1 query for list + N queries for each item's relation
→ How to fix N+1? → JOIN FETCH, @EntityGraph, or DTO projection (preferred)
→ Where does @Transactional go? → service layer, not repository
→ Why not repository layer? → business operation spans multiple repositories
→ What is deep pagination? → offset grows → DB discards more rows → slows down
→ Fix for deep pagination? → keyset/cursor-based pagination
→ When native query? → complex SQL, window functions, DB-specific features