package com.zero.panama.example.ffm;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;

/**
 * {@link MemoryLayout} 描述了一块内存中数据的组织结构，如大小、对齐和字节序等。可以简单理解为 C 语言中的结构体，注意：它需要手动对齐。
 *  主要分为：
 *      - 基本类型布局
 *      - 序列类型布局
 *      - 复合类型布局
 *
 * @author Zero.
 * <p> Created on 2025/6/19 14:03 </p>
 */
public class MemoryLayoutExample {
    public static void main(String[] args) {

        // MARK: 基本类型
        try(Arena arena = Arena.ofConfined()){
            // 以 int 类型为例
            ValueLayout.OfInt layout = ValueLayout.JAVA_INT;
            System.out.println("int layout size: " + layout.byteSize()); // 布局字节长度
            System.out.println("int layout order: " + layout.order()); // 大端 or 小端
            // 根据布局分配出所需内存
            MemorySegment segment = Arena.global().allocate(layout);
            // 根据内存段设置value
            segment.set(layout, 0, 95);
            // 根据 VarHandle + MemorySegment 获取其值
            int value = (int) ValueLayout.JAVA_INT.varHandle().get(segment, 0);
            System.out.println("int layout value: " + value);
        }

        // MARK：数组类型
        try(Arena arena = Arena.ofConfined()){
            // 定义一个 int[10] 类型的布局
            int count = 10;
            SequenceLayout arrayLayout = MemoryLayout.sequenceLayout(count, ValueLayout.JAVA_INT);
            // 分配内存
            MemorySegment arraySegment = arena.allocate(arrayLayout);
            // 写入值
            for (int i = 0; i < count; i++) {
                arraySegment.setAtIndex(ValueLayout.JAVA_INT, i, (i + 1) * 100);
            }
            // 读取值
            for (int i = 0; i < count; i++) {
                int item = (int) arraySegment.get(ValueLayout.JAVA_INT, i * ValueLayout.JAVA_INT.byteSize());
                System.out.println(item);
            }
        }

        // MARK: 复合类型布局
        StructLayout layout = MemoryLayout.structLayout(
                ValueLayout.JAVA_INT,
                MemoryLayout.paddingLayout(4), // 字节对齐填充
                ValueLayout.JAVA_LONG,
                ValueLayout.JAVA_FLOAT,
                MemoryLayout.paddingLayout(4), // 字节对齐填充
                ValueLayout.JAVA_DOUBLE
        );
        // 为结构分配内存
        MemorySegment segment = Arena.global().allocate(layout);
        System.out.println("struct layout size: " + segment.byteSize());
        // 按照结构顺序，向内存中写入值
        segment.set(ValueLayout.JAVA_INT, 0, 123);
        segment.set(ValueLayout.JAVA_LONG, 8, 456789L);
        segment.set(ValueLayout.JAVA_FLOAT, 16, 3.14f);
        segment.set(ValueLayout.JAVA_DOUBLE, 24, 2.2134);
        // 从这段内存中读取不同类型的值
        System.out.println("int: " + segment.get(ValueLayout.JAVA_INT, 0));
        System.out.println("long: " + segment.get(ValueLayout.JAVA_LONG, 8));
        System.out.println("float: " + segment.get(ValueLayout.JAVA_FLOAT, 16));
        System.out.println("double: " + segment.get(ValueLayout.JAVA_DOUBLE, 24));

        // MARK: 联合布局
        // 对 C 比较熟悉的话应该能意识到这个类型的布局是不需要对齐的
        UnionLayout unionLayout = MemoryLayout.unionLayout(
                ValueLayout.JAVA_INT,
                ValueLayout.JAVA_LONG
        );
        System.out.println("union layout size: " + unionLayout.byteSize());
        MemorySegment unionSgm = Arena.global().allocate(unionLayout);
        unionSgm.set(ValueLayout.JAVA_INT, 0, 12345);
        System.out.println("union layout value（int）: " + (int) unionSgm.get(ValueLayout.JAVA_INT, 0));
        System.out.println("union layout value（long）: " + (long) unionSgm.get(ValueLayout.JAVA_LONG, 0));

        // MARK 操作布局中的字段
        // 定义一个简单的复合布局
        StructLayout userLayout = MemoryLayout.structLayout(
                ValueLayout.JAVA_INT.withName("age"),
                MemoryLayout.paddingLayout(4),
                ValueLayout.JAVA_LONG.withName("userId")
        );
        // 为布局分配内存段
        MemorySegment userSegment = Arena.global().allocate(userLayout);
        // 想要操作某个字段，就要先获取其 ValHandle
        // 根据 name 获取字段 handle
        VarHandle ageHandle = userLayout.varHandle(MemoryLayout.PathElement.groupElement("age"));
        // 根据 位置 获取字段 handle
        VarHandle userIdHandle = userLayout.varHandle(MemoryLayout.PathElement.groupElement(2));

        // 通过 handle 设置值 (内存段, offset, value)，offset 通常设置0，因为VarHandle 已经包含了字段在结构体中的偏移信息
        ageHandle.set(userSegment, 0, 18);
        userIdHandle.set(userSegment, 0, 6378219321L);

        // 获取值
        int age =  (int) ageHandle.get(userSegment, 0);
        long userId = (long) userIdHandle.get(userSegment, 0);
        System.out.println("age: " + age);
        System.out.println("userId: " + userId);

        // 也可以获取字段的偏移量
        long offset = userLayout.byteOffset(MemoryLayout.PathElement.groupElement("userId"));
        System.out.println("userId offset: " + offset);



    }
}
