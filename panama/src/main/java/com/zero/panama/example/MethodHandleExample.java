package com.zero.panama.example;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * {@link java.lang.invoke.MethodHandle} 方法句柄是一个比反射更加底层并且JIT对其优化更加良好的产物。
 *  - 类型化：方法句柄是有类型的，意味着它知道自己操作的方法的参数类型和返回值类型。
 *  - 直接可执行：方法句柄可以直接执行，不需要额外的解释或者编译等步骤。
 *  - 低级别操作：方法句柄引用的是底层方法，构造函数、字段类似的低级操作。
 *  - 可选转换：方法句柄支持对参数或返回值进行自定义转换。
 *  - 不可变性：对于方法句柄的转换会产生一个新的句柄。
 *  如果你对 C/C++ 语言比较了解，那么简单来说方法句柄其实就是一个函数指针。
 *
 * @author Zero.
 * <p> Created on 2025/6/18 14:59 </p>
 */
public class MethodHandleExample {
    /// 如何获取并使用方法句柄？
    public static void main(String[] args) throws Throwable {
        // 最简单的方案就是使用 Lookup 来查找现有的 方法句柄
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        // 查找普通方法的句柄（也称为虚方法）
        MethodHandle methodHandle = lookup.findVirtual(Accumulator.class, "add", MethodType.methodType(long.class, int.class, int.class));
        // 执行方法句柄，对于虚方法的执行，需要在参数列表最前面传入一个 实例对象，用于多态的实现.
        Accumulator accumulator = new Accumulator(20);
        long addRes = (long) methodHandle.invokeExact(accumulator, 10, 100);
        System.out.println(addRes);

        // 查找静态方法的句柄
        MethodHandle handle = lookup.findStatic(String.class, "join", MethodType.methodType(String.class, CharSequence.class, CharSequence[].class));
        String joins = (String) handle.invoke("hello, ", "world, ", "panama!");
        System.out.println(joins);
    }


    static class Accumulator {
        private final int c;
        public Accumulator(int c) {
            this.c = c;
        }
         long add(int a, int b) {
            return (long) a + b + c;
        }
    }
}
