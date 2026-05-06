# [7.1] Observability

## Keywords
observability · prometheus · grafana · distributed-tracing · jaeger ·
zipkin · opentelemetry · red-method · use-method · trace-id · span ·
correlation-id · metrics · logs · traces · alert · p95 · p99 · sla

---

## Three Pillars of Observability

Logs (ELK Stack):
→ What happened? Error details, event trail, debug info
→ Covered in 13-elk-stack.md

Metrics (Prometheus + Grafana):
→ How is the system performing? Numerical measurements over time
→ CPU, memory, request rate, error rate, response time

Traces (Jaeger / Zipkin / OpenTelemetry):
→ Where is the bottleneck? Request journey across services
→ Which service is slow in a chain of 5 services?

Together:
Grafana alert fires (metric) →
check ELK for error details (log) →
check Jaeger for slow service (trace)

---

## Prometheus — Metrics Collection

Pull model: Prometheus periodically scrapes metrics from applications.
Every 15 seconds → hits /actuator/prometheus → collects metrics.

Spring Boot exposes metrics automatically:
management:
endpoints:
web:
exposure:
include: health, prometheus

Metrics Prometheus collects:
→ JVM: heap usage, GC pause time, thread count
→ HTTP: request count, error rate, response time
→ Custom: business metrics (reservations per hour, payment failures)

Custom metric example:
@Autowired MeterRegistry registry;

Counter reservations = registry.counter("reservations.created");
reservations.increment(); // called when reservation created

---

## Grafana — Visualization and Alerting

Queries Prometheus data → renders dashboards and charts.

Prometheus = data collection and storage
Grafana    = visualization and alerting

Grafana dashboard shows:
→ Request rate over time
→ Error rate percentage
→ p95/p99 response times
→ CPU and memory usage
→ Active DB connections

Alerting:
Error rate > 1% for 5 minutes → fire alert → PagerDuty → on-call engineer

---

## RED Method — Service Health

Three metrics that define service health.
Applies to every service in the system.

Rate:     how many requests per second?
Errors:   what percentage are failing?
Duration: how long do requests take? (p95, p99)

PayPay Card SLA example:
p99 response time < 500ms
Error rate < 0.1%
These are monitored in Grafana, alerted when breached.

p95 vs p99:
p95 → 95% of requests faster than this value
p99 → 99% of requests faster than this value
p99 matters more — covers the worst 1% that real users experience

---

## USE Method — Infrastructure Health

For servers, databases, thread pools — not services.

Utilization: how busy is the resource? (CPU 80%)
Saturation:  is there a queue building up? (thread pool full?)
Errors:      are there errors at resource level?

Rakuten connection:
Gatling stress test results showed p95 response time.
High p95 → USE method → CPU saturation? DB connection pool full?
That investigation led to the SQL optimization (8s → 3s).

---

## Distributed Tracing

Problem:
User request touches 5 services. Total response time: 500ms.
Which service is slow? Logs alone cannot answer this.

Solution: Trace ID + Spans

Every request gets a unique Trace ID at entry point.
Each service creates a Span — records start time, end time, service name.
Trace ID passed to every downstream service call.

Trace ID: abc-123
API Gateway      span: 5ms
Order Service    span: 20ms
Payment Service  span: 150ms  ← bottleneck
Bank API         span: 100ms  ← called by Payment
Stock Service    span: 30ms

Payment Service is slow. Without tracing, impossible to know.

Spring Boot auto-configuration:
<dependency>
<groupId>io.micrometer</groupId>
<artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>

Automatically adds Trace ID to every request.
Propagates through RestTemplate, WebClient, Kafka headers.
No manual code needed per service.

Correlation ID vs Trace ID:
Correlation ID → manually added, simpler, used in logs
Trace ID       → auto-generated, carries timing data, used in tracing tools

---

## Tools

Prometheus:
→ Open source, CNCF project
→ Pull model, time-series DB
→ PromQL query language
→ Standard in Kubernetes ecosystem

Grafana:
→ Open source, visualization
→ Connects to Prometheus, Elasticsearch, and many others
→ Dashboards, alerts, on-call routing

Jaeger:
→ Open source distributed tracing (Uber created)
→ Visualizes trace timeline across services

Zipkin:
→ Twitter created, older, simpler
→ Similar to Jaeger

OpenTelemetry:
→ Vendor-neutral standard for metrics, logs, traces
→ Write once, send to Jaeger, Zipkin, Datadog, New Relic — any backend
→ Industry moving toward this standard
→ Woven by Toyota job listing mentioned this

---

## Rakuten Connection

Prometheus + Grafana monitored:
→ Server CPU, RAM, disk
→ HTTP error rates (404s, 500s)
→ Response times
→ JVM metrics

ELK for logs — Kibana for investigation.
Gatling stress test results fed into this observability picture.

---

## Interview Checklist
→ Three pillars of observability? → logs, metrics, traces
→ Prometheus pull vs push? → pull — Prometheus scrapes endpoints periodically
→ RED method? → Rate, Errors, Duration — service health
→ USE method? → Utilization, Saturation, Errors — infrastructure health
→ p95 vs p99? → 95th/99th percentile response time, p99 covers worst real user experience
→ Distributed tracing why? → identify bottleneck across multiple services
→ Trace ID vs Correlation ID? → trace has timing spans, correlation is simpler log linking
→ OpenTelemetry? → vendor-neutral standard, write once run anywhere
→ Logs vs metrics vs traces together? → what happened, how performing, where slow