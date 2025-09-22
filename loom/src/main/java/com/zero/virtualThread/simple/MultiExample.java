package com.zero.virtualThread.simple;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

/**
 * 使用自定义的调度器来运行大量虚拟线程
 *  - 通过10个平台线程运行1000个虚拟线程.
 *
 * @author Zero.
 * <p> Created on 2025/7/13 18:24 </p>
 */
public class MultiExample {
    /// 虚拟线程调度器
    public static final SimpleVirtualScheduler SCHEDULER = new SimpleVirtualScheduler(10);

    public static void main(String[] args) throws InterruptedException {
        // 运行调度器
        new Thread(SCHEDULER).start();
        // 创建1w个虚拟线程
        for (int i = 1; i <= 1000; i++) {
            int n = i;
            SCHEDULER.schedule(new SimpleVirtualThread(() -> {
                // 模拟阻塞操作
                WaitingOperate.preform("sleep", Duration.ofMillis(100), SCHEDULER);
                System.out.println("第 " + n + "个 Virtual thread执行...");
            }));
        }

        new CountDownLatch(1).await();
    }
}
