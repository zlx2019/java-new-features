package com.zero.structural_concurrency.example;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

/**
 * 结构化并发
 *  Joiner
 *      -
 *      -
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

    /**
     * 总会成功的任务
     */
    static String alwaysSucceedTask() throws InterruptedException {
        TimeUnit.SECONDS.sleep(random.nextInt(1,5));
        return "always succeed";
    }

    /**
     * 总会失败的任务
     */
    static String alwaysFailedTask() throws InterruptedException {
        TimeUnit.SECONDS.sleep(random.nextInt(1,5));
        throw new RuntimeException("always failed");
    }

    /**
     * 可能会失败的任务
     */
    static String mayFailTask() throws InterruptedException {
        int n = random.nextInt(1, 5);
        TimeUnit.SECONDS.sleep(n);
        if (n % 2 == 0) {
            throw new RuntimeException("may fail");
        }
        return "success";
    }

    /// 场景一：等待所有子任务执行完成 (无论是否有子任务失败)
    /// 需要手动根据任务的最终状态进行处理
    static void waitAllTaskComplete() throws InterruptedException {
        // 创建结构化并发作用域
        try(StructuredTaskScope<Long, Void> scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.awaitAll())){
            // 同时启动多个任务
            StructuredTaskScope.Subtask<Long> userTask = scope.fork(() -> getUserId());
            StructuredTaskScope.Subtask<Long> orderTask = scope.fork(() -> getOrderId());
            System.out.println("Waiting for all task to complete...");
            // 等待所有任务结束
            scope.join();
            // 直接获取子任务结果
            Long userId = userTask.get();
            // 根据子任务状态, 手动处理任务失败情况.
            Long orderId = switch (orderTask.state()){
                case FAILED -> {
                    Throwable e = orderTask.exception();
                    System.out.println("执行执行失败: " + e.getMessage());
                    yield -1L;
                }
                case SUCCESS -> orderTask.get();
                case UNAVAILABLE -> -1L;

            };
            System.out.println("userId: " + userId);
            System.out.println("orderId: " + orderId);
        }
    }



    /// 场景二：等待所有子任务都成功完成, 如果有任意一个子任务执行失败或异常, 则取消其他所有子任务(这会中断其他任务线程)。
    /// 所有子任务成功后, 会将结果收集到流中, 可通过 join() 方法获取。
    /// 这适用于所有任务都必须成功的场景
    static void waitAllTaskSuccessOrThrow(){
        System.out.println("============== 等待所有任务执行成功 ==============");
        try(var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.allSuccessfulOrThrow())){
            scope.fork(()-> alwaysSucceedTask());
            scope.fork(()-> mayFailTask());
            try {
                // 等待所有子任务执行完成, 如果有其中任意一个子任务失败或异常, 则取消其他所有子任务（中断其他子任务线程） 并且抛出 StructuredTaskScope.FailedException 异常
                Stream<StructuredTaskScope.Subtask<Object>> futures = scope.join();
                List<Object> result = futures.map(future -> future.get()).toList();
                System.out.println("All task success: " + Arrays.toString(result.toArray()));
            } catch (StructuredTaskScope.FailedException ste){
                System.out.println("有任务执行失败: " + ste.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /// 场景三：等待所有子任务都成功完成, 如果有任意一个子任务执行失败或异常, 则取消其他所有子任务(这会中断其他任务线程)。
    /// 这与场景二完全一致，区别是该方式不会收集任务结果，适用于没有返回值的任务
    /// 适用于所有任务都必须成功的场景
    static void waitTaskCompleteOrAnyTaskFailed()  {
        try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow())){
            StructuredTaskScope.Subtask<String> task1 = scope.fork(() -> alwaysSucceedTask());
            StructuredTaskScope.Subtask<String> task2 = scope.fork(() -> mayFailTask());
            System.out.println("Waiting for all task to complete...");
            try {
                // 等待所有子任务执行完成, 如果有其中任意一个子任务失败或异常, 则取消其他所有子任务（中断其他子任务线程）,并且抛出 StructuredTaskScope.FailedException 异常
                scope.join();
                // 表示所有任务都执行完成
                String task1Result = task1.get();
                String task2Result = task2.get();
                System.out.println("task1: " + task1Result);
                System.out.println("task2: " + task2Result);
            }catch (StructuredTaskScope.FailedException stf){
                System.out.println("其中有一个任务异常了: " + stf.getMessage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /// 场景四：等待任意一个子任务成功, 一旦有一个任务成功，则会取消其他所有任务。
    /// 如果所有的任务全都失败了则会抛出 StructuredTaskScope.FailedException 异常
    /// 适用于仅需要获取最快完成的任务结果
    static void waitCompletedFirstTask() throws InterruptedException, ExecutionException {
        try (var scope = StructuredTaskScope.open(StructuredTaskScope.Joiner.anySuccessfulResultOrThrow())) {
            scope.fork(()-> getUserId());
            scope.fork(()-> getOrderId());
            // 等待任意一个任务完成
            Object result = scope.join();
            System.out.println("The fastest task to complete: " + result);
        }

//        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<Long>()) {
//            var userTask = scope.fork(()-> getUserId());
//            var orderTask = scope.fork(()-> getOrderId());
//            Long result = scope.join().result();
//            System.out.println("Fast task result: " + result);
//        }
    }

    ///  使用结构化并发协调多个任务的执行
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // 运行多个任务，等待所有任务完成
        // waitAllTaskComplete();

        // 执行多个任务, 等待所有任务成功完成，并收集任务结果
        // waitAllTaskSuccessOrThrow();

        // 运行多个任务，当有任务失败时，取消其他所有任务
        // waitTaskCompleteOrAnyTaskFailed();

        // 运行多个任务，取最快完成的任务结果
        waitCompletedFirstTask();
    }
}
