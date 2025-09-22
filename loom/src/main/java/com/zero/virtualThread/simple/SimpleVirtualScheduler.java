package com.zero.virtualThread.simple;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 自定义虚拟线程调度器. 用于调度{@link SimpleVirtualThread}
 *
 * @author Zero.
 * <p> Created on 2025/7/12 23:39 </p>
 */
public class SimpleVirtualScheduler implements Runnable, AutoCloseable {
    /// 是否处于运行中
    private volatile boolean running = false;
    /// 每个平台线程当前正在运行的虚拟线程.
    public static final ScopedValue<SimpleVirtualThread> CURRENT_VT = ScopedValue.newInstance();


    /// 平台线程池，实际的执行单元
    private final ExecutorService executor;
    /// 虚拟线程队列，存储待执行的虚拟线程
    private final Queue<SimpleVirtualThread> tasks = new ConcurrentLinkedQueue<>();

    public SimpleVirtualScheduler(int threads) {
        executor = Executors.newFixedThreadPool(threads);
    }

    /**
     * 启动调度器，将可执行的虚拟线程调度至平台线程进行执行
     */
    @Override
    public void run() {
        running = true;
        while (running){
            if (!tasks.isEmpty()){
                // 从任务队列获取任务，调度到平台线程进行执行.
                SimpleVirtualThread task = tasks.poll();
                executor.execute(()-> ScopedValue.where(CURRENT_VT, task).run(task));
            }
        }
    }

    /**
     * 向调度器添加虚拟线程
     * @param task 虚拟线程
     */
    public void schedule(SimpleVirtualThread task){
        if (!running){
            throw new RuntimeException("scheduler already closed");
        }
        tasks.add(task);
    }

    @Override
    public void close() {
        if (!running){
            throw new RuntimeException("scheduler already closed");
        }
        running = false;
        executor.shutdown();
        if (!tasks.isEmpty()){
            System.out.println("unfinished tasks: " + tasks.size());
        }
    }


}
