# Day 50 — June 5, 2026

## What I learned

### Interview Experience
- Completed a short screening interview for a contract backend position
- Format: brief, culture/experience focused, not deep technical

### Concurrent Task Manager — First Look
- Studied Java's java.util.concurrent package basics
- Reviewed SimpleTaskExecutor implementation:
    - BlockingQueue<Runnable> — LinkedBlockingQueue, take() blocks when empty
    - volatile boolean running — visibility guarantee across threads
    - Worker loop: while (running || !queue.isEmpty())
    - submit() — queue.put(), blocks on full queue (back-pressure)
    - shutdown() — running=false + worker.interrupt() to unblock take()
    - try/catch around task.run() — one bad task must not kill the worker
    - InterruptedException — Thread.currentThread().interrupt() to restore flag

### Key Insight
- Worker thread must NOT die when a single task throws an exception
- shutdown() must handle threads blocked on take() via interrupt()
- volatile is mandatory for running flag — without it, threads may cache stale value

## How I feel
Big day. A lot happened. Starting to focus on what matters most for next week.

## Next
- Full focus starts tomorrow — 3.5 days of intensive prep
- Round 2: concurrent task manager — drill until muscle memory
- Round 1: gRPC basics, messaging API design
- Mock interview Sunday