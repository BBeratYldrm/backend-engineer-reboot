package com.berat.reboot.javacore;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Time Complexity:  O(1) — put and take both O(1) with LinkedBlockingQueue
 * Space Complexity: O(n) — queue holds at most capacity elements
 *
 * Producer-Consumer pattern — classic multi-threading problem.
 * Producer generates data, Consumer processes it at a different speed.
 * BlockingQueue handles thread coordination automatically:
 * put() blocks when queue is full, take() blocks when queue is empty.
 * No manual wait/notify needed.
 */
public class ProducerConsumer {

    private static final BlockingQueue<Integer> queue = new LinkedBlockingQueue<>(5);

    static class Producer implements Runnable {
        public void run() {
            for (int i = 1; i <= 10; i++) {
                try {
                    System.out.println("Produced: " + i);
                    queue.put(i);
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    static class Consumer implements Runnable {
        public void run() {
            for (int i = 1; i <= 10; i++) {
                try {
                    int item = queue.take();
                    System.out.println("Consumed: " + item);
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static void main(String[] args) {
        Thread producer = new Thread(new Producer());
        Thread consumer = new Thread(new Consumer());

        producer.start();
        consumer.start();
    }
}