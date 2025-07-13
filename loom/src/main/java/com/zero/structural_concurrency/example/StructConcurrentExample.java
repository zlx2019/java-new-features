package com.zero.structural_concurrency.example;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;

/**
 * 结构化并发
 *
 * @author Zero.
 * <p> Created on 2025/6/19 10:52 </p>
 */
public class StructConcurrentExample {
    private static final RandomGenerator random = RandomGenerator.getDefault();

    /// 任务一：获取用户ID
    static long getUserId() throws InterruptedException {
        // 模拟IO阻塞
        TimeUnit.SECONDS.sleep(random.nextInt(1,5));
        return 1001L;
    }

    /// 任务二：获取订单ID
    static long getOrderId() throws InterruptedException {
        // 模拟IO阻塞
        TimeUnit.SECONDS.sleep(random.nextInt(1,5));
        return 2001L;
    }

    /// 案例一：等待所有任务执行完成
    static void waitAllTaskComplete() throws InterruptedException {
        // 创建并发范围
        try(var scope = new StructuredTaskScope<>()){
            // 同时启动多个任务
            var userTask = scope.fork(()-> getUserId());
            var orderTask = scope.fork(()-> getOrderId());
            // 等待所有任务结束
            System.out.println("Waiting for all task to complete...");
            scope.join();
            Long userId = userTask.get();
            Long orderId = orderTask.get();
            System.out.println("userId: " + userId);
            System.out.println("orderId: " + orderId);
        }
    }

    /// 当有任意一个任务失败时，则同时取消其他所有任务.
    /// 适用于所有任务都必须成功的场景
    static void waitTaskCompleteOrAnyTaskFailed() throws InterruptedException, ExecutionException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var userTask = scope.fork(()-> getUserId());
            var orderTask = scope.fork(()-> getOrderId());
            // 等待任务执行，并且传播异常
            scope.join().throwIfFailed();
            System.out.println("Waiting for task to complete...");
            System.out.println("userId: " + userTask.get());
            System.out.println("orderId: " + userTask.get());
        }
    }

    /// 当有任意一个任务完成时，同时取消其他所有任务
    /// 适用于只需要获取第一个完成的任务结果的场景
    static void waitAnyTaskSuccess() throws InterruptedException, ExecutionException {
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<Long>()) {
            var userTask = scope.fork(()-> getUserId());
            var orderTask = scope.fork(()-> getOrderId());
            Long result = scope.join().result();
            System.out.println("Fast task result: " + result);
        }
    }

    ///  使用结构化并发协调多个任务的执行
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // 运行多个任务，等待所有任务完成
        waitAllTaskComplete();

        // 运行多个任务，当有任务失败时，取消其他所有任务
        waitTaskCompleteOrAnyTaskFailed();

        // 运行多个任务，取最快完成的任务结果
        waitAnyTaskSuccess();
    }
}
