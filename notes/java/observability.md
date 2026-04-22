# [7.1] Observability

## Three Pillars of Observability
Logs    → "What happened?" — events, errors, debug info
Metrics → "How is it going?" — numbers, trends, thresholds
Tracing → "Where did it go wrong?" — request journey across services

## ELK Stack — Logging

E → Elasticsearch  — stores and indexes logs, fast search
L → Logstash       — transforms, filters, enriches logs
K → Kibana         — visualization, dashboards, search UI
+ Filebeat         — reads log files, ships to Elasticsearch

Flow:
App writes JSON logs → Filebeat reads → Elasticsearch indexes → Kibana shows

Why JSON logs:
→ Searchable by any field (reservationId, userId, level)
→ "Find all errors for userId=789" → instant
→ Plain text logs can't do this efficiently

Rakuten:
Tirebringin wrote logs to /var/log/tirebringin/app.log.json
Filebeat installed on server → watched log file
New log line → Filebeat ships to Elasticsearch
Kibana → search by reservationId during incidents

Production incident flow:
User reports error → get their userId
→ Kibana: filter by userId + time range
→ See exact error, stack trace, which service

## Metrics — Prometheus + Grafana

ELK tells you WHAT happened.
Prometheus tells you HOW the system is performing.

Metrics examples:
→ CPU: 45%
→ Heap memory: 2GB / 4GB
→ HTTP requests: 1200/sec
→ Error rate: 0.3%
→ DB connection pool: 8/10

Spring Boot setup:
implementation 'io.micrometer:micrometer-registry-prometheus'
management.endpoints.web.exposure.include: prometheus

/actuator/prometheus → Prometheus scrapes every 15s
Grafana → reads from Prometheus → shows dashboards, alerts

Alerting:
"Error rate > 1% for 5 minutes" → PagerDuty alert → on-call engineer
"Heap > 90%" → alert before OOM crash

## Distributed Tracing

Problem in microservices:
Request goes through 5 services. Error in service C.
Which request? No connection between logs. ❌

Solution: Trace ID — unique ID per request, passed through all services.

API Gateway → generates Trace ID: "abc-123"
→ passes in header: X-Trace-Id: abc-123
→ every service logs with this ID

Service A: [abc-123] processed request
Service B: [abc-123] called payment
Service C: [abc-123] ERROR — DB timeout ← found it ✅

Tools:
Zipkin → open source, simple
Jaeger → more advanced, better UI
Spring Cloud Sleuth → auto adds Trace ID in Spring apps
OpenTelemetry → modern standard, vendor-agnostic (recommended)

Rakuten connection:
Monolith — distributed tracing not needed
Logs on Elasticsearch → searched by reservationId
If microservices → would need Trace ID across services

The question I ask myself:
"If something goes wrong in production, can I find it in 5 minutes?"
+ Logs → what happened
+ Metrics → when did it start degrading
+ Tracing → which service caused it
  All three together → full observability