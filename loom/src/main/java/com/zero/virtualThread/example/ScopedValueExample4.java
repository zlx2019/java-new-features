package com.zero.virtualThread.example;

import java.util.UUID;
import java.util.concurrent.StructuredTaskScope;

/**
 * ScopedValue 继承特性：ScopedValue 支持跨线程共享。这种共享仅限于 在结构化情况下，子线程被启动并在限定范围内终止 由父线程执行的执行周期。
 *
 * @author Zero.
 * <p> Created on 2025/7/1 15:35 </p>
 */
public class ScopedValueExample4 {
    private static final ScopedValue<String> TRACE = ScopedValue.newInstance();
    static void main() {
        String traceId = UUID.randomUUID().toString();
        ScopedValue.where(TRACE, traceId).run(()-> {
            try(var scope = StructuredTaskScope.open()){
                scope.fork(()-> task1());
                scope.fork(()-> task2());
                scope.fork(()-> task3());
                scope.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void task1(){
        System.out.println("Task one trace: " + TRACE.get());
    }
    private static void task2(){
        System.out.println("Task two trace: " + TRACE.get());
    }
    private static void task3(){
        System.out.println("Task three trace: " + TRACE.get());
    }
}

