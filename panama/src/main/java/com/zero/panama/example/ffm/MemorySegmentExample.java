package com.zero.panama.example.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.StructuredTaskScope;

/**
 * Panama FFM  外部内存访问功能的一部分。
 *
 * {@link MemorySegment} 表示一块内存段，可通过特定的API对这段内存进行读写访问。
 *
 * {@link Arena} 是 {@link MemorySegment} 的生命周期管理器, 可通过本类来分配堆外内存段。
 *   一个 Arena 中分配出多个 MemorySegment, MemorySegment释放后会放回 Arena，当 Arena 关闭时，则会释放它分配的所有 MemorySegment。
 *  注意：Java 的 Arena 并不保证分配出来的多个 {@link MemorySegment} 内存是连续的，它仅强调统一回收这个概念。
 *
 *
 *
 *
 * @author Zero.
 * <p> Created on 2025/6/18 15:57 </p>
 */
public class MemorySegmentExample {
    public static void main(String[] args) throws InterruptedException {
        // 包装一个Java原始数组，获取其内存段
        int[] rwaArray = new int[3];
        MemorySegment rwaArraySgm = MemorySegment.ofArray(rwaArray);
        // 通过内存段来遍历数组元素
        for (int i = 0; i < rwaArraySgm.byteSize() / ValueLayout.JAVA_INT.byteSize(); i++) {
            rwaArraySgm.set(ValueLayout.JAVA_INT, i * ValueLayout.JAVA_INT.byteSize(), (i + 1) * 100);
        }
        for (int i = 0; i < rwaArraySgm.byteSize() / ValueLayout.JAVA_INT.byteSize(); i++) {
            System.out.println(rwaArraySgm.get(ValueLayout.JAVA_INT, i * ValueLayout.JAVA_INT.byteSize()));
        }

        // 操作堆外内存
        try (Arena arena = Arena.ofConfined()){
            // 申请堆外内存存储 Int 类型
            MemorySegment segment = arena.allocate(ValueLayout.JAVA_INT);
            // 赋值
            segment.set(ValueLayout.JAVA_INT, 0, 50);
            // 获取值
            int val = segment.get(ValueLayout.JAVA_INT, 0);
            System.out.println(val);

            // 申请 长度为3的 long[] 数组堆外内存
            MemorySegment arraySegment = arena.allocate(ValueLayout.JAVA_LONG, 3);
            // 根据便宜量 插入元素值
            arraySegment.set(ValueLayout.JAVA_LONG, 0, 100);
            arraySegment.set(ValueLayout.JAVA_LONG, 8, 200);
            arraySegment.set(ValueLayout.JAVA_LONG, 16, 300);
            // 根据索引访问元素值
            System.out.println(arraySegment.getAtIndex(ValueLayout.JAVA_LONG, 0));
            System.out.println(arraySegment.getAtIndex(ValueLayout.JAVA_LONG, 1));
            System.out.println(arraySegment.getAtIndex(ValueLayout.JAVA_LONG, 2));
            // arena 会在 try-with-resource 作用域结束时自动释放.

            // 分配一个堆外的Long，并设置值
            MemorySegment longSegment = arena.allocate(ValueLayout.JAVA_LONG);
            longSegment.set(ValueLayout.JAVA_LONG, 0, 1024);
            System.out.println(longSegment.get(ValueLayout.JAVA_LONG, 0));

            // 分配10个字节，设置字符串值
            MemorySegment seg = arena.allocate(10);
            seg.setString(0, "Hello");
            seg.setString(4, "World");
            String value = seg.getString(0, StandardCharsets.UTF_8);
            System.out.println("string value: " + value);

            // 根据本地内存地址，创建出指针内存段（谨慎使用）
            long address = 0x1232131; // 假的内存地址
            MemorySegment prtSegment = MemorySegment.ofAddress(address);


        }

        // Arena 释放后，其分配出的 MemorySegment 将无法使用，会直接抛出异常
        Arena arena = Arena.ofConfined();
        MemorySegment segment = arena.allocate(ValueLayout.JAVA_INT);
        arena.close();// 手动释放堆外内存

        try {
            segment.set(ValueLayout.JAVA_INT, 0, 1024);
        }catch (Exception e){
            // Exception in thread "main" java.lang.IllegalStateException: Already closed
            System.out.println("unable to use after closing: " + e.getMessage());
        }

        // MARK Confined Arena：只允许一个单线程使用，并且需要手动关闭的 Arena
        Arena localArena = Arena.ofConfined();
        Thread.startVirtualThread(()-> {
            try {
                localArena.allocate(ValueLayout.JAVA_INT);
            }catch (Throwable e) {
                System.out.println("localArena other thread unusable");
            }
        }).join();
        localArena.close();

        // MARK Shared Arena：支持多线程使用的版本，需要手动关闭，如果在关闭的过程中仍有线程在使用其分配出来的 MemorySegment 则会关闭失败
        MemorySegment uaf = null;
        // 建议与结构化并发API一同使用，示例如下
        try(StructuredTaskScope<String> scope = new StructuredTaskScope<>(); Arena sharedArena = Arena.ofShared()){
            // 创建任务1
            StructuredTaskScope.Subtask<String> task1 = scope.fork(() -> {
                MemorySegment _ = sharedArena.allocate(1024);
                return "Hello ";
            });
            StructuredTaskScope.Subtask<String> task2 = scope.fork(() -> {
                MemorySegment _ = sharedArena.allocate(1024);
                return "World!";
            });
            // 等待所有任务结束
            scope.join();
            System.out.println(task1.state());
            System.out.println(task2.state());
            System.out.println(task1.get() + task2.get());

            uaf = sharedArena.allocate(1024);
        }
        try {
            // 抛出异常，因为 arena 管理器已关闭.
            uaf.get(ValueLayout.JAVA_BYTE, 0);
        } catch (Exception e) {
            System.out.println("sharedArena to use after closing: " + e.getMessage());
        }

        // MARK Auto Arena：不允许手动关闭的 Arena。
        // 允许多线程访问，分配从内存段均被自动管理（GC + Cleaner）,当GC发现 Arena 不可达后，
        // 触发一个 Cleaner 回调，然后回收其分配出的所有内存段

        // MARK Global Arena
        // 全局的 Arena，不允许关闭，只管内存的分配，并且这些内存也不会回收（类似于 Rust 的 'static 生命周期）。
        // 分配出来的内存允许跨线程任意访问，随着进程销毁而回收
        Arena globalArena = Arena.global();
        System.out.println(globalArena.allocate(1024).scope());
        System.out.println(MemorySegment.ofAddress(0).scope());

        // MARK Custom Arena
        // 你可以使用java.lang.foreign.SegmentAllocator实现一个将一大块内存拿出来切块分配，最后统一回收的Arena,batch化内存分配操作
        CustomArena customArena = new CustomArena(1024 * 1042);
        MemorySegment _ = customArena.allocate(8);
        customArena.close();
    }

    /// 自定义 一个 Arena
    /// 一次分配一块超大的内存段，然后切成小块来分配，最后统一回收释放。
    static class CustomArena implements Arena {
        final Arena arena = Arena.ofConfined();
        final SegmentAllocator allocator;
        public CustomArena(long totalMemory) {
            this.allocator = SegmentAllocator.slicingAllocator(arena.allocate(totalMemory));
        }
        @Override
        public MemorySegment allocate(long byteSize, long byteAlignment) {
            return allocator.allocate(byteSize, byteAlignment);
        }
        @Override
        public MemorySegment.Scope scope() {
            return arena.scope();
        }
        @Override
        public void close() {
            arena.close();
        }
    }
}
