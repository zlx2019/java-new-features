package com.zero.vt.example;

/**
 * {@link ScopedValue<?>} 使用示例
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
        System.out.println(Thread.currentThread().getName() + ": " + USER.orElse(null));
    }
}
