# OOP Principles

## Encapsulation
- Hide internal state (private fields)
- Expose controlled access (public methods)
- Goal: protect invariants — keep object in valid state always

## Real example:
- BankAccount.balance → private
- deposit/withdraw → validates before changing state
- Outsider cannot set balance = -500

## Interview one-liner:
"Encapsulation protects the invariants of a class by hiding
internal state and controlling access through methods."

## Polymorphism
- Same interface, different behavior per class
- Add new types without touching existing code
- if/else chains → sign that polymorphism is needed

## Real example:
- Notification → Email, SMS, Slack implement same interface
- NotificationService doesn't know or care which type
- New type = new class only, nothing else changes

## Interview one-liner:
"Polymorphism lets us add new behaviors without modifying
existing code — we extend, not change."

## SOLID — S: Single Responsibility Principle
- A class should have only ONE reason to change
- If a class does 4 things → 4 reasons to change → split it

## Real example:
- UserService doing validate + save + email + log → WRONG
- UserValidator, UserRepository, WelcomeEmailSender → each does 1 thing

## Interview one-liner:
"SRP means a class should have only one reason to change.
If it does multiple things, split it into focused classes."

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