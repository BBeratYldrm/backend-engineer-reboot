# [7] HBase

## Keywords
hbase · row-key · region · region-server · hmaster · wal · memstore · hfile ·
compaction · hotspot · salting · reversed-timestamp · hdfs · bloom-filter ·
hbase-vs-cassandra

---

## [7.1] Why HBase

MySQL → single server, bottleneck under write-heavy workload
HBase → distributed, horizontal scale, designed for time-series / append-heavy data

+ write-heavy workload
+ strong consistency
+ efficient range scan on sorted row keys
- high operational complexity (HMaster, ZooKeeper, HDFS dependency)
- multi-datacenter replication is hard

---

## [7.2] Row Key Design

Everything in HBase is sorted alphabetically by row key.
Row key must be unique per record.

chat_id alone → not unique (many messages per chat)
chat_id + timestamp → unique but two problems:

Problem 1 — ordering:
Alphabetical sort → oldest message first → must scan entire list for recent messages.
Fix: reversed timestamp
Long.MAX_VALUE - timestamp
Newest message → smallest value → appears first in scan.

Problem 2 — hotspot:
New chats have sequential IDs → all land on same Region → bottleneck.
Fix: salt prefix
salt = hash(chat_id) % N
row key = salt + "_" + chat_id + "_" + reversed_timestamp

Final row key:
2_conv123_9223372036854775805
↑         ↑                  ↑
salt      chat_id            reversed timestamp

+ hotspot avoided
+ recent messages returned first
+ same chat's messages physically co-located

---

## [7.3] Architecture

HMaster      → region assignment, DDL operations
RegionServer → actual read/write handling
ZooKeeper    → cluster coordination, heartbeat
HDFS         → underlying file storage (HFiles stored here)

Region = a row key range slice of a table, hosted on a RegionServer.

---

## [7.4] Write Path

1. Client → RegionServer
2. Write to WAL (disk, sequential → fast, durability guarantee)
3. Write to MemStore (memory, fast)
4. MemStore full → flush to HFile (disk, permanent)

WAL = Write-Ahead Log
→ crash recovery from WAL
→ sequential write → low latency cost

Same pattern as Kafka (disk-based), Cassandra (CommitLog + MemTable + SSTable).

---

## [7.5] Read Path

1. Check MemStore
2. Check HFiles (Bloom filter skips irrelevant files)
3. Merge results → return

Bloom filter → "this key is definitely not in this HFile" → avoids unnecessary disk reads.

---

## [7.6] Compaction

Minor → merges small HFiles, fast, background, tombstones may remain
Major → merges all HFiles, removes tombstones and expired data, slow, schedule off-peak

---

## [7.7] HBase vs Cassandra

HBase:
+ strong consistency
+ efficient range scans on sorted keys
- complex ops (ZooKeeper, HDFS, HMaster dependency)
- multi-DC replication is difficult

Cassandra:
+ masterless, peer-to-peer
+ multi-DC replication built-in
+ simpler operations
- eventual consistency
- no strong transactions

Trade-off in interview:
"Both are valid for messaging. I chose Cassandra in my design for simpler
horizontal scaling and operations. HBase is the stronger choice when strong
consistency and efficient time-series scans are priorities."
