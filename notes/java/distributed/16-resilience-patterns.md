# [4.3] Resilience Patterns

## Keywords
resilience · circuit-breaker · bulkhead · timeout · retry ·
rate-limiter · fallback · cascading-failure · fast-fail ·
resilience4j · half-open · failure-isolation · thread-pool-isolation

---

## What Are Resilience Patterns

Resilience patterns answer one question:
"When something goes wrong, how does the system stay alive?"

In distributed systems, failure is not exceptional — it is expected.
Services go down, networks blip, dependencies slow down.
Resilience patterns contain the damage and keep the rest running.

They are a category of distributed systems patterns — alongside
SAGA (transaction management), CQRS (read/write separation),
Outbox (event reliability).

The four core resilience patterns:
Timeout         → do not wait forever
Retry           → try again on temporary failure
Circuit Breaker → stop calling a broken service
Bulkhead        → isolate failure, prevent spreading

Used together — each covers what the others cannot.

---

## The Problem — Cascading Failure

Without resilience patterns:

Payment service slows down (30s response time)
→ ShopSearchService threads fill up waiting
→ ShopSearchService stops responding
→ Services depending on ShopSearch fail
→ Entire system down

One slow service took down everything.
This is cascading failure — the domino effect in distributed systems.

---

## Timeout — Do Not Wait Forever

Simplest pattern. Set a maximum wait time.

Without timeout:
Thread waits 30 seconds → holds resources → pool fills up → system chokes

With timeout:
Thread waits 3 seconds → no response → fail fast → thread released → system breathes

@Bean
public RestTemplate restTemplate() {
HttpComponentsClientHttpRequestFactory factory =
new HttpComponentsClientHttpRequestFactory();
factory.setConnectTimeout(1000);  // 1s connection timeout
factory.setReadTimeout(3000);     // 3s read timeout
return new RestTemplate(factory);
}

Trade-offs:
+ Releases threads quickly — system stays responsive
+ User gets fast feedback instead of waiting
- Too short → false failures on slow but healthy services
- Too long → does not protect fast enough

Rule of thumb: timeout should be lower than the caller's timeout.
If the user waits 10s, internal calls should timeout at 3s.

---

## Retry — Try Again on Temporary Failure

Network blip, brief service restart — transient failures.
Worth retrying. Not worth giving up immediately.

Always use Exponential Backoff:
Attempt 1 → immediate
Attempt 2 → wait 1s
Attempt 3 → wait 2s
Attempt 4 → wait 4s → failed → DLQ or error

Why backoff: hammering a struggling service makes it worse.
Backoff gives it time to recover.

Always pair with Idempotency:
Retry sends the same request multiple times.
Receiver must not process it twice.
Unique operation ID → check before processing → skip if already done.

Trade-offs:
+ Handles transient failures automatically
- Without idempotency → duplicate operations (payments, orders)
- Without backoff → retry storm worsens the outage
- Not appropriate for non-idempotent operations without ID protection

---

## Circuit Breaker — Stop Calling a Broken Service

Named after electrical circuit breakers — trips when overloaded, protects the system.

Three states — transitions happen automatically at runtime.
You do not manually open or close it.
Resilience4j monitors failure rate and transitions automatically.
You only configure the thresholds upfront.

CLOSED (normal operation):
→ Requests pass through
→ Library counts successes and failures in a sliding window

OPEN (tripped):
→ Failure rate exceeded threshold
→ Requests blocked immediately — service not called at all
→ Fast fail: returns error in 0ms instead of waiting 30s
→ Stays open for configured wait duration

HALF-OPEN (testing recovery):
→ After wait duration, allows a few test requests through
→ Success → back to CLOSED
→ Failure → back to OPEN

@CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
public PaymentResponse charge(PaymentRequest request) {
return paymentClient.charge(request);
}

public PaymentResponse paymentFallback(PaymentRequest request, Exception e) {
return PaymentResponse.unavailable("Payment temporarily unavailable");
}

resilience4j:
circuitbreaker:
instances:
paymentService:
failure-rate-threshold: 50        # 50% failure rate → OPEN
wait-duration-in-open-state: 30s  # wait 30s → try HALF-OPEN
permitted-calls-in-half-open: 3   # 3 test calls in HALF-OPEN
sliding-window-size: 10           # measure last 10 calls

Fallback options when OPEN:
→ Return cached data
→ Return a safe default response
→ Return a meaningful error message (fast fail)
→ Redirect to degraded but functional path (graceful degradation)

