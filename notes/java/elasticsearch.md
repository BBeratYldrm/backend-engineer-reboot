# [3.3] Elasticsearch

## What is it?
Search engine built on inverted index.
Not a replacement for DB — a complement to it.

Inverted index:
"toyota" → documents [1, 3]
"osaka"  → documents [1]
Searching "toyota" → instantly finds docs 1 and 3
No full table scan needed.

## When to use
+ Full-text search (LIKE '%keyword%' in SQL is slow)
+ Log analysis and monitoring (ELK Stack)
+ Autocomplete suggestions
+ Large-scale data analytics

## When NOT to use
- Primary data store — no ACID guarantees
- Transactions — use SQL for that
- Joins — not relational
- Source of truth — always write to DB first, sync to ES

## Typical architecture
Write path:  Client → DB (source of truth) → sync → Elasticsearch
Read path:   Client → Elasticsearch → fast search results

## Real usage at Rakuten
ELK Stack: Filebeat + Logstash + Elasticsearch
→ Filebeat: collects logs from servers
→ Logstash: processes and transforms logs
→ Elasticsearch: stores and indexes logs
→ Kibana: visualizes and searches logs

This is exactly what's on my CV.
When asked "how did you monitor production?" →
"We used ELK stack. Filebeat collected logs,
Logstash processed them, Elasticsearch indexed them,
and we queried through Kibana to debug incidents."

## Key terms
Index    → like a table in SQL
Document → like a row in SQL
Shard    → partition for horizontal scaling
Replica  → copy for high availability

## Interview one-liner
"Elasticsearch is a search engine, not a database.
Use it for full-text search and log analytics.
Always write to DB first — ES is eventually consistent."