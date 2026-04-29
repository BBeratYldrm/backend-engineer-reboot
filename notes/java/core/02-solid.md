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

How to spot:
→ Ask "who wants to change this?" — multiple answers = SRP violation
→ Methods that don't use any class fields = candidate for extraction
→ Class name contains "And" or "Manager" → almost always SRP violation

Interview tip:
"SRP is not about size — it's about reasons to change.
If two different teams would modify this class for different reasons,
it has too many responsibilities."

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

Extension point design:
OCP is not magic — it requires deliberate design of extension points.
An extension point is a place in the system that is explicitly designed
to accept new behavior without modification.

How to design extension points:
→ Define an interface for the varying behavior
→ Make the context depend on that interface (DIP)
→ New behavior = new implementation, no existing code touched

// Extension point: discount calculation
public interface DiscountStrategy {
double apply(double price);
}

// Extensions — each is a new class:
class NoDiscount     implements DiscountStrategy { ... }
class MemberDiscount implements DiscountStrategy { ... }
class SeasonalSale   implements DiscountStrategy { ... }

// Context — never modified when new discount added:
class PriceCalculator {
private final DiscountStrategy discount;

    PriceCalculator(DiscountStrategy discount) {
        this.discount = discount;
    }

    double calculate(double price) {
        return discount.apply(price);
    }
}

Where OCP breaks down:
OCP is not absolute — anticipating every extension is overengineering.
The goal is to protect stable, core logic from churn in volatile behavior.
If you can't predict what will vary, don't over-abstract prematurely.

Connection to Polymorphism:
OCP tells you WHY to use polymorphism.
Polymorphism is HOW you implement OCP.

How to spot violations:
→ if/else or switch on type → OCP violation candidate
→ "To add X, I need to modify this class" → OCP violation
→ Same class changes every time a new business rule arrives

Interview tip:
"OCP means adding new behavior by adding new code, not rewriting existing code.
The key is designing extension points deliberately — interfaces where variation is expected.
Premature abstraction is also a mistake — OCP applies where change is predictable."

The question I ask myself:
"To add a new feature, do I need to open an existing class?"
+ Yes → OCP violation, consider polymorphism
+ No → OCP is working

---

## L — Liskov Substitution Principle

Rule: A subclass must be fully substitutable for its parent.
Behavior must not break when parent is replaced by child.

Subtype contract:
LSP is not just about not throwing exceptions.
The subclass must honor the contract — preconditions, postconditions, invariants.

Precondition: what the caller must guarantee before calling
Postcondition: what the method guarantees after running
Invariant: what must always be true about the object

LSP rule:
→ Subclass cannot strengthen preconditions (require more from the caller)
→ Subclass cannot weaken postconditions (guarantee less than the parent)
→ Subclass cannot break invariants

Bad:
class Animal { void fly() { ... } }
class Dog extends Animal {
void fly() { throw new RuntimeException("Dogs can't fly!"); }
}
// Dog cannot replace Animal — LSP violation
// postcondition broken: parent guarantees flying, child throws instead

Bad in repositories:
class ReadOnlyRepository implements Repository {
void save() { throw new UnsupportedOperationException(); }
}
// Caller expects save() to work — child breaks that contract

Good:
Split the interface — ReadOnlyRepository only implements read methods.
No forced implementation of methods it can't support.

Classic LSP violation — Square extends Rectangle:
class Rectangle {
void setWidth(int w)  { this.width = w; }
void setHeight(int h) { this.height = h; }
}

class Square extends Rectangle {
void setWidth(int w)  { this.width = w;  this.height = w; } // breaks contract
void setHeight(int h) { this.width = h; this.height = h; }  // breaks contract
}

// Caller:
Rectangle r = new Square();
r.setWidth(5);
r.setHeight(10);
// Expected: area = 50. Actual: area = 100 — Square broke the contract

Real solution: Square and Rectangle are not in an is-a relationship — use composition.

Connection to ISP:
LSP violations often come from ISP violations.
Interface too large → subclass forced to implement unused methods → LSP broken.
Fix ISP → LSP violations often disappear automatically.

How to spot:
→ throw new UnsupportedOperationException() in a method → LSP + ISP violation
→ Subclass overrides method with empty body or exception → red flag
→ Caller must instanceof-check before using → LSP broken

Interview tip:
"LSP is about honoring contracts, not just avoiding exceptions.
A subclass that throws where the parent succeeds, or changes behavior in a way
the caller doesn't expect, violates LSP even without an exception.
The Square-Rectangle example is the classic case."

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
void eat() { throw new UnsupportedOperationException(); }
}

