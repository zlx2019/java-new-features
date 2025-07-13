package com.zero.virtual_thread.simple;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 使用自定义的虚拟线程 + 调度器案例
 * 运行参数 --add-exports java.base/jdk.internal.vm=ALL-UNNAMED
 *
 * @author Zero.
 * <p> Created on 2025/7/12 23:37 </p>
 */
public class SimpleExample {
    /// 虚拟线程调度器
    public static final SimpleVirtualScheduler SCHEDULER = new SimpleVirtualScheduler(3);

    public static void main(String[] args) throws InterruptedException {
        // 创建虚拟线程调度器
        try (var scheduler = SCHEDULER) {
            // 启动调度器
            new Thread(scheduler).start();

            // 向调度器中注册虚拟线程
            scheduler.schedule(new SimpleVirtualThread(()-> {
                System.out.println("VT1：1");
                System.out.println("VT1：2");
                // 模拟网络io, 阻塞2秒
                WaitingOperate.preform("Network", Duration.ofSeconds(2), SCHEDULER);
                System.out.println("VT1：3");
                System.out.println("VT1：4");
            }));
            scheduler.schedule(new SimpleVirtualThread(()-> {
                System.out.println("VT2：1");
                System.out.println("VT2：2");
                // 模拟db操作, 阻塞5秒
                WaitingOperate.preform("DB", Duration.ofSeconds(5), SCHEDULER);
                System.out.println("VT2：3");
                System.out.println("VT2：4");
            }));

            TimeUnit.SECONDS.sleep(10);
        }
    }
}
