# [D-24] System Design — Write-Ahead Log (WAL)

## Keywords
wal · write-ahead-log · crash-recovery · durability · replication ·
commit-log · kafka-internals · postgresql · mysql · acid · fsync

---

## What is WAL?

Before any data is written to disk, the change is first recorded in a log.
If the system crashes mid-write, the log is replayed on restart.
Nothing is lost.

"Write ahead" = log first, apply second. Always.

---

## Why Does It Exist?

Writing to a database is not atomic at the hardware level.
A crash mid-write leaves data in an inconsistent state.

Without WAL:
→ System crashes during write
→ Data is half-written
→ No way to know what happened
→ Corruption

With WAL:
→ Change recorded in log first
→ System crashes
→ On restart, replay the log
→ Consistent state restored

---

## How It Works

1. Transaction starts
2. Change written to WAL (append-only, sequential write → fast)
3. WAL flushed to disk (fsync)
4. Acknowledgment sent to client
5. Data written to actual table (async, later)

If crash happens at step 5 → WAL replayed → data recovered.
If crash happens before step 3 → transaction never committed → no data loss.

Sequential append to log = much faster than random disk writes.
This is why WAL improves write performance, not just safety.

---

## Where It Appears

### PostgreSQL / MySQL
Every write goes to WAL first.
Crash → database replays WAL on startup → consistent state.
This is how ACID durability (the D) is guaranteed.

### Primary-Replica Replication
Primary writes to WAL.
WAL shipped to replicas.
Replicas replay WAL → stay in sync.
No WAL → no replication.

### Kafka
Kafka's commit log is WAL.
Every message appended to disk sequentially.
Consumer offset tracks position in the log.
Broker crash → log replayed → no message lost.
This is why Kafka calls itself a "distributed commit log."

### Outbox Pattern
Outbox table acts as a WAL for microservices.
Event written to outbox before business logic commits.
Relay reads outbox → publishes to Kafka.
Same guarantee: write first, act second.

---

## SOV Interview Angle

Interviewer: "How does your database survive a crash without losing data?"
Answer: WAL. Every change is logged before it's applied.
The log is append-only and sequential — fast to write, easy to replay.

Interviewer: "How does MySQL replication work?"
Answer: Primary writes to binary log (WAL variant).
Replicas stream and replay that log to stay in sync.

Interviewer: "Why is Kafka so fast and durable at the same time?"
Answer: Sequential append to commit log (WAL).
No random writes → fast. Log persisted to disk → durable.

---

## Trade-offs

+ Durability — survives crashes, guarantees D in ACID
+ Enables replication — WAL shipping is how replicas stay in sync
+ Write performance — sequential append faster than random writes
- Disk space — log must be stored and periodically compacted
- Write amplification — every write goes to log AND data file
- fsync cost — flushing to disk on every commit adds latency

---

## One-liner for interviews

"WAL means you write to a log before touching actual data.
If anything crashes, you replay the log. That's how databases
guarantee durability and how replicas stay in sync."