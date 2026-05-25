# Day 39 — May 25, 2026

## What I learned

### Java Interview Prep — Continued
- Process vs Thread — process is running application, thread is unit of execution within process
- ExecutorService — thread pool manager, reuses threads instead of creating new ones
- CountDownLatch — wait for multiple threads to finish before continuing
- ReentrantLock — more powerful than synchronized, tryLock prevents deadlock
- wait() and notify() — low-level thread coordination, always inside synchronized block
- Runnable vs Callable — Runnable returns void, Callable returns result via Future
- Reflection — inspect and modify class structure at runtime, how Spring DI works internally
- Unsafe API — direct memory access, bypasses JVM safety, used by Netty and Kafka internally

### Java Collections
- HashMap vs LinkedHashMap vs TreeMap — unordered vs insertion order vs sorted
- ArrayList vs LinkedList — index access vs node-based, ArrayList almost always preferred

### Java Fundamentals
- @Component vs @Service vs @Repository vs @Controller — all @Component specializations
- == vs equals() — reference vs value comparison, String pool edge case
- Checked vs unchecked exceptions — compiler enforced vs runtime, connection to @Transactional rollback behavior

### Real World — Session, Cookie, Proxy
- Cookie — small data stored in browser, carries session ID
- Session — user state stored on server, identified by cookie
- Proxy — intermediary between client and server, can drop or modify cookies
- ACL — access control list, whitelist for network access
- Sticky session — load balancer sends same user to same server always
- Root cause of Gatling STG issue — OAuth2 callback URL mismatch, proxy cookie handling

### Restaurant Analogy — Multithreading
- Restaurant = Process, Chef/Waiter = Thread, Order = Task
- ExecutorService = Manager, Kitchen = Shared Memory
- synchronized = Door lock, BlockingQueue = Waiting list
- CountDownLatch = Wait for all dishes before serving
- volatile = Menu board everyone sees latest version
- Deadlock = Two waiters each waiting for the other's tray

## How I feel
Long productive day. A lot of ground covered across Java internals,
collections, and real-world system concepts. The Gatling story connected
many concepts together — session, cookie, proxy, OAuth2.
Pattern recognition is getting faster across topics.

## Next
- Java interview prep continues
- Two Pointer algorithm practice
- Behavioral prep when interview date confirmed