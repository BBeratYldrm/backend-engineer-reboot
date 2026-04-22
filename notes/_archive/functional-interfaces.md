# Functional Interfaces

## Definition
Interface with exactly ONE abstract method.
Enables lambda expressions.

## The 3 Most Important

| Interface | Takes | Returns | Used in |
|-----------|-------|---------|---------|
| Predicate<T> | T | boolean | filter() |
| Function<T,R> | T | R | map() |
| Supplier<T> | nothing | T | orElseThrow() |

## Examples
Predicate<Integer> isEven = n -> n % 2 == 0;
Function<String, Integer> length = s -> s.length();
Supplier<String> greeting = () -> "Hello!";

## Interview one-liner:
"Functional interfaces have one abstract method and
enable lambdas. Predicate tests, Function transforms,
Supplier produces."