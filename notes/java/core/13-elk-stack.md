# [3.3] ELK Stack — Logging Pipeline

## Why ELK

In production, SSH-ing into a server and grepping log files is not acceptable.
Especially when multiple servers are running.

Problems without centralized logging:
- Multiple servers → which server has the log?
- Log files rotate and get deleted
- No search, no filter, no correlation
- Cannot connect logs from different services

ELK solves this:
- All logs in one place
- Full-text search in milliseconds
- Filter by level, service, time range, user
- Correlate logs across services with correlation ID

## Architecture

Spring Boot App (each pod/server)
│
│ writes JSON logs via LogstashEncoder
↓
Log file (.log.json)
│
Filebeat (installed on same server)
│ watches log file, ships new lines
↓
Logstash
│ parses, filters, enriches
↓
Elasticsearch
│ indexes, stores, makes searchable
↓
Kibana
│ search, dashboard, visualization

## Spring Boot — JSON Log Production

Plain text logs are hard to parse. JSON logs are structured — every field
is a key-value pair that Elasticsearch can index and search.

Logback configuration (spring-logback.xml):
Two appenders running simultaneously:

FILE_ALERT → plain text format, human readable, 30 days retention
FILE_JSON  → LogstashEncoder → JSON format, 7 days retention, gzip compressed

LogstashEncoder (net.logstash.logback.encoder) produces:
{
"@timestamp": "2024-03-15T09:23:11.234+09:00",
"level": "INFO",
"logger_name": "com.example.service.ShopSearchService",
"message": "Shop search completed for zipCode: 1234567",
"thread_name": "http-nio-8080-exec-3"
}

Filebeat reads the JSON file → fields already parsed → no extra Logstash parsing needed.

Two log files running simultaneously:
- app.log       → plain text, for quick SSH grep during incidents
- app.log.json  → JSON, for Filebeat → ELK pipeline

## Filebeat — Log Shipper

Lightweight agent installed on the same server as the application.
Job: watch log files, ship new lines to Logstash.
Does not parse or transform — just ships.

filebeat.yml config:
filebeat.inputs:
- type: log
  enabled: true
  paths:
    - /var/log/app/app.log.json
      json.keys_under_root: true

output.logstash:
hosts: ["logstash-host:5044"]

Two key settings:
- paths → which file to watch
- output.logstash.hosts → where to ship (Logstash server address)

After configuring this, logs start appearing in Kibana under that host.

Why Filebeat and not direct write to Logstash?
- Filebeat is lightweight — minimal CPU/memory on app server
- Handles log rotation automatically
- Buffers if Logstash is temporarily down — no log loss
- Application does not need to know about the logging infrastructure

## Logstash — Parser and Enricher

Receives raw logs from Filebeat, processes them, sends to Elasticsearch.

Since LogstashEncoder already produces JSON, Logstash config is minimal:
input {
beats {
port => 5044
}
}

filter {
# JSON already parsed — add enrichment if needed
# add environment tag, mask sensitive fields, etc.
}

output {
elasticsearch {
hosts => ["elasticsearch-host:9200"]
index => "app-logs-%{+YYYY.MM.dd}"
}
}

Daily index: app-logs-2024.03.15
Each day gets its own index → easy to delete old data, manage retention.

When Logstash does heavy lifting (plain text logs):
filter {
grok {
match => { "message" => "%{LOGLEVEL:level}\[%{TIMESTAMP_ISO8601:timestamp}\] %{GREEDYDATA:msg}" }
}
}
Grok parses unstructured text into fields.
With JSON logs → grok not needed → simpler pipeline.

## Elasticsearch — Storage and Search

Stores logs as documents, indexes every field.
Search across millions of log lines in milliseconds.

Index    → like a DB table, one per day
Document → one log line
Field    → each JSON key (@timestamp, level, message...)

Why not just use a database?
- Full-text search → DB is slow, Elasticsearch is fast
- Scale → billions of documents, distributed by design
- Aggregations → "how many ERROR logs per hour?" in real time

## Kibana — Visualization and Search

Where the actual investigation happens.

Discover tab:
- Search logs by keyword, level, time range
- Filter by service, environment
- "Show me all ERROR logs from this service in the last 1 hour"

Real usage:
- Bot detection → same IP making hundreds of requests → visible in Kibana
- Production incident → search by error message, trace the chain
- Performance investigation → slow requests logged, filtered in Kibana

Correlation ID — connecting logs across services:
Each request gets a unique ID at entry point.
ID passed through all service calls in header.
Logged in every service.
In Kibana: filter by correlation ID → see entire request journey across services.

## Structured Logging Best Practices

Always log in JSON — do not concatenate strings:
// WRONG:
log.info("User " + userId + " placed order " + orderId);

// CORRECT:
log.info("Order placed", kv("userId", userId), kv("orderId", orderId));

JSON output:
{ "message": "Order placed", "userId": "123", "orderId": "456" }

Why: string concatenation → unsearchable blob
structured fields → Kibana can filter by userId, orderId independently

Never log sensitive data:
- Passwords, tokens, card numbers → never in logs
- Mask before logging if needed
- Production logs are often accessible to many people

Log levels — use correctly:
ERROR → something broke, needs immediate attention
WARN  → unexpected but recoverable, investigate later
INFO  → normal business events (order placed, user logged in)
DEBUG → development only, never in production (log volume explodes)

## RED Metrics — Observability Connection

ELK covers logs. Full observability needs three signals:

Logs (ELK)                   → what happened, error details, event trail
Metrics (Prometheus/Grafana) → how the system is performing
Traces (Jaeger/Zipkin)       → request journey across services

RED method:
Rate     → requests per second
Errors   → error rate
Duration → response time (p95, p99)

## Interview Answer — "Did you set up ELK? How was it?"

Yes, set it up for a new service in production.

Configured LogstashEncoder in Logback to produce structured JSON logs.
Installed and configured Filebeat on the server — pointed it at the JSON log file,
set the Logstash host as output.
Logs started flowing into Elasticsearch, accessible in Kibana.

Used it daily for incident investigation — bot detection by IP pattern,
production error tracing, slow request analysis.
The structured JSON format made filtering in Kibana much faster
compared to plain text grep.

## Interview Checklist
→ Why ELK? → centralized logging, full-text search, multi-server
→ Filebeat vs Logstash role? → Filebeat ships, Logstash parses and enriches
→ Why JSON logs? → structured fields, Elasticsearch can index and search each field
→ Why not write directly to Logstash? → Filebeat lightweight, handles rotation, buffers
→ Daily index in Elasticsearch? → easy retention management, delete old data
→ Correlation ID? → trace request across services in Kibana
→ Log levels in production? → INFO minimum, DEBUG never in prod
→ ELK vs full observability? → logs only, need metrics (Prometheus) and traces too