Netflix example:
Recommendation service circuit opens
→ Fallback: return popular movies list
→ User sees something instead of blank screen
→ Graceful degradation — partial functionality beats total failure

Trade-offs:
+ Prevents cascading failure — broken service isolated
+ Fast fail — threads released immediately
+ Self-healing — HALF-OPEN tests recovery automatically
- False positives — might open on healthy but momentarily slow service
- Fallback must be designed carefully — bad fallback worse than no fallback

---

## Bulkhead — Isolate Failure, Prevent Spreading

Named after ship compartments — watertight sections prevent whole ship from sinking.

Without Bulkhead — shared thread pool:
Payment service slow → fills shared thread pool (200 threads)
→ Shop search needs threads → none available
→ Shop search fails — nothing to do with payment

With Bulkhead — isolated thread pools:
Payment service  → own pool: 10 threads
Shop search      → own pool: 20 threads
Notification     → own pool: 5 threads

Payment fills its 10 threads → only payment degrades
Shop search runs normally — separate pool, unaffected

@Bulkhead(name = "paymentService", type = Bulkhead.Type.THREADPOOL)
public PaymentResponse charge(PaymentRequest request) {
return paymentClient.charge(request);
}

resilience4j:
bulkhead:
instances:
paymentService:
max-concurrent-calls: 10  # max 10 concurrent requests
max-wait-duration: 0      # if full → fail immediately, do not queue

Circuit Breaker vs Bulkhead — different concerns:
Circuit Breaker → time-based: "too many errors over time → stop calling"
Bulkhead        → resource-based: "too many concurrent calls → reject new ones"

They complement each other:
Bulkhead        → failure cannot spread to other services
Circuit Breaker → do not keep calling an already broken service

---

## Rate Limiter — Control Incoming Traffic

Limits how many requests a service accepts per time window.
Protects the service from being overwhelmed.

Different from Bulkhead:
Bulkhead     → limits concurrent calls going OUT to a dependency
Rate Limiter → limits calls coming IN to your service

resilience4j:
ratelimiter:
instances:
shopSearch:
limit-for-period: 100      # 100 requests
limit-refresh-period: 1s   # per 1 second
timeout-duration: 0        # if limit hit → fail immediately

---

## How They Work Together

Request arrives at service
→ Rate Limiter: am I receiving too many requests? (protect self)
→ Bulkhead: do I have capacity to call the dependency? (isolate)
→ Circuit Breaker: is the dependency healthy? (fast fail if not)
→ Timeout: how long will I wait? (release thread if too long)
→ Retry: was it a transient failure? (try again with backoff)
→ Fallback: if all fails, what do I return? (graceful degradation)

Rakuten connection:
ShopSearchService calling external shop API:
→ Timeout: 3s max wait
→ Retry: 3 attempts with exponential backoff
→ Circuit Breaker: if >50% fail → OPEN for 30s
→ Bulkhead: max 10 concurrent calls to shop API
→ Fallback: return cached shop list

---

## Senior-Level Framing

Mentioning Circuit Breaker alone is junior level.
Framing it as part of resilience patterns is senior level.

In system design interviews:
"Resilience patterns handle failure containment in distributed systems.
The four core ones are Timeout, Retry, Circuit Breaker, and Bulkhead.
I use them together — Timeout prevents thread exhaustion,
Retry handles transient failures with backoff and idempotency,
Circuit Breaker stops cascading failure by fast-failing when a dependency is broken,
Bulkhead ensures one slow dependency cannot take down unrelated services.
In Java, Resilience4j implements all four with Spring Boot integration."

---

## Interview Checklist
→ What are resilience patterns? → patterns for keeping systems alive when dependencies fail
→ Cascading failure? → one slow service takes down everything through thread exhaustion
→ Four core patterns? → Timeout, Retry, Circuit Breaker, Bulkhead
→ Circuit Breaker states? → CLOSED normal, OPEN fast-fail, HALF-OPEN testing recovery
→ Who opens/closes Circuit Breaker? → Resilience4j automatically, based on failure rate
→ Bulkhead vs Circuit Breaker? → resource isolation vs failure rate detection
→ Rate Limiter vs Bulkhead? → incoming traffic limit vs outgoing concurrent call limit
→ Fallback options? → cached data, safe default, fast fail, graceful degradation
→ Resilience4j? → Java library implementing all four patterns, Spring Boot integrated