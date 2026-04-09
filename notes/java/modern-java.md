# Modern Java — Records

## What is it, really?
A record is just a data carrier. Think of it like a envelope —
you put data in, pass it around, someone opens it. That's it.
No behavior, no state change. Just data.

Java auto-generates everything you'd normally write by hand:
constructor, getters, equals, hashCode, toString.

public record UserResponse(String name, String email) {}
// This one line replaces ~30 lines of boilerplate. That's it.

## The mental model that clicked for me
"If it's just clothing — wear it and move on" → Record
"If it has a life, changes over time" → Class

A UserResponse just carries data to the frontend. Clothing.
A User entity gets updated, has business logic. It has a life.

## When it makes sense
→ API responses (UserResponse, OrderResponse)
→ API requests (CreateOrderRequest, LoginRequest)  
→ Returning multiple values from a method (MinMax, PageResult)
→ Value objects that never change (Money, Coordinate, Point)

## When it breaks down
→ JPA Entities — Hibernate needs to mutate fields internally.
@Entity + record = trouble. Always use class for entities.

→ Anything with state that changes over time:
BankAccount.balance changes → not a record
Order.status goes PENDING → SHIPPED → not a record

→ When you need inheritance:
Records can implement interfaces but cannot extend classes.

## The question I ask myself now
"Does this thing just carry data, or does it do something?"
→ Just carries data? Record.
→ Does something / changes over time? Class.

## Real example from work
UserResponse, OrderRequest, ErrorResponse → all Records.
User, Order, BankAccount → always Class.

# JVM Memory — What I need to know

## Three areas
→ Heap: where objects live (new User(), new ArrayList())
→ Stack: method calls, local variables, primitives
→ Metaspace: class definitions (not objects)

## Garbage Collection
→ When no reference points to an object, GC cleans it up
→ We don't control when — JVM decides
→ Help GC by not holding references you don't need

## Two errors I should recognize
→ StackOverflowError: infinite recursion, stack fills up
→ OutOfMemoryError: too many objects, heap fills up

## The question I ask myself
"Where does this live?"
→ new Something() → Heap
→ int x = 5 inside a method → Stack