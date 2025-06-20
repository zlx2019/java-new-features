package com.zero.vt.example;

import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;

import java.util.concurrent.TimeUnit;

/**
 * {@link Continuation} 译为 '续体'，是Java虚拟线程中幕后的魔法。通过它可以实现任务执行的的暂停和恢复。
 * {@link ContinuationScope} 续体作用域。
 *   续体可以看作为任务执行过程中的状态体现，如任务是否执行完毕、具体执行到了哪一步。
 * 所以我们只需要通过 {@link Continuation} + {@link ContinuationScope} 就可以控制一个任务的运行。
 *
 *  注：续体在JDK内部并未对外开放使用，仅限于JDK内部使用，所以想要使用需要暴露 java.base 包下所有内容：--add-exports java.base/jdk.internal.vm=ALL-UNNAMED
 * Generator
 * @author Zero.
 * <p> Created on 2025/6/17 18:19 </p>
 */
public class ContinuationExample {
    public static void main(String[] args) throws InterruptedException {
        // 创建 Continuation
        Continuation cont = getContinuation();

        // 首次执行 run() 会让续体任务开始执行。直到任务执行完成或续体调用了yield()方法而暂停。
        cont.run();
        TimeUnit.SECONDS.sleep(3);

        // 如果续体任务未完全执行完毕，而是调用了 yield() 暂停了
        // 再次调用 run() 方法会使续体继续从上次暂停的位置继续执行。
        cont.run();
        TimeUnit.SECONDS.sleep(3);

        // 再次继续执行...
        cont.run();
        if (cont.isDone()){
            // 续体已完全执行完毕
            System.out.println("Task completed.");
        }

    }

    // 创建一个续体对象，在执行任务中分别输出 A、B、C 等输出,但是需要由外部控制运行的进度。
    private static Continuation getContinuation() {
        ContinuationScope scope = new ContinuationScope("cont-score");
        // 续体对象，绑定作用域和可执行对象
        Continuation cont = new Continuation(scope, ()-> {
            System.out.println("Task started.");
            System.out.println("A");

            // 当通过续体的作用域，调用 yield 方法后，当前任务会暂停，并将执行器转交至上次调用 run() 方法位置
            Continuation.yield(scope);
            System.out.println("B");

            // 再次暂停...
            Continuation.yield(scope);
            System.out.println("C");
            System.out.println("Task down.");
        });
        return cont;
    }
}
