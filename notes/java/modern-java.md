# Modern Java

## Records (Java 16+)
- Immutable data carrier — once created, cannot change
- Auto-generates: constructor, getters, equals, hashCode, toString
- Replaces boilerplate DTO classes

## When to use:
✅ DTOs (UserResponse, OrderRequest)
✅ Value Objects (Money, Coordinate)
✅ Return types carrying multiple values

## When NOT to use:
❌ JPA Entities — Hibernate needs mutable fields
❌ When state needs to change
❌ When inheritance needed (records can't extend classes)

## Example:
public record UserResponse(String name, String email) {}
// That's it. Constructor, getters, equals, hashCode, toString — all included.