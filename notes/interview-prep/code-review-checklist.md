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


# Code Review Checklist — 5-Step Structure

A fixed order to follow every time, most critical to least critical.
Not every step will find something — that's fine. The point is the habit,
not finding N issues every time.

**Why this order.** This isn't arbitrary — it follows the same severity
logic used in professional review practice: check what's most expensive
to get wrong in production first, and leave what's cheapest to fix later
for last.

- A correctness bug (Step 1) can cause data loss or a production incident.
- A structural problem (Step 2) makes the codebase harder to change safely,
  but doesn't break anything today.
- A performance issue (Step 3) degrades gracefully until it suddenly doesn't.
- Weak error handling (Step 4) only matters once something else goes wrong.
- A naming or readability issue (Step 5) is annoying but never causes an
  incident by itself — it's the cheapest to fix at any point in time.

Cost-of-being-wrong is the ordering principle. Everything else follows from it.

---

## Step 1 — Correctness & Data Integrity

The code must not silently produce wrong or inconsistent data.

- Is there a transaction boundary where one is needed? (`@Transactional`)
- Does the transaction boundary include things that shouldn't be inside it?
  (slow external calls, email/notification sending, HTTP calls to other services)
- Null / Optional handling — any `.get()` on an `Optional` without a check?
  Any unguarded `.get(0)`, unchecked map lookups, etc.?
- Are exceptions swallowed silently (empty catch block) or too broad (`catch (Exception e)`)?
- Race conditions — is shared mutable state touched by multiple threads without protection?

## Step 2 — Responsibility & Structure (SOLID, mainly SRP)

Does each class/method do one thing, and one thing only?

- Can you summarize what a method does in one sentence without using "and"?
  If not, it's probably doing too much.
- Is business logic mixed with orchestration mixed with side effects
  (DB write + external call + notification all in one method)?
- Constructor injection vs field injection — are dependencies explicit and testable?
- Is there logic here that belongs in a different layer (e.g. business rule
  living in a controller, or persistence detail leaking into a service)?

## Step 3 — Performance & Query Efficiency

Does this scale, or does it quietly get slower as data grows?

- N+1 queries — is there a DB call inside a loop that could be pulled outside
  or replaced with a single batched query / JOIN FETCH?
- Repeated identical queries — same lookup done more than once for no reason?
- Unbounded result sets — is there pagination where the data could grow large?
- Any unnecessary object creation inside a hot loop?

## Step 4 — Error Handling & Observability

When this breaks in production, will anyone know why?

- Are exceptions meaningful (custom exception types) or generic
  (`RuntimeException`, `NoSuchElementException` with no context)?
- Is there logging at the right level for failures? (not everything as INFO,
  not everything as ERROR)
- Are external calls (email, HTTP, message queue) protected with retry /
  timeout / fallback, or can one slow dependency block everything?

## Step 5 — Naming, Readability & Magic Values

Would a new team member understand this without asking questions?

- Magic strings / numbers that should be constants or enums
  (e.g. `"PENDING"`, `"CONFIRMED"` as raw strings instead of an enum)
- Method and variable names — do they say what they do, or are they vague
  (`data`, `helper`, `process()`)?
- Is there dead code, commented-out code, or leftover debug logging?

---

## How to use this in practice

1. Read the code once fully before commenting on anything — get the full picture first.
2. Go through Steps 1 → 5 in order. Stop mentally re-ordering; the order is deliberate —
   correctness bugs matter more than a bad variable name.
3. Not every step needs a finding. Silence on a step is a valid outcome.
4. When you flag something, also say *why* it matters — not just "this is wrong."