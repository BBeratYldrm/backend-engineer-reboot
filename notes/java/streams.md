# Java Streams

## What is Stream?
- Process collections in a functional way

## Common Operations

map → transform data  
filter → select data  
forEach → iterate  
collect → convert to list

## Example
list.stream()
.filter(x -> x > 5)
.map(x -> x * 2)
.collect(Collectors.toList());