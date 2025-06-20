package com.zero.vt.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * WhatIsVirtualThread
 * 虚拟线程的创建
 *
 * @author Zero.
 * <p> Created on 2025/6/17 16:22 </p>
 */
public class VirtualThreadCreateExample {
    public static void main(String[] args) {
        //  TODO 创建方式一
        // 创建虚拟线程
        Thread thread = Thread.ofVirtual().start(() -> {
            System.out.println("Hello World virtual thread.");
        });
        try {
            // 等待虚拟线程结束
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // 创建虚拟线程，不启动
        Thread unstarted = Thread.ofVirtual().name("vt-work-1").unstarted(() -> {});


        // TODO 创建方式二
        // 通过虚拟线程工厂创建
        Thread thread2 = Thread.ofVirtual().factory().newThread(() -> {
            System.out.println("Created through thread factory.");
        });
        // 启动
        thread2.start();
        try {
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }


        // TODO 创建方式三
        // 通过虚拟线程池，会为提交的每个任务都创建一个新的虚拟线程，数量不上限
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        executor.execute(() -> {
            System.out.println("Created through virtual thread pool.");
        });
        executor.shutdown();
    }
}
