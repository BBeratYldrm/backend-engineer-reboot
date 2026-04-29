# [2.3] Spring MVC

## Controller Design — Thin Controller

Controller's only job: receive HTTP request, delegate to service, return response.
Validation, business logic, DB operations do not belong in the controller.

@RestController
@RequiredArgsConstructor
public class OrderController {
private final OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest req) {
        OrderResponse response = orderService.createOrder(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

## Validation

@Valid → automatically validates, throws MethodArgumentNotValidException on failure

public record OrderRequest(
@NotNull @Positive Double amount,
@NotBlank String currency
) {}

Validation Groups — same DTO, different rules per scenario:
@NotNull(groups = OnCreate.class) → required only on create
@Validated(OnCreate.class) → activate group in controller

Use case: create requires customerId, update does not.
Same DTO, two different validation profiles.

## REST Semantics

Safe: no side effects — does not modify state (GET)
Idempotent: calling N times produces the same effect on the system as calling once

GET    → safe + idempotent (no side effect, same system impact every time)
PUT    → idempotent (same update applied N times = same result)
DELETE → idempotent (deleting something that's already gone = same outcome)
POST   → neither safe nor idempotent (each call creates a new resource)
PATCH  → can be idempotent depending on implementation

Idempotency is about the effect on the system, not the response value.
GET may return different data if DB changed — that does not break idempotency.
The request itself caused no side effect. That is what matters.

HTTP Status Codes:
200 → successful GET / PUT / PATCH
201 → successful POST, new resource created
204 → successful DELETE, no body
400 → client error, validation failure
401 → no identity (not authenticated — "I don't know who you are")
403 → identity known, no permission (not authorized — "I know who you are, you can't do this")
404 → resource not found
409 → conflict (e.g. email already exists)
422 → unprocessable (format valid, semantics wrong)
500 → server error

401 vs 403 — frequently asked:
401 → authenticate first
403 → authenticated but not allowed

## Global Exception Handling

One central place for all exception → HTTP status mapping.
No try-catch in controllers. No stack traces sent to clients.

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(OrderNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("ORDER_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .toList();
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("VALIDATION_FAILED", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("INTERNAL_ERROR", "Something went wrong"));
    }
}

Problem Details — RFC 7807 (Spring Boot 3+):
Standard error response format across APIs.
{
"type": "https://api.tirebringin.com/errors/order-not-found",
"title": "Order Not Found",
"status": 404,
"detail": "Order with id 123 does not exist",
"instance": "/orders/123"
}

Clients can parse errors consistently across different APIs.

## API Versioning

URI versioning    → /api/v1/orders       (most common, cacheable, explicit)
Header versioning → Accept-Version: v2   (clean URL, harder to cache)
Media type        → Accept: application/vnd.app.v2+json (most RESTful, most complex)

In practice: URI versioning is the default choice — simple and tooling-friendly.

## Rakuten Connection

ShopSearchService exposed as REST endpoint.
GET /shops?zipCode=1234567
→ Controller received zip code, passed to ShopSearchService
→ Service ran business logic
→ Controller returned List<ShopSearchResponse> with 200 OK

Validation: zip code format checked with @Pattern in controller layer.
Exception: zip code not found → ValidationException → GlobalExceptionHandler → 400 Bad Request.

## Interview Checklist
→ What belongs in a controller? → HTTP input/output only, no business logic
→ What are validation groups? → same DTO, different rules per scenario
→ What does idempotent mean? → same system effect regardless of how many times called
→ 401 vs 403? → not authenticated vs not authorized
→ Global exception handling? → @RestControllerAdvice, central domain-to-HTTP mapping
→ Problem Details? → RFC 7807 standard error format, Spring Boot 3+ built-in