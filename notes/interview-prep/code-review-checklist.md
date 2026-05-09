# Code Review — Universal Rules

## Security
1. Null check — validate every input before use
2. Input validation — format, type, length, range
3. Sensitive data in logs — never log passwords, tokens, card numbers, personal IDs
4. Injection risk — SQL, command, LDAP injection vulnerabilities
5. Authentication/Authorization — who is allowed to call this method?

## Resource Management
6. Resource leak — are Connection, Stream, File objects closed?
7. try-with-resources — Closeable objects properly handled?
8. Memory leak — large objects held longer than necessary?

## Error Handling
9. Exception handling — are catch blocks correct and meaningful?
10. Silent failure — empty catch blocks swallowing exceptions?
11. Meaningful error messages — are exceptions descriptive?
12. Raw data in catch — sensitive data logged in error blocks?

## Code Quality
13. Naming — are variable/method names descriptive and clear?
14. Variable reuse — same name used for different data?
15. SRP — does each method do only one thing?
16. DIP — is new called inside constructor? Should be injected.
17. Magic numbers/strings — hardcoded values should be constants

## Performance
18. Logger placeholder — use {} not string concatenation
19. Encoding — charset specified in String/byte conversions?
20. Unnecessary object creation — new inside loops?

## Testability
21. Dependency injection — dependencies injected, not created inside?
22. Method size — too large? Should be broken into smaller methods?

## General
23. Readability — can someone else understand this easily?
24. Code duplication — DRY principle violated?
25. Thread safety — shared mutable state accessed by multiple threads?