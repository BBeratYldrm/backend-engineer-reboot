# Java Encapsulation & Abstraction

## Encapsulation

- Protect internal state
- Use private fields + controlled access
- Prevent invalid data

### Example
private double balance;

public void deposit(double amount) {
    if (amount <= 0) throw new IllegalArgumentException();
    balance += amount;
}

### Key Idea
- Not just getter/setter
- Enforces rules and invariants

---

## Abstraction

- Hide implementation details
- Expose only necessary behavior

### Example
interface Payment {
    void pay(double amount);
}

### Key Idea
- User doesn’t know how it works
- Only knows what to use

---

## Difference

Encapsulation → protects data  
Abstraction → hides complexity