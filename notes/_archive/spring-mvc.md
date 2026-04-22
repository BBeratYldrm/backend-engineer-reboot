# [2.3] Spring MVC

## How a request flows
Request
→ DispatcherServlet (Spring's front controller — routes all requests)
→ Controller (receives request, delegates to service)
→ Service (business logic)
→ Repository (data access)
→ DB
← same path back

---

## DispatcherServlet
Spring MVC's "gatekeeper."
Receives every request, decides which Controller handles it.
Interview one-liner:
"DispatcherServlet is the front controller in Spring MVC.
It receives all requests and routes them to the right handler."

---

## Thin Controller — the most important rule
Controller only does 3 things:
→ Receive request
→ Delegate to service
→ Return response

Nothing else. No business logic, no validation, no DB calls.

## Fat Controller — what to avoid
- if/else validation inside controller
- Direct repository calls
- Business logic (calculations, decisions)
- Exception throwing with new RuntimeException()
- Returning entity directly (use DTO/Response instead)

## Correct Controller structure
@RestController
@RequiredArgsConstructor
public class OrderController {
private final OrderService orderService;

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid CreateOrderRequest request) {
        return ResponseEntity.status(201).body(orderService.createOrder(request));
    }
}

---

## Key annotations
@RestController  → @Controller + @ResponseBody combined
@GetMapping      → HTTP GET
@PostMapping     → HTTP POST
@PathVariable    → /orders/{id} → id
@RequestBody     → JSON body → Java object
@Valid           → trigger validation on request object

---

## @Valid — Request Validation
Put validation rules on DTO fields, not in Controller.
@Valid on @RequestBody triggers automatic validation before method runs.

Common annotations:
@NotNull    → cannot be null
@Email      → valid email format
@Size       → min/max length
@NotBlank   → not null and not empty

If validation fails → MethodArgumentNotValidException thrown automatically.
Controller method never executes.

---

## GlobalExceptionHandler — @ControllerAdvice
Centralizes all exception handling in one place.
Without it → try/catch in every controller method → code duplication.

@ControllerAdvice
public class GlobalExceptionHandler {
@ExceptionHandler(UserNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNotFound(UserNotFoundException ex) {
return ResponseEntity.status(404).body(new ErrorResponse(ex.getMessage()));
}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(...) {
        return ResponseEntity.status(400)...
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(500).body(new ErrorResponse("Internal server error"));
    }
}

Why @ControllerAdvice:
- One place for all exception handling
- SRP — controller doesn't handle exceptions
- DRY — no try/catch duplication across controllers

---

## REST Semantics

HTTP Methods:
GET    → read data         → 200 OK
POST   → create new        → 201 Created
PUT    → full update       → 200 OK
PATCH  → partial update    → 200 OK
DELETE → delete            → 204 No Content

Common Status Codes:
200 OK           → success
201 Created      → new resource created
204 No Content   → success, nothing to return
400 Bad Request  → client error (validation failed)
401 Unauthorized → not logged in
403 Forbidden    → logged in but no permission
404 Not Found    → resource doesn't exist
500 Server Error → our mistake

Idempotency:
Idempotent → same result no matter how many times called
+ GET    → always returns same data
+ PUT    → same payload = same result
+ DELETE → 2nd call returns 404 but system unchanged

Not idempotent:
- POST → each call creates a new record

---

## Safe Methods
Safe = does not modify server state, read-only

Safe:     GET, HEAD, OPTIONS
Not safe: POST, PUT, PATCH, DELETE

Note — idempotent vs safe:
GET    → safe + idempotent
DELETE → idempotent but NOT safe (it deletes data)
POST   → neither safe nor idempotent

---

## Boundary Validation
Validation happens at the system boundary — before data enters the service layer.
This is exactly what @Valid does on @RequestBody.
Controller is the boundary — invalid data never reaches service.

---

## Validation Groups
Problem: same DTO used for create and update, but different fields required.

Solution 1 — Separate DTOs (preferred):
CreateUserRequest — no id field
UpdateUserRequest — id field required
Clean, explicit, easy to read.

Solution 2 — Validation Groups:
Define marker interfaces (OnCreate, OnUpdate)
Annotate fields with group: @NotNull(groups = OnUpdate.class)
Use @Validated(OnCreate.class) in controller instead of @Valid
Works but adds complexity — avoid unless necessary.

Real world preference:
Separate DTOs > Validation Groups
Simpler, more readable, easier to maintain.

---

## Error Response Standard — Problem Details (RFC 7807)

Spring's default error response is not informative enough for clients.
Standard approach: consistent error response across all endpoints.

Structure:
{
"status": 400,
"title": "Validation Failed",
"detail": "email: Invalid email format, name: cannot be null",
"instance": "/users"
}

Why it matters:
+ Client knows exactly what went wrong
+ Client knows which field to fix
+ Consistent across all endpoints
+ Frontend can parse and display errors properly

Rule:
Every API should return the same error response structure.
Never return different error shapes from different endpoints.

---

## API Versioning

Problem: breaking changes in API without breaking existing clients.

3 approaches:

URI versioning (preferred):
GET /api/v1/users
GET /api/v2/users
+ Explicit, easy to test, cache-friendly
+ Most widely used in practice

Header versioning:
GET /api/users
Header: API-Version: 2
+ Keeps URI clean
- Harder to test, not visible in browser

Media type versioning:
Accept: application/vnd.company.v2+json
+ Most RESTful
- Complex, rarely used in practice

Interview answer:
"I prefer URI versioning — explicit, easy to test,
and straightforward for clients to understand."