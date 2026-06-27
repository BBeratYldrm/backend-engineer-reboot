# Ad Click Event Aggregation System Design
// keywords: ad click, kafka, flink, HBase, OLAP, aggregation, stream processing, lambda architecture

## 1. Scope (Step 1)

- 10M active ads
- Peak 10k clicks/second, average ~1k clicks/second
- ~100M clicks/day
- Advertisers query metrics at 1 minute granularity
- No fraud detection (out of scope)
- No ad targeting/serving (out of scope)

**System interface:**
- Input: ad click events from users
- Output: aggregated click metrics for advertisers

## 2. High Level Design (Step 2)

```
User clicks ad
  ↓
LB & API Gateway
  ↓
Click Processor Service
  ↓
Kafka (buffer — absorbs 10k clicks/sec)
  ↓
Flink (stream processor)
  ├── Tumbling window (1 minute)
  ├── Aggregates in memory: ad-A=15000, ad-B=8000
  └── Flushes per window
       ↙              ↘
HBase                BigQuery / OLAP DB
(raw click data)     (aggregated data)
  ↓                      ↓
S3/GCS               Analytics Service
(data lake)              ↓
  ↓               Advertiser Dashboard
Spark (nightly)
  ↓
Reconciliation Worker → fix OLAP if mismatch
```

## 3. Key Components

### Kafka
- Absorbs write spikes — decouples click ingestion from processing
- Partition by AdId — all clicks for same ad go to same partition
- 7 day retention — if Flink goes down, replay from Kafka
- Hot shard problem: popular ads overwhelm one partition
  → Solution: append random suffix to AdId (ad-A:0, ad-A:1, ... ad-A:N)
  → Flink strips suffix before writing to DB
  → Distributes load across partitions

### Flink (Stream Processor)
- Real-time aggregation — processes events as they arrive
- Tumbling window: non-overlapping 1 minute buckets
- Sliding window: overlapping, used for "top N ads in last M minutes"
- Event time vs processing time:
  → Event time = when click actually occurred
  → Processing time = when Flink received it
  → Use event time for accuracy (handles out-of-order events)
- Watermark: tells Flink when it's safe to close a window
- Checkpoint: periodic state snapshot to S3/GCS
  → If Flink crashes, resumes from last checkpoint
  → Combined with Kafka offset = exactly-once processing
- Why Flink over Spark Streaming?
  → Spark Streaming = micro-batch, latency in seconds
  → Flink = true streaming, event-by-event, latency in milliseconds
  → Flink has built-in windowing, watermarks, exactly-once guarantees

### HBase (Raw Data Store)
- Write-heavy storage for raw click events
- LSM Tree (Log-Structured Merge Tree):
  → Write to memory first (MemTable) — very fast
  → When memory full → flush to disk as sorted SSTable
  → Background compaction merges SSTables
  → Sequential writes → much faster than random writes (MySQL)
- WOW detail — Reversed Timestamp trick:
  → HBase rows sorted lexicographically by row key
  → row key = adId + (Long.MAX_VALUE - timestamp)
  → Latest data at the beginning → fast scan

### OLAP DB (BigQuery / Redshift / ClickHouse)
- Read-heavy storage for aggregated metrics
- Columnar storage — stores data column by column
  → Aggregation queries (SUM, COUNT, AVG) extremely fast
- Schema:
  AdId | AdvertiserId | MinuteTimestamp | ClickCount
- Rakuten uses GCP → BigQuery natural choice

### S3 / GCS (Data Lake)
- Raw click events archived continuously
- Kafka Connect S3 Sink Connector → automatic, no extra load on Flink
- Source of truth for reconciliation
- Spark reads from here for batch processing

### Spark (Batch Layer)
- Runs nightly via cron
- Reads raw events from GCS
- Re-aggregates from scratch
- Compares with OLAP DB → fixes discrepancies

## 4. Lambda Architecture (WOW concept)

```
Speed Layer  → Flink → real-time, low latency, minor inaccuracies possible
Batch Layer  → Spark → slower, 100% accurate, corrects speed layer nightly
```

- Advertisers see near real-time data
- Accuracy guaranteed by next morning reconciliation

## 5. Idempotency — Duplicate Click Prevention (WOW detail)

**Problem:** same click sent twice (network retry, user double-click)

**Solution — Impression ID + HMAC:**
- Ad Placement Service generates unique impression ID per ad shown
- Signed with HMAC secret key → prevents forgery
- Click Processor:
    1. Verify HMAC signature
    2. Check Redis cache: impression ID already seen?
    3. Yes → duplicate, ignore
    4. No → write to Kafka, add to Redis cache (TTL = 24h)

**Why dedup before Kafka?**
- Duplicate across window boundary → Flink counts it twice in different windows
- Must dedup at entry point

## 6. Scaling

### Hot Shard Problem

- Popular ad → millions of clicks → one Kafka partition overwhelmed
- Solution: partition key = adId + random suffix (adId:0 ... adId:N)
- Flink strips suffix when writing → counts combined correctly

### Horizontal Scaling
- Click Processor → stateless, scale with load balancer
- Flink → add more task slots per Kafka partition
- OLAP → BigQuery/Snowflake auto-scale

## 7. Fault Tolerance

- Kafka 7 day retention → replay if Flink goes down
- Flink checkpoint → resume from last known state
- Reconciliation → fixes any incorrect OLAP data

## 8. Observability

- Timestamp at each stage → latency tracking
- Kafka consumer lag → if increasing, Flink falling behind
- Grafana: click throughput, latency, error rate
- DLQ for failed events

## 9. R Context

- Stack: Java, Spring Boot, HBase, Kafka, GCP
- HBase → raw click storage (Hadoop ecosystem)
- BigQuery → OLAP layer (GCP native)
- Kafka → event streaming
- Spark → batch reconciliation