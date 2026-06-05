package com.berat.reboot.javacore;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SimpleTaskExecutor {
    private final BlockingQueue<Runnable> queue;
    private final Thread[] workers;
    private volatile boolean running = true; // Visibility garantisi için şart [8]

    public SimpleTaskExecutor(int threadCount) {
        this.queue = new LinkedBlockingQueue<>(); // Bounded (kapasiteli) seçmek gerçek hayatta back-pressure sağlar [6, 9]
        this.workers = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            workers[i] = new Thread(() -> {
                // Thread ölmesin diye while döngüsü
                // running false olsa bile kuyruk boşalana kadar devam et [User Query]
                while (running || !queue.isEmpty()) {
                    try {
                        Runnable task = queue.take(); // Kuyruk boşsa burada bloklanır [7, 10]
                        task.run();
                    } catch (InterruptedException e) {
                        // shutdown sırasında interrupt edilirse döngüden çıkış için
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        // ÖNEMLİ: Bir task patlarsa worker thread ölmemeli! [User Query]
                        System.err.println("Task failed: " + e.getMessage());
                    }
                }
            });
            workers[i].start();
        }
    }

    public void submit(Runnable task) throws InterruptedException {
        if (running) {
            queue.put(task); // Kuyruk doluysa bloklanır (Back-pressure) [9, 10]
        }
    }

    public void shutdown() {
        running = false;
        for (Thread worker : workers) {
            worker.interrupt(); // take() içinde bekleyen threadleri uyandır [User Query]
        }
    }
}

//public class SimpleTaskExecutor { // Sorumluluk: Sadece task yönetimini koordine eder (SRP) [2]
//    private final BlockingQueue<Runnable> taskQueue;
//    private final Thread[] workerThreads;
//    private volatile boolean isStopped = false; // Thread'ler arası görünürlük için volatile şart [6, 7]
//
//    public SimpleTaskExecutor(int poolSize) {
//        this.taskQueue = new LinkedBlockingQueue<>(); // Bounded (kapasiteli) seçmek sistem güvenliği için iyidir [4]
//        this.workerThreads = new Thread[poolSize];
//
//        for (int i = 0; i < poolSize; i++) {
//            String threadName = "Worker-Thread-" + i;
//            workerThreads[i] = new Thread(this::runWorker, threadName);
//            workerThreads[i].start();
//        }
//    }
//
//    private void runWorker() {
//        // Döngü: Durdurulmadıkça veya kuyrukta iş oldukça devam et
//        while (!isStopped || !taskQueue.isEmpty()) {
//            try {
//                Runnable task = taskQueue.take(); // Kuyruk boşsa thread burada "BLOCK" olur (uyur) [4]
//                task.run(); // İşin kendisi burada çalışır
//            } catch (InterruptedException e) {
//                // Shutdown sırasında take() metodunda bekleyen thread'i uyandırmak için [User Query]
//                Thread.currentThread().interrupt();
//                break;
//            } catch (Exception e) {
//                // ÖNEMLİ: Bir task hata verirse worker ölmemeli, sadece hatayı loglamalıyız [User Query]
//                System.err.println("Task execution failed: " + e.getMessage());
//            }
//        }
//        System.out.println(Thread.currentThread().getName() + " has finished.");
//    }
//
//    public void submit(Runnable task) throws InterruptedException {
//        if (this.isStopped) throw new IllegalStateException("Executor is shut down");
//        this.taskQueue.put(task); // Kuyruk doluysa producer'ı (ana thread'i) burada bekletir (Backpressure) [3, 4]
//    }
//
//    public void shutdown() {
//        this.isStopped = true;
//        for (Thread worker : workerThreads) {
//            worker.interrupt(); // take() metodunda takılı kalan thread'leri dürtüp uyandırır [User Query]
//        }
//    }
//}