package com.zero.vt.example;

/**
 * @author Zero.
 * <p> Created on 2025/7/3 16:21 </p>
 */
public class VirtualThreadLocalExample {
    private static final ThreadLocal<String> threadLocal = new ThreadLocal<>();
    public static void main(String[] args) throws InterruptedException {
        Thread.ofVirtual().start(()-> {
            // 在虚拟线程中设置 ThreadLocal 数据
            threadLocal.set("simple data");
            System.out.println("阻塞前 - 虚拟线程ID: " + Thread.currentThread().threadId());
            System.out.println("阻塞前 - 平台线程: " + VirtualThreadUtils.getCurrentThreadInfo());
            System.out.println("阻塞前 - ThreadLocal值: " + threadLocal.get());

            try {Thread.sleep(1000);} catch (InterruptedException e) {throw new RuntimeException(e);}

            System.out.println("\n阻塞后 - 虚拟线程ID: " + Thread.currentThread().threadId());
            System.out.println("阻塞后 - 平台线程: " + VirtualThreadUtils.getCurrentThreadInfo());
            System.out.println("阻塞后 - ThreadLocal值: " + threadLocal.get());

        }).join();
    }






    private static String getCurrentPlatformThread() {
        Thread current = Thread.currentThread();
        if (current.isVirtual()) {
            // 虚拟线程，尝试获取底层平台线程信息
            return "平台线程池中的某个线程 (虚拟线程: " + current.threadId() + ")";
        } else {
            return current.getName() + " (ID: " + current.threadId() + ")";
        }
    }
}
