# [2.4] Spring Data & JPA

## Persistence Context & Dirty Checking

JPA tracks every entity loaded within a transaction.
At transaction end → compares current state with original → if changed → auto UPDATE.

This is called Dirty Checking.

@Transactional present:
→ Dirty Checking active
→ setStatus() without save() → DB gets updated automatically

@Transactional absent:
→ No Persistence Context
→ setStatus() without save() → nothing happens in DB

Pitfall — unintentional update:
@Transactional
public Order getOrder(Long id) {
Order order = repo.findById(id).get();
order.setStatus("SHIPPED"); // just wanted to read!
// transaction ends → UPDATE fires → bug!
}

Fix — readOnly = true:
@Transactional(readOnly = true)
public Order getOrder(Long id) {
return repo.findById(id).get();
// Dirty Checking disabled → safe, also faster
}

The question I ask myself:
"Am I only reading data in this method?"
+ Yes → @Transactional(readOnly = true)
+ No → @Transactional

---

## N+1 Problem

Symptom:
1 query to fetch N records + N queries for each record's relation = N+1 queries total.

Example:
findAll() → 100 orders
order.getItems() in loop → 100 separate queries
Total: 101 queries — kills performance under load.

Why it happens:
@OneToMany is LAZY by default.
JPA doesn't load related data until you access it.
Each access = new SQL query.

Fix — JOIN FETCH:
@Query("SELECT o FROM Order o JOIN FETCH o.items")
List<Order> findAllWithItems();
Result: 1 query, everything loaded at once.

How to detect:
Enable SQL logging:
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
Count the queries — if you see N+1 pattern, fix with JOIN FETCH.

Real world at Rakuten:
SQL EXPLAIN analysis + slow query detection = exactly this kind of work.

--- 

## Projection

Problem:
Fetching full entities when only a few fields are needed.
Wastes memory, network, and DB resources.

Solution 1 — Interface Projection:
public interface UserSummary {
String getName();
String getEmail();
}
List<UserSummary> findAllProjectedBy();
Spring generates: SELECT name, email FROM users

Solution 2 — DTO Projection:
public record UserSummary(String name, String email) {}

@Query("SELECT new com.example.UserSummary(u.name, u.email) FROM User u")
List<UserSummary> findAllSummaries();

When to use which:
+ Interface Projection → simple, quick, read-only views
+ DTO Projection → more control, can combine multiple tables

The question I ask myself:
"Do I really need the full entity here?"
+ No → use projection
+ Yes → use entity

---

## Specification — Dynamic Query

Problem:
Multiple optional filters → combinatorial explosion of repository methods.
10 filters = potentially hundreds of method combinations.

Solution — Specification pattern:
Each filter = one Specification
Combine at runtime with .and() / .or()

Example:
public class UserSpec {
public static Specification<User> hasName(String name) {
return (root, query, cb) ->
name == null ? null : cb.equal(root.get("name"), name);
}
public static Specification<User> hasEmail(String email) {
return (root, query, cb) ->
email == null ? null : cb.equal(root.get("email"), email);
}
}

Usage:
Specification<User> spec = Specification
.where(UserSpec.hasName(name))
.and(UserSpec.hasEmail(email));
userRepository.findAll(spec);

Null filters are automatically ignored.
One clean query, any combination.

Real world:
Shop search with zip code + tire size + import car filters
= exactly this pattern.

The question I ask myself:
"Do I have multiple optional filters?"
+ Yes → Specification
+ No → simple repository method

---

## Pagination

Problem:
findAll() on millions of records = memory explosion + slow response.

Solution — Pageable:
Pageable pageable = PageRequest.of(page, size);
Page<Order> result = orderRepository.findAll(pageable);

Page object contains:
→ content: actual records
→ totalElements: total count
→ totalPages: total pages
→ number: current page

Pitfall — Deep Pagination:
page=50000, size=20 → OFFSET 1000000
DB counts and skips 1M rows — very slow.

Fix — Cursor based pagination:
Instead of OFFSET, use last seen ID:
WHERE id > lastSeenId LIMIT 20
Always fast regardless of how deep you go.

When to use which:
+ Pageable (offset) → small datasets, admin panels
+ Cursor based → large datasets, infinite scroll, feeds

The question I ask myself:
"How large can this dataset get?"
+ Small → Pageable is fine
+ Large/infinite → cursor based pagination