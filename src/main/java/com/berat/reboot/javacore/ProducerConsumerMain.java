package com.berat.reboot.javacore;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ProducerConsumerMain {
    public static void main(String[] args) {
        BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();

        Thread producer = new Thread(new Producer(queue));
        Thread consumer1 = new Thread(new  Consumer(queue));
        Thread consumer2 = new Thread(new  Consumer(queue));

        consumer1.start();
        consumer2.start();
        producer.start();
    }
}
