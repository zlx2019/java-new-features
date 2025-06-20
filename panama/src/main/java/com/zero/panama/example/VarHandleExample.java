package com.zero.panama.example;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 *  {@link java.lang.invoke.VarHandle} 该类有点类似于 Reflection API中的 Field, 但是它实际上是与 {@link java.lang.invoke.MethodHandle} 一样，
 *  是一个更底层的API，从它的实现上来看，它更贴近与VM的内存操作，从使用上来看它是 Unsafe 类中的内存操作的安全封装。
 *
 * {@link java.lang.invoke.VarHandle} 变量句柄的使用
 * @author Zero.
 * <p> Created on 2025/6/18 15:36 </p>
 */
public class VarHandleExample {
    private static final String txt = "Hello";
    private static Integer num;

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        // 变量句柄同样也需要通过 Lookup 来获取句柄
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        // 获取静态变量句柄
        VarHandle varHandle = lookup.findStaticVarHandle(VarHandleExample.class, "txt", String.class);
        String str = (String) varHandle.get();
        System.out.println(str);

        // 查找静态变量
        VarHandle numHandle = lookup.findStaticVarHandle(VarHandleExample.class, "num", Integer.class);
        numHandle.set(100);
        System.out.println(num);
        int oldVal = (int) numHandle.getAndSet(300);
        assert 100 == oldVal;
        assert 300 == (int)numHandle.get();
        // 甚至可以设置值时带有 volatile 语义（定义时无需用 volatile 声明）
        numHandle.setVolatile(1001);
    }
}
