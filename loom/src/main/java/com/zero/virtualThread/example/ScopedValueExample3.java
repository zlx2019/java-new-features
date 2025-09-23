package com.zero.virtualThread.example;

import java.util.UUID;

/**
 * @author Zero.
 * <p> Created on 2025/7/1 15:35 </p>
 */
public class ScopedValueExample3 {
    private static final ScopedValue<Long> USER_CTX = ScopedValue.newInstance();
    private static final ScopedValue<String> TRACE_CTX = ScopedValue.newInstance();
    static void main() {
        // 可以链式绑定多个作用域值
        ScopedValue
                .where(USER_CTX, 101L)
                .where(TRACE_CTX, UUID.randomUUID().toString())
                .run(ScopedValueExample3::requestHandle);
    }
    private static void requestHandle(){
        System.out.println("userId: " + USER_CTX.get());
        System.out.println("traceId: " + TRACE_CTX.get());
    }
}
