# Day 51 — June 6, 2026

## What I learned

### Concurrent Task Manager — Full Implementation
- Built TaskManager from scratch: BlockingQueue + Thread[] + volatile running
- BlockingQueue vs ArrayList — thread-safe, FIFO, take() blocks when empty (no CPU waste)
- LinkedBlockingQueue vs ArrayBlockingQueue — unbounded vs bounded (back-pressure)
- volatile boolean running — visibility guarantee, without it threads read stale value from CPU cache
- Worker loop — while(running) + take() + task.run()
- Two separate catch blocks — InterruptedException (break) vs Exception (log, continue)
- Why two catches? One bad task must not kill the worker thread
- start() separate from constructor — SRP, constructor only initializes
- workerPool() as private method — SRP, cleaner than lambda inline
- submit() — put() not offer(), blocks when full, task never lost
- Running check in submit() — after shutdown, reject new tasks
- shutdown() — running=false + interrupt() + join()
- Why interrupt()? Worker sleeping in take() must be woken up
- Why join()? Wait for all workers to fully finish before returning
- Two separate loops in shutdown() — first interrupt all, then join all

### Follow-up Questions Covered
- Task throws exception → catch(Exception e), worker must not die
- Wait for all tasks before shutdown → join()
- Return result from task → Callable + FutureTask concept
- Limit queue size → ArrayBlockingQueue(capacity) + constructor parameter
- How many threads optimal → CPU-bound: core count, IO-bound: cores / (1 - blocking fraction)
- Deadlock risk → no explicit locks, BlockingQueue handles sync internally, no circular dependency

### Key Insight
- Kafka analogy — BlockingQueue is the topic, worker threads are consumers
- Same skeleton applies to any concurrent problem: worker pool, message processor, job executor
- Incremental design strategy: write minimal working version first, then improve live

## How I feel
Started the day knowing nothing about BlockingQueue or concurrent patterns.
Ended the day able to write TaskManager from scratch in 10 minutes and explain every line.

## Next
- Morning: rewrite TaskManager from scratch without looking
- Round 1 prep: gRPC basics, messaging API design, SQL schema
- Mock interview practice: explain design decisions out loud in English