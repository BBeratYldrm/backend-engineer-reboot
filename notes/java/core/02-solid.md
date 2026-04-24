# [1.1] SOLID Principles

## S — Single Responsibility Principle

Rule: A class should have only one reason to change.

Not about size — about reasons to change.
"Who would want to change this class, and why?"
If two different people/teams would change it for different reasons → split it.

Bad:
UserService handles validation + business logic + email sending
→ Three different reasons to change → SRP violation

Good:
UserService → orchestration only
UserValidator → validation rules
EmailService → email sending

Rakuten connection:
ShopSearchService had ShopSearchValidator and ShopSearchFormatter extracted.
Each class has one job — one reason to change.

Interview tip:
"SRP is not about size — it's about reasons to change.
If two different teams would modify this class for different reasons,
it has too many responsibilities."

How to spot:
→ Ask "who wants to change this?" — multiple answers = SRP violation
→ Methods that don't use any class fields = candidate for extraction

The question I ask myself:
"If requirements change in two unrelated areas, does this class change twice?"
+ Yes → split it
+ No → SRP is fine

---

## O — Open/Closed Principle

Rule: Open for extension, closed for modification.
Add new behavior by adding new code — not by modifying existing code.

Bad:
if (type.equals("email")) { ... }
else if (type.equals("sms")) { ... }
// New type → modify existing class → OCP violation

Good:
interface Notification { void send(String message); }
class EmailNotification implements Notification { ... }
class SmsNotification implements Notification { ... }
// New type → add new class, touch nothing else 

Connection to Polymorphism:
OCP tells you WHY to use polymorphism.
Polymorphism is HOW you implement OCP.

How to spot:
→ if/else or switch on type → OCP violation candidate
→ "To add X, I need to modify existing class" → OCP violation

Interview tip:
"OCP means adding new behavior by adding new code, not rewriting existing code.
This is achieved through polymorphism and interfaces."

The question I ask myself:
"To add a new feature, do I need to open an existing class?"
+ Yes → OCP violation, consider polymorphism
+ No → OCP is working

---

## L — Liskov Substitution Principle

Rule: A subclass must be fully substitutable for its parent.
Behavior must not break when parent is replaced by child.

Bad:
class Animal { void fly() { ... } }
class Dog extends Animal {
void fly() { throw new RuntimeException("Dogs can't fly!"); } // 
}
// Dog cannot replace Animal — LSP violation

Bad in repositories:
class ReadOnlyRepository implements Repository {
void save() { throw new UnsupportedOperationException(); } // 
}

Good:
Split the interface — ReadOnlyRepository only implements read methods.
No forced implementation of methods it can't support.

Connection to ISP:
LSP violations often come from ISP violations.
Interface too large → subclass forced to implement unused methods → LSP broken.
Fix ISP → LSP violations often disappear automatically.

How to spot:
→ throw new UnsupportedOperationException() in a method → LSP + ISP violation
→ Subclass overrides method with empty body or exception → red flag

Interview tip:
"LSP violation is a signal that the inheritance hierarchy is wrong.
If a subclass can't fully replace its parent,
consider composition or interface segregation instead."

The question I ask myself:
"Can every subclass be used wherever the parent is expected, without surprises?"
+ No → LSP violation, rethink the hierarchy

---

## I — Interface Segregation Principle

Rule: A class should not be forced to implement methods it doesn't use.
Many small, focused interfaces > one large interface.

Bad:
interface Worker {
void work();
void eat();   // Robot doesn't eat 
void sleep(); // Robot doesn't sleep 
}
class Robot implements Worker {
void eat() { throw new UnsupportedOperationException(); } // 
}

Good:
interface Workable { void work(); }
interface BiologicalNeeds { void eat(); void sleep(); }

Robot implements Workable only.
Human implements Workable + BiologicalNeeds.

Connection to LSP:
ISP violation → forced implementation → UnsupportedOperationException → LSP violation.
Fix ISP → LSP often fixes itself.

LSP vs ISP — two perspectives:
LSP → bottom-up: "Can subclass replace parent?"
ISP → top-down: "Is parent forcing too much on subclass?"

