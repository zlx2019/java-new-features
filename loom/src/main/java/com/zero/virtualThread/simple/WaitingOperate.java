package com.zero.virtualThread.simple;

import jdk.internal.vm.Continuation;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

import static com.zero.virtualThread.simple.SimpleVirtualScheduler.CURRENT_VT;
import static com.zero.virtualThread.simple.SimpleVirtualThread.SCOPE;


/**
 * 模拟阻塞操作
 *
 * @author Zero.
 * <p> Created on 2025/7/13 18:05 </p>
 */
public class WaitingOperate {

    /**
     * 模拟虚拟线程阻塞操作
     * @param eventName 事件名
     * @param duration  阻塞时长
     * @param scheduler 调度器
     */
    public static void preform(String eventName, Duration duration, SimpleVirtualScheduler scheduler) {
        System.out.println("Waiting for " + eventName + ", for " + duration.toString());
        // 获取当前正在运行的虚拟线程
        SimpleVirtualThread virtualThread = CURRENT_VT.get();

        // 注册定时器，阻塞一定的时长后，将虚拟线程再次加入调度器
        var timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                scheduler.schedule(virtualThread);
                timer.cancel();
            }
        }, duration.toMillis());

        // 将虚拟线程暂停运行，这将从平台线程中卸载下来.
        Continuation.yield(SCOPE);
    }
}
