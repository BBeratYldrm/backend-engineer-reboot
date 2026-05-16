# Day 31 — May 16, 2026

## What I learned

### gRPC vs REST
- gRPC uses Protobuf over HTTP/2 — faster, binary, strongly typed
- REST uses JSON over HTTP/1.1 — human-readable, universal
- gRPC for internal microservice communication
- REST for public APIs
- Note added: notes/distributed/23-grpc-vs-rest.md

### Kafka Notebook Project
Built a working Spring Boot + Kafka demo project from scratch.
Concepts implemented with code and explanations:

Topic + Producer + Consumer:
- OrderController receives HTTP POST
- OrderProducer publishes to "orders" topic
- NotificationConsumer, PaymentConsumer, InventoryConsumer listen

Consumer Groups — Fan-out Pattern:
- Each service has its own groupId
- Same message delivered to all groups independently
- One event triggers three downstream services

Partition Key — Ordering Guarantee:
- orderId used as partition key
- Same orderId always routes to same partition
- Ordering preserved within partition

Offset:
- Auto-incrementing sequence number per partition
- Kafka stores consumer position in __consumer_offsets
- Consumer resumes exactly where it left off on restart

Retry + Dead Letter Queue:
- @RetryableTopic — automatic retry on failure
- After all retries exhausted — message goes to DLT
- @DltHandler — handles failed messages for manual inspection

Idempotency:
- Same message may arrive twice due to retry
- orderId tracked in processedOrders Set
- Duplicate messages detected and skipped
- Production note: use Redis or DB instead of in-memory Set

Project: github.com/BBeratYldrm/kafka-notebook

### Algorithm — MinSizeSubarraySum (Sliding Window)
- Trigger words: "shortest" + "subarray" → Sliding Window
- left and right pointers both start at 0
- right expands on every iteration → currentSum += nums[right]
- when currentSum >= target → valid window found
- update minLen with Math.min(minLen, right - left + 1)
- shrink from left → currentSum -= nums[left], left++
- use while not if — window may still be valid after shrinking
- if minLen never updated → return 0 (no valid window found)
- edge case handled: return minLen == Integer.MAX_VALUE ? 0 : minLen
- all three test cases passed

## How I feel
Long day — 3.5+ hours at the cafe. Migrated everything to a new chat
without losing context, which took effort but was worth it.
Sliding window clicked today. The currentSum mechanics — adding on the
right, subtracting on the left — finally made sense through working code.
Still need more reps to make pattern recognition automatic, but the
foundation is there. Ready to continue tomorrow.

## Next
- System Design — SD-06 Search Autocomplete
- Algorithm pattern practice — mixed questions
- AWS basics
- Mock interview preparation