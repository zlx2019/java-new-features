package com.zero.virtualThread.example;

import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * Java 创建虚拟线程的方式.
 *
 * @author Zero.
 * <p> Created on 2025/6/17 16:22 </p>
 */
public class VirtualThreadCreateExample {
    public static void main(String[] args) throws Exception {
        virtualThreadCreate1();
        virtualThreadCreate2();
        virtualThreadCreate3();
        virtualThreadCreate4();
        virtualThreadCreate5();
        virtualThreadCreate6();
        virtualThreadCreate7();
    }


    /// 方式1: 通过 {@link Thread} 创建虚拟线程
    ///   虚拟线程本就属于 {@link Thread} 的子类, 所以可以无缝切换使用.
    public static void virtualThreadCreate1() throws InterruptedException {
        // 创建一个虚拟线程,并直接启动
        Thread thread1 = Thread.ofVirtual().start(() -> {
            System.out.println("Virtual thread (1)");
            sleep(1);
        });
        thread1.join(); // 等待虚拟线程执行完成

        // 更快捷的方式
        Thread thread2 = Thread.startVirtualThread(() -> {
            System.out.println("Virtual thread (2)");
            sleep(1);
        });
        thread2.join();

        // 创建一个虚拟线程, 手动控制
        Thread thread3 = Thread.ofVirtual().unstarted(() -> {
            System.out.println("Virtual thread (3)");
            sleep(1);
        });
        thread3.start(); // 启动虚拟线程
        thread3.join();  // 等待虚拟线程执行完成
    }

    /// 方式2：通过构建器定制化并创建虚拟线程
    public static void virtualThreadCreate2() throws InterruptedException {
        Thread thread = Thread.ofVirtual()
                .name("Virtual thread (4)")
                .uncaughtExceptionHandler((t, e) -> {
                    // 虚拟线程的异常处理器
                    System.out.println("线程 [" + t.getName() + "] 发生了异常: " + e.getMessage());
                })
                .start(() -> {
                    System.out.println("Virtual thread (4)");
                    if (new Random().nextInt(100) % 2 == 0) {
                        throw new RuntimeException("是偶数噢");
                    }
                });
        thread.join();
    }

    /// 方式3: 通过线程工厂创建多个虚拟线程
    public static void virtualThreadCreate3() throws InterruptedException {
        //  创建虚拟线程工厂
        ThreadFactory factory = Thread.ofVirtual()
                .name("virtual-thread-factory-", 0)
                .factory();
        // 通过工厂创建多个虚拟线程
        List<Thread> threads = IntStream.range(0, 5).mapToObj(i -> {
            Thread thread = factory.newThread(() -> {
                sleep(i);
                System.out.println(Thread.currentThread().getName());
            });
            thread.start();
            return thread;
        }).toList();
        for (Thread thread : threads) {
            thread.join();
        }
    }

    /// 方式四：通过线程池创建虚拟线程
    public static void virtualThreadCreate4() {
        // 创建虚拟线程池, 注意：这个线程池没有上限, 只要有内存就会不停的接收新的任务, 直到内存溢出
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 5; i++) {
                int n = i;
                executor.execute(()-> {
                    sleep(n);
                    System.out.println("virtual-thread-pool-" + n);
                });
            }
        }
    }

    /// 方式五：通过 {@link CompletableFuture} 结合虚拟线程来执行任务
    public static void virtualThreadCreate5() {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            sleep(2);
            System.out.println("CompletableFuture virtual thread: " + Thread.currentThread().isVirtual());
        }, executor);
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            sleep(3);
            System.out.println("CompletableFuture virtual thread: " + Thread.currentThread().isVirtual());
            return "success";
        }, executor);
        CompletableFuture.allOf(future1, future2).join();
        executor.shutdown();
    }


    /// 方式六：手动创建一个虚拟线程池
    public static void virtualThreadCreate6() throws InterruptedException {
        // 定义虚拟线程工厂
        ThreadFactory factory = Thread.ofVirtual()
                .name("custom-virtual-thread-pool-", 0)
                .uncaughtExceptionHandler((t, e) -> e.printStackTrace()).factory();
        try (ThreadPoolExecutor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 0L, TimeUnit.SECONDS,
                new SynchronousQueue<>(), factory)){
            executor.execute(()-> {
                System.out.println(Thread.currentThread().getName() + ": " + Thread.currentThread().isVirtual());
            });
        }
    }

    /// 限制一定的数量, 防止虚拟线程过多导致内存溢出.
    public static void virtualThreadCreate7() throws InterruptedException {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Semaphore semaphore = new Semaphore(10);
            for (int i = 0; i < 100; i++) {
                int no = i;
                CompletableFuture.runAsync(()-> {
                    try {
                        semaphore.acquire();
                        sleep(2);
                        System.out.println("Async task " + no + " end");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }finally {
                        semaphore.release();
                    }

                }, executor);
            }
        }
    }


    private static void sleep(long second) {
        try {
            TimeUnit.SECONDS.sleep(second);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
