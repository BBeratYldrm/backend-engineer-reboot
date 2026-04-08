# Optional

## Why?
- Replaces null returns — makes "might be empty" explicit
- Forces caller to handle the empty case

## Common usage — DAO/Repository layer
- // Başka bir yerde: 
  - Optional<User> optionalUser = findUserByEmail("berat@gmail.com");

- // Kullanım 1 — varsa işlem yap
  - optionalUser.ifPresent(user -> System.out.println(user.getName()));

- // Kullanım 2 — yoksa default değer
  - User user = optionalUser.orElse(new GuestUser());

- // Kullanım 3 — yoksa exception fırlat
  - User user = optionalUser.orElseThrow(() -> new UserNotFoundException("User not found"));

## Key methods
- Optional.ofNullable(value) → wrap, null olabilir
- Optional.of(value) → wrap, null olamaz (exception fırlatır)
- .ifPresent(consumer) → varsa işlem yap
- .orElse(default) → yoksa default döner
- .orElseThrow(() -> ex) → yoksa exception fırlat
- .map(function) → varsa dönüştür

## Anti-patterns
1. Field olarak kullanma:
    - Optional is not Serializable
    - Not designed for fields, designed for return types

2. Nested optional — Optional<Optional<X>> → never do this

3. isPresent() + get() → code smell
   if (opt.isPresent()) { opt.get() } // No
   opt.ifPresent(...)                  // Yes

## Interview one-liner:
"Optional is designed for method return types to signal
that a value might be absent. Never use it as a field."