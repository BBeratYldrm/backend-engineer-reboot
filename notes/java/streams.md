# Stream API

## Pipeline
source → intermediate operations → terminal operation

## Intermediate (lazy — terminal gelene kadar çalışmaz)
- filter(predicate) → condition'a uyanlar
- map(function) → her elemana işlem
- sorted() → sırala
- distinct() → unique elemanlar

## Terminal (stream'i bitirir)
- collect(Collectors.toList()) / toList()
- reduce(identity, accumulator)
- sum() / count() / min() / max()
- forEach()

## Real example:
numbers.stream()
.filter(n -> n % 2 == 0)
.map(n -> n * n)
.toList();

## Interview one-liner:
"Streams are lazy — intermediate operations don't run
until a terminal operation is called."