Good:
interface Workable       { void work(); }
interface BiologicalNeeds { void eat(); void sleep(); }

Robot implements Workable only.
Human implements Workable + BiologicalNeeds.

Interface death by fat interface:
One large interface → every implementor drags unused methods
→ Forced empty/throwing implementations → LSP violation
→ Any change to the interface forces recompile of all implementors

Solution: role-based interfaces — one interface per role.
A class can implement multiple role interfaces — that's fine and encouraged.

Rakuten connection:
Spring Data JpaRepository is large — findAll, save, delete, count, existsById...
For read-only services, I would extend only a read interface:

interface ShopReadRepository {
Optional<Shop> findById(Long id);
List<Shop> findByCity(String city);
}
// Write operations not exposed → ISP respected

CQRS naturally follows ISP:
Read interface → query side
Write interface → command side
Two separate contracts, two separate implementors.

Connection to LSP:
ISP violation → forced implementation → UnsupportedOperationException → LSP violation.
Fix ISP → LSP often fixes itself.

LSP vs ISP — two perspectives:
LSP → bottom-up: "Can subclass replace parent?"
ISP → top-down: "Is parent forcing too much on subclass?"

How to spot:
→ UnsupportedOperationException in implemented method → ISP violation
→ Empty method body in implementation → ISP violation
→ "I implement this interface but only use 2 of its 10 methods" → split it

Interview tip:
"ISP violations are easy to spot — look for UnsupportedOperationException
or empty method bodies. That's a class forced to implement something it doesn't need.
The fix is role-based interfaces — one interface per behavior."

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
private MySQLOrderRepository repository = new MySQLOrderRepository();
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

Where to draw the boundary:
High-level module: business logic (OrderService, ShopSearchService)
Low-level module: infrastructure (repositories, HTTP clients, message queues)

DIP says: the high-level module defines the interface it needs.
The low-level module implements that interface.
The interface belongs to the domain, not the infrastructure.

// WRONG — domain imports infrastructure:
// order/ package imports mysql/ package → wrong direction

// CORRECT — infrastructure imports domain:
// order/ defines OrderRepository interface
// mysql/ has MySQLOrderRepository implements OrderRepository

Spring connection:
@RequiredArgsConstructor + interface fields = DIP in action.
Spring decides which implementation to inject at runtime.
Write against the interface — Spring handles the rest.

Constructor injection vs field injection:
@Autowired on field → dependencies hidden, harder to test, not recommended
Constructor injection → dependencies explicit, easy to mock, testable

// Prefer this:
@Service
@RequiredArgsConstructor
public class OrderService {
private final OrderRepository repository;  // Spring injects via constructor
}

DIP + OCP + Polymorphism together:
DIP → depend on interface
OCP → add new implementation without touching existing code
Polymorphism → runtime decides which implementation runs
= Loose coupling, extensible, testable system

Rakuten connection:
ShopSearchService depends on PartsShopRepository (interface), not the concrete class.
Spring injects the real implementation in production.
In tests, a mock could be injected instead — no code change needed.
This is DIP making the system testable without a real database.

How to spot violations:
→ new ConcreteClass() inside a service method → DIP violation
→ import of infrastructure package inside domain class → DIP violation
→ "I can't write a unit test without a real DB" → DIP is missing

Interview tip:
"DIP is not just about using interfaces — it's about who owns the interface.
The domain defines what it needs. Infrastructure adapts to that.
If the business layer imports from the infrastructure layer, DIP is already violated."

The question I ask myself:
"If I need to swap the database, HTTP client, or message queue —
how many business classes do I need to touch?"
+ More than zero → DIP is missing somewhere

---

## SOLID Together — How They Connect

SRP → each class has one reason to change
OCP → new behavior via new code, not modification
LSP → subclasses honor the contract of the parent
ISP → no class is forced to implement what it doesn't use
DIP → business logic depends on interfaces, not infrastructure

They reinforce each other:
ISP violation → forces implementation → LSP violation
DIP + OCP together → extension without modification
SRP + DIP → small, focused classes that depend on interfaces

Senior interview framing:
SOLID is not a checklist — it's a system for managing change.
Each principle reduces a specific type of coupling.
Knowing when NOT to apply them is as important as knowing how.
Over-applying SOLID to a simple 2-class system is overengineering.