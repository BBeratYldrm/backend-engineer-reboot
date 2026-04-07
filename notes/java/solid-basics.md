# SOLID Principles (Basics)

## SRP (Single Responsibility Principle)

- One class → one responsibility
- One reason to change

### Bad
UserService → save + email

### Good
UserService + EmailService

---

## OCP (Open/Closed Principle)

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