## SOLID — S: Single Responsibility Principle
- A class should have only ONE reason to change
- If a class does 4 things → 4 reasons to change → split it

## Real example:
- UserService doing validate + save + email + log → WRONG
- UserValidator, UserRepository, WelcomeEmailSender → each does 1 thing

## Interview one-liner:
"SRP means a class should have only one reason to change.
If it does multiple things, split it into focused classes."## OCP (Open/Closed Principle)

- Open for extension
- Closed for modification

---

## Example

interface Discount {
double apply(double price);
}

---

## Key Idea

- Add new behavior without changing existing code



## SOLID — L: Liskov Substitution Principle
- Subclass must be substitutable for its parent
- If subclass breaks parent's behavior → LSP violation

## Real example:
- Penguin extends Bird, fly() throws exception → WRONG
- Solution: remove fly() from Bird, create Flyable interface
- Only flying birds implement Flyable

## Interview one-liner:
"LSP means a subclass should be usable anywhere the parent
is used, without breaking the program."

## SOLID — I: Interface Segregation Principle
- Don't force classes to implement methods they don't need
- Split large interfaces into smaller, focused ones

## Real example:
- Worker interface: work() + eat() + sleep() → Robot can't eat/sleep
- Solution: Workable, Eatable, Sleepable → separate interfaces
- Robot implements only Workable

## Interview one-liner:
"ISP says interfaces should be small and focused.
Don't force classes to implement what they don't need."

## SOLID — D: Dependency Inversion Principle
- High-level classes should not depend on low-level classes
- Both should depend on abstractions (interfaces)
- Dependency comes from outside, not created inside

## Real example:
- OrderService creates MySQLDatabase inside → WRONG
- OrderService receives Database interface → CORRECT
- Switch MySQL to PostgreSQL → only change one line

## Connection to Spring:
- @Autowired = DIP in action
- Spring injects the implementation, class only knows the interface

## Interview one-liner:
"DIP says depend on abstractions, not concretions.
High-level modules shouldn't care which implementation they use."

