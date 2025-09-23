package com.zero.virtualThread.example;

/**
 * {@link ScopedValue} 作用域值, 一个可以不使用方法参数的情况下并且安全高效共享数据。
 * {@link ThreadLocal} 线程局部变量三大缺陷：
 *  - 可变性：只要能访问到的线程都可以随意更改值。
 *  - 无界生命周期：需要手动 remove, 否则可能会产生数据的污染性。
 *  - 继承成本昂贵：不适用于存储大对象, 当线程量过多时会导致内存占用显著增加。
 *
 * @author Zero.
 * <p> Created on 2025/6/17 18:40 </p>
 */
public class ScopedValueExample {
    private static final ScopedValue<String> USER = ScopedValue.newInstance();
    public static void main(String[] args) {
        final String name = "Zero";

        // 创建新的作用域，并且设置值
        ScopedValue.where(USER, name).run(()-> {
            System.out.println("当前是否可访问: " +  USER.isBound());
            // 在此作用域内，所有线程都可访问 USER
            System.out.println(Thread.currentThread().getName() + ": " + USER.get());

            // 平台线程访问
            try {
                Thread.ofPlatform().name("pt").start(() -> {
                    ScopedValue.where(USER, name).run(()-> {
                        System.out.println(Thread.currentThread().getName() + ": " + USER.get());
                    });
                }).join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // 在虚拟线程访问
            try {
                Thread.ofVirtual().name("vt").start(() -> {
                    ScopedValue.where(USER, name).run(() -> {
                        System.out.println(Thread.currentThread().getName() + ": " + USER.get());
                    });
                }).join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        });
        // 不处于 特定作用域内是不可访问 USER的
        System.out.println(Thread.currentThread().getName() + ": " + USER.orElse(""));
    }
}
