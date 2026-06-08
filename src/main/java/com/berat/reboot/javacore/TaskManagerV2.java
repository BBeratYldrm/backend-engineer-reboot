package com.berat.reboot.javacore;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TaskManagerV2 {

    // Thread-safe FIFO queue. Bounded capacity = back-pressure.
    // When full, put() blocks the caller instead of losing the task.
    private final BlockingQueue<Runnable> queue;

    // Worker thread pool. Fixed size, no dynamic scaling.
    private final Thread[] workers;

    // volatile = visibility guarantee across threads.
    // Without volatile, threads may read stale value from CPU cache.
    private volatile boolean running;

    public TaskManagerV2(int threadCount, int queueCapacity) {
        this.queue = new LinkedBlockingQueue<>(queueCapacity); // bounded queue
        this.workers = new Thread[threadCount];
        this.running = true;
        // Note: start() is NOT called here. SRP — constructor only initializes.
    }

    // Public: must be called explicitly after construction.
    public void start() {
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Thread(this::workerPool, "Worker-" + i); // named threads for easier debugging
            workers[i].start();
        }
    }

    private void workerPool() {
        // Drain-and-Stop pattern: even after running=false,
        // keep processing until queue is fully empty.
        // Prevents message loss on shutdown.
        while (running || !queue.isEmpty()) {
            try {
                // poll(500ms) instead of take():
                // take() blocks forever — risks getting stuck if interrupt is missed.
                // poll(500ms) returns null after timeout → loop re-checks running flag.
                // Graceful shutdown without relying solely on interrupt().
                Runnable task = queue.poll(500, TimeUnit.MILLISECONDS);

                if (task != null) { // poll returns null on timeout, must null-check
                    task.run();
                }
            } catch (InterruptedException e) {
                // Restore interrupt flag. Do NOT swallow it.
                // Allows the while condition to re-evaluate and exit cleanly.
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                // One bad task must NOT kill the worker thread.
                // Log and continue — worker stays alive for next task.
                System.err.println("Exception in workerPool: " + e.getMessage());
            }
        }
    }

    public void stop() throws InterruptedException {
        running = false; // signal workers to stop after draining

        // Two separate loops — critical design decision:
        // If we interrupt+join in same loop, Worker-0 join blocks
        // before Worker-1 and Worker-2 are interrupted. Slower shutdown.
        // Interrupt all first, then join all = parallel shutdown.
        for (Thread t : workers) {
            t.interrupt(); // wake up threads blocked in poll()
        }
        for (Thread t : workers) {
            t.join(); // wait for each worker to fully finish
        }
    }

    public void submitTask(Runnable task) {
        // Reject new tasks after shutdown. Fail-fast, no silent loss.
        if (!running) return;

        try {
            // put() blocks when queue is full — back-pressure.
            // Caller slows down instead of overflowing the system.
            queue.put(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}