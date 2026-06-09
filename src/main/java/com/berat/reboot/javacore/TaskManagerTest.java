package com.berat.reboot.javacore;

import java.util.concurrent.*;

public class TaskManagerTest {

    private final BlockingQueue<Runnable> taskQueue;
    private final Thread[] threads;
    private volatile boolean running;

    public TaskManagerTest(int queueCapacity, int threadCount) {
        taskQueue = new LinkedBlockingQueue<>(queueCapacity);
        threads = new Thread[threadCount];
        running = true;
    }

    public void start() {
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(this::workerPool, "Worker" + i);
            threads[i].start();

        }
    }

    private void workerPool() {
        while (running || !taskQueue.isEmpty()) {
            try {
                Runnable task = taskQueue.poll(500, TimeUnit.MILLISECONDS);
                if (task != null) {
                    task.run();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println("TaskManagerTest: Error starting thread: " + e.getMessage());
            }
        }
    }

    public void stop() throws InterruptedException {
        running = false;

        for (Thread thread : threads) {
            thread.interrupt();
        }

        for (Thread thread : threads) {
            thread.join();
        }
    }

    public void submit(Runnable task) {
        if (!running) return;
        try {
            taskQueue.put(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public <T> Future<T> submit(Callable<T> callable) {
        if (!running) return null;

        FutureTask<T> futureTask = new FutureTask<T>(callable);
        try {
            taskQueue.put(futureTask);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return futureTask;
    }
}
