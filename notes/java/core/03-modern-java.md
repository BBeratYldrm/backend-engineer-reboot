# [1.2] Modern Java (17 → 21 → 25)

## Records

Problem: DTO classes require too much boilerplate.
3 fields → constructor + getters + equals + hashCode + toString = 50+ lines

Solution:
public record ShopSearchItem(
String shopName,
String address,
double distance
) {}

Auto-generated: constructor, getters (shopName() not getShopName()),
equals(), hashCode(), toString()

Records are immutable by default — fields are final.

When to use:
+ DTO — carry data, no business logic
+ Request/Response — API boundary
+ Value Object — immutable value

When NOT to use:
- JPA Entity — needs to be mutable, requires no-arg constructor
- Service class — has business logic
- Classes that need inheritance — records can't extend classes

Rakuten connection:
ShopSearchItem, ShopSearchResponse → could have been Records
ZipCode entity → should NOT be a Record (JPA entity)

Interview tip:
"Records eliminate DTO boilerplate — constructor, getters, equals, hashCode,
toString all generated automatically. They're immutable by design,
making them perfect for Value Objects and API boundaries."

## var — Local Variable Type Inference

Java 10+. Compiler infers the type — less boilerplate.

var items = new ArrayList<String>(); // type is clear → OK
var user = userRepository.findById(id); // unclear what returns → avoid

When to use:
+ Type is obvious from right side → var items = new ArrayList<>()
+ Long generic types → var map = new HashMap<String, List<Integer>>()

When NOT to use:
- Return type of a method
- Field declaration
- When type is not obvious → confusing for readers
- Lambda parameters

The question I ask myself:
"Is the type obvious from the right side of the assignment?"
+ Yes → var is fine
+ No → write the type explicitly