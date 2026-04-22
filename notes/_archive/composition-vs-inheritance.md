# Composition vs Inheritance

## Inheritance

class Dog extends Animal

### Problems
- Tight coupling
- Hard to change

---

## Composition

class Car {
Engine engine;
}

### Advantages
- Loose coupling
- More flexible
- Easier to extend

---

## Rule

Prefer composition over inheritance

---

## Key Idea
- HAS-A > IS-A (in most cases)