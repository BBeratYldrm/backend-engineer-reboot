# Java Object Model

## equals()
- Object equality check
- Default: reference comparison

## hashCode()
- Used in HashMap / HashSet
- Determines bucket

## Important Rule
If equals() is overridden, hashCode() must also be overridden.

## Why?
Hash-based collections use hashCode first, then equals.