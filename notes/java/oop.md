# OOP Principles

## Encapsulation
- Hide internal state (private fields)
- Expose controlled access (public methods)
- Goal: protect invariants — keep object in valid state always

## Real example:
- BankAccount.balance → private
- deposit/withdraw → validates before changing state
- Outsider cannot set balance = -500

## Interview one-liner:
"Encapsulation protects the invariants of a class by hiding
internal state and controlling access through methods."