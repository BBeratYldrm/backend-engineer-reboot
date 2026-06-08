package com.berat.reboot.javacore;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Consumer implements Runnable {
    private BlockingQueue<Integer> queue;

    public Consumer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Integer data = queue.poll(500, TimeUnit.MILLISECONDS);
                if (data == null) break; // producer bitti, çık
                System.out.println("Consumed: " + data);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
