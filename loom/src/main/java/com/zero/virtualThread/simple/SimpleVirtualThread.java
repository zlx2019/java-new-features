package com.zero.virtualThread.simple;

import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义虚拟线程,通过{@link jdk.internal.vm.Continuation} 实现简易版虚拟线程.
 *  - 简单来说，虚拟线程本身就是一个续体，它可以被 {@link SimpleVirtualScheduler} 所调度。
 *
 * @author Zero.
 * <p> Created on 2025/6/19 13:52 </p>
 */
public class SimpleVirtualThread implements Runnable {
    private static final AtomicInteger COUNT = new AtomicInteger(1);
    public static final ContinuationScope SCOPE = new ContinuationScope("SimpleVirtualThread");
    private Continuation cont;
    private int id;

    public SimpleVirtualThread(Runnable runnable) {
        cont = new Continuation(SCOPE, runnable);
        id = COUNT.getAndIncrement();
    }

    public void run() {
        System.out.println("SimpleVirtualThread[" + id + "] is running on " + Thread.currentThread());
        cont.run();
    }
}
