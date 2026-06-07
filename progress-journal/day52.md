# Day 52 — June 7, 2026

## What I learned

### gRPC — From Zero to Interview-Ready

- gRPC is a contract-based RPC framework using Protobuf (binary) over HTTP/2
- REST comparison: JSON (text) vs Protobuf (binary), HTTP/1.1 vs HTTP/2,
  no type safety vs strongly typed contract
- Proto file lives in src/main/proto/, build generates Java stubs automatically
- Field numbers (= 1, = 2) are not values — they are binary encoding identifiers
- Four streaming types: Unary, Server Streaming, Client Streaming, Bidirectional
- service = service definition, rpc = method, message = data structure, stream = push

### Reading a Proto File

- Unary rpc → one request, one response (sendMessage, markAsRead)
- Server streaming rpc → client subscribes once, server pushes continuously (Subscribe)
- stream keyword on returns side = server-side push, replaces WebSocket transport layer
- client_message_id in proto → idempotency key, must recognize this immediately

### gRPC in the Messaging System Architecture

- Client → gRPC stream (Subscribe) → API Gateway → gRPC → Message Service
- External: gRPC from client. Internal: gRPC between services.
- L7 load balancer required for gRPC (Envoy) — HTTP/2 framing must be understood
- L4 is sufficient for WebSocket but NOT for gRPC — important distinction
- Presence value in Redis: grpcServerId (not wsServerId, not "online")
- Delivery Service reads Redis → gets grpc-server-3 → publishes to Redis Streams
- gRPC Server subscribes to Redis Streams → receives → pushes down open stream
- This pattern = loose coupling, same as Kafka producer-consumer

### Key Trade-offs Practiced

- Cassandra vs HBase: masterless vs strong consistency, simple ops vs complex ops
- at-least-once + idempotency key vs exactly-once: cheaper, same result
- Fan-out on write vs read: small groups write, large groups read, hybrid
- Token Bucket vs Leaky Bucket: burst allowed vs strict uniform rate
- L4 vs L7 for gRPC: L7 required, Envoy preferred

### Mock Interview — Full System Design

- Ran full Round 1 mock: given proto file + SQL schema, designed complete system
- Started from QPS and scale clarification
- Identified stream in proto → switched to gRPC-based delivery correctly
- Redis presence value → caught own mistake, added grpcServerId
- Hot partition → proposed Decaton (LINE-specific, concurrent within single partition)
- Outbox + Debezium CDC → preferred over polling for memory efficiency
- Sync Service → Cassandra query with CLUSTERING ORDER BY message_id DESC
- Multi-device → per-device presence key, iterate all devices on delivery

### Gaps to Remember

- Client ACK: server pushes via stream, client must ACK separately
  → without ACK, can't distinguish delivered from lost
- Sequence number: seq_id via Redis INCR per chat
  → client detects gaps (received 1,2,4 → missing 3 → trigger sync)
- 2-week retention: device offline > 2 weeks → full re-sync required
- Thundering herd on reconnect → exponential backoff + jitter + Redis cache absorbs load

## How I feel
Started the day having never used gRPC. Ended the day reading proto files,
explaining streaming types, and running a full mock interview.
The system design is solid. The gaps are known and small.

## Next
- Tomorrow morning: review gaps — client ACK, seq_id, multi-device
- Review SQL schema reading for Round 1
- Round 1: Monday 16:00
- Round 2: Wednesday 16:00