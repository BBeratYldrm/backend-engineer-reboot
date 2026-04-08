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