Rakuten connection:
Spring Data JpaRepository is large — findAll, save, delete, count...
For read-only use cases, extend only what you need.
CQRS naturally follows ISP — read interface, write interface separate.

How to spot:
→ UnsupportedOperationException in implemented method → ISP violation
→ Empty method body in implementation → ISP violation
→ "I implement this interface but only use 2 of its 10 methods" → split it

Interview tip:
"ISP violations are easy to spot — look for UnsupportedOperationException
or empty method bodies. That's a class forced to implement something it doesn't need."

The question I ask myself:
"Does this class implement methods it never actually uses?"
+ Yes → split the interface

---

## D — Dependency Inversion Principle

Rule: Depend on abstractions, not concrete implementations.
High-level modules should not depend on low-level modules.
Both should depend on interfaces.

Bad:
public class OrderService {
private MySQLOrderRepository repository = new MySQLOrderRepository(); // 
}
// Switching to PostgreSQL → must modify OrderService
// Testing → can't mock the repository

Good:
public class OrderService {
private final OrderRepository repository; // interface 

    public OrderService(OrderRepository repository) { // constructor injection
        this.repository = repository;
    }
}
// Switch DB → inject different implementation, OrderService unchanged
// Testing → inject mock, easy

Spring connection:
@RequiredArgsConstructor + interface fields = DIP in action.
Spring decides which implementation to inject at runtime.
You write against the interface — Spring handles the rest.

DIP + OCP + Polymorphism together:
DIP → depend on interface
OCP → add new implementation without touching existing code
Polymorphism → runtime decides which implementation runs
= Loose coupling, extensible system

Rakuten connection:
ShopSearchService depends on PartsShopRepository (interface), not the concrete class.
Spring injects the real implementation.
In tests, a mock could be injected instead — no code change need

## Stream API

Pipeline of 3 parts:
1. Source → where data comes from (list, array, set)
2. Intermediate → what to do (lazy — nothing runs until terminal)
3. Terminal → how to finish (triggers execution)

list.stream()           // source
.filter(...)        // intermediate
.map(...)           // intermediate
.toList();          // terminal — execution starts here

Lazy evaluation:
Intermediate operations don't execute without a terminal operation.
JVM can optimize the pipeline before running.

Short-circuit:
Each element passes through the full pipeline one by one.
Not: all elements through filter, then all through map.
But: element 1 → filter → map, element 2 → filter → map...

Most used operations:
Intermediate: filter, map, flatMap, distinct, sorted, limit, skip
Terminal: toList, collect, count, findFirst, anyMatch, allMatch, reduce

flatMap:
Flattens nested structures.
List<List<T>> → flatMap → List<T>
Optional → flatMap(Optional::stream) → empty optionals dropped automatically

Parallel Stream:
.parallelStream() → splits list across multiple CPU cores → faster for large lists

Trade-offs:
+ Large lists (100k+) → significant speedup
- Small lists → overhead worse than benefit
- No ordering guarantee
- Not safe with stateful operations or shared mutable state

Primitive Streams — avoid boxing:
int/long/double → boxing to Integer/Long/Double → object creation → GC pressure
IntStream, LongStream, DoubleStream → no boxing → faster for numeric operations

int vs Integer:
int → primitive, stack, cannot be null, fast
Integer → object, heap, can be null, slower
Use Integer when: null needed, used in collections (List<Integer>)
Use int when: calculations, method variables

Real world:
Rakuten — ShopSearchService:
shops.stream()
.map(shop -> createShopSearchItem(shop, zipCode))
.sorted(Comparator.comparingDouble(ShopSearchItem::getDistance))
.toList()

Netflix → filter highly rated movies → map to genre → distinct genres
Amazon → filter pending orders → sort by date → limit 100 → process

Interview tips:
"Streams are lazy — nothing runs without a terminal operation."
"Parallel streams aren't always faster — benchmark first."
"Use IntStream for numeric operations to avoid boxing overhead."

The question I ask myself:
"Am I transforming a collection?" → Stream
"Do I need parallel processing on large data?" → parallelStream (carefully)
"Am I doing numeric aggregation?" → IntStream/LongStream

