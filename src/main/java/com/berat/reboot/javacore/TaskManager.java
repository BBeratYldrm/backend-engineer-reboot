package com.berat.reboot.javacore;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TaskManager {

    private final BlockingQueue<Runnable> taskQueue;
    private final Thread[] taskWorkers;
    private volatile boolean running;

    public TaskManager(int threadCount, int queueCapacity) {
        this.taskQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.taskWorkers = new Thread[threadCount];
        this.running = true;
    }

    public void start() {
        for (int i = 0; i < taskWorkers.length; i++) {
            taskWorkers[i] = new Thread(this::workerPool, "Worker-" + i);
            taskWorkers[i].start();
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
                break;
            } catch (Exception e) {
                System.err.println("Task Failed: " + e.getMessage());
            }
        }
    }

    public void shutdown() throws InterruptedException {
        running = false;
        for (Thread thread : taskWorkers) {
            thread.interrupt();
        }
        for (Thread thread : taskWorkers) {
            thread.join();
        }
    }

    public void submitTask(Runnable task) {
        if (!running) return;
        try {
            taskQueue.put(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}