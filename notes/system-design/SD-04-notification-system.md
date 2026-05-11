# [SD-04] System Design — Notification System

## Keywords
notification-system · push-notification · apns · fcm · kafka · workers ·
scheduled · cron-job · rate-limiting · authentication · retry ·
event-driven · template · notification-log · analytics · fan-out

---

## Two Types of Notifications

Event-driven:
Triggered by user action or system event.
Order placed → "Your order has been received"
Delivery shipped → "Your package is on the way"

Scheduled:
Triggered by time, not user action.
Marketing campaign → "20% off today only!" → sent at 9am to 1M users
No relation to any user activity.

Both types go through the same pipeline — only the trigger differs.

---

## High-Level Architecture

Event-driven trigger:
Service 1 / Service 2 / Service 3 (any backend service)

Scheduled trigger:
Cron Job (Spring Scheduler or similar)
→ fires at configured time
→ enters same pipeline as event-driven

Flow:
Services / Cron Job
↓
Notification Server (Auth + Rate Limiting)
↓
Kafka (Event Queue)
↓
Workers (Consumers)
↓
APNs (iOS) / FCM (Android) / SMS / Email

---

## Notification Server

Entry point for all notification requests.

Authentication:
Not every service should be allowed to send notifications.
Verify caller identity before accepting the request.

Rate Limiting:
Prevent a buggy service from accidentally spamming 1M users.
Per-service rate limit enforced here.

Redis Cache + DB:
User device tokens, notification preferences, service settings.
"This user disabled marketing notifications" → check here, skip if disabled.
Redis for fast lookup, DB as source of truth.

---

## Kafka — The Backbone

Why Kafka and not ActiveMQ?
ActiveMQ → memory-based, limited scale
Kafka → disk-based, horizontal scale, replay on failure

Topic: "notifications"
Partition key: userId

Why userId and not chatId?
Ordering not critical for notifications.
userId distributes load evenly across partitions.

Partition count:
Base: requests per second / 10MB per partition
Peak time matters more than average:
1M notifications at 9am → ~1000/sec peak
→ 100 partitions, 100 workers recommended

---

## Workers

Kafka consumers — scale horizontally.
Each worker:
1. Reads notification event from Kafka
2. Fetches message template
3. Sends via correct channel (APNs / FCM / SMS / Email)
4. Logs result

Online/offline check NOT needed here.
APNs and FCM handle this automatically:
→ App open → in-app notification
→ App closed → push notification
This is the key difference from chat system.

---

## Retry Strategy

APNs or FCM may fail temporarily.
Worker retries automatically.

Retry with exponential backoff:
Attempt 1 → immediate
Attempt 2 → wait 1s
Attempt 3 → wait 2s
Attempt 4 → wait 4s
→ All failed → DLQ

Failed notifications → Analytics service tracks as "send pending"

---

## Templates and Logs

Notification template:
Reusable message formats.
"Hello {name}, your order {orderId} has been shipped."
Workers fill in the variables at send time.

Notification log:
Every send attempt recorded.
Sent / failed / retried — full audit trail.
Used for analytics and debugging.

---

## Analytics Service

Listens to Kafka — separate consumer group.
Tracks:
→ Sent: notification delivered successfully
→ Click tracking: user tapped the notification
→ Send pending: failed, waiting for retry

Data feeds back into the system:
→ Which notifications get clicked?
→ Which users never engage?
→ Optimize delivery time and content.

---

## Scheduled vs Event-driven — Architecture Difference

Event-driven:
Immediate trigger → enters Kafka → processed within seconds

Scheduled:
Cron job fires → generates events for all target users → enters Kafka
1M users → 1M events in Kafka → workers process in parallel
Same pipeline, different entry point.

---

## Constraints Change the Design

If interviewer says "no ordering needed" → userId partition key, max parallelism
If interviewer says "ordering critical" → different partition key strategy
If interviewer says "small scale" → simple queue, no Kafka needed
If interviewer says "real-time only, no scheduling" → remove cron job
If interviewer says "high reliability" → increase replication factor, add DLQ

Always ask first:
→ Scale? (daily volume, peak QPS)
→ Delivery guarantee? (at-least-once acceptable?)
→ Channels? (push only, or email/SMS too?)
→ Scheduled notifications needed?

---

## Diagram

Services / Cron Job
↓
Notification Server
[Auth + Rate Limiting]
↓              ↓
Kafka          Redis Cache
(partition:       ↓
userId)         DB
↓         [device tokens
Workers        user settings]
↓
[Templates] [Logs]
↓
APNs → iOS
FCM  → Android
SMS
Email
↑
Analytics Service ← click tracking

---

## Interview Checklist
→ Two types of notifications? → event-driven vs scheduled
→ Why Kafka over ActiveMQ? → scale, disk-based, replay
→ Partition key for notifications? → userId, ordering not critical
→ Online/offline check needed? → no, APNs/FCM handle it automatically
→ How scheduled notifications work? → cron job triggers, same Kafka pipeline
→ Retry strategy? → exponential backoff, DLQ after max attempts
→ Analytics? → separate consumer group, tracks sent/clicked/pending
→ What changes with constraints? → always clarify scale, channels, ordering needs