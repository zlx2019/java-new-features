package com.zero.panama.example.ffm;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;

/**
 * 较为复杂的 Layout 处理
 * 如在Rust中有如下结构：
 * ```
 *
 * #[repr(C)]
 * pub struct User {
 *    pub id: i64,
 *    pub age: i64
 * }
 *
 * #[repr(C)]
 * pub struct Grade {
 *   pub id: i64,
 *   pub class_ids: [i64; 3],
 *   pub user: User,
 *   pub class_ids_ptr: *mut i64,
 *   pub user_prt: *mut User
 * }
 *
 * ```
 *
 * 那么如何在Java 中映射此结构，并且操作呢？
 *
 * @author Zero.
 * <p> Created on 2025/6/21 09:48 </p>
 */
public class MemoryLayoutExample2 {
    public static void main(String[] args) {
        // step1: 首先构建出这两个结构体在Java中对应的布局结构
        // User layout
        final MemoryLayout userLayout = MemoryLayout.structLayout(
          ValueLayout.JAVA_LONG.withName("id"),
          ValueLayout.JAVA_LONG.withName("age")
        );
        // Grade layout
        final MemoryLayout layout = MemoryLayout.structLayout(
                ValueLayout.JAVA_LONG.withName("id"),
                // 数组类型
                MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_LONG).withName("class_ids"),
                userLayout.withName("user"),
                // 数组指针类型
                ValueLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(Integer.MAX_VALUE, ValueLayout.JAVA_LONG)).withName("class_ids_ptr"),
                ValueLayout.ADDRESS.withTargetLayout(userLayout).withName("user_ptr")
        );
        // step2: 为整个结构，分配所需内存
        MemorySegment segment = Arena.global().allocate(layout);
        // step3 ================ 接下来为每个字段设置初始值 ================
        // {
        //  id: 37281932112,
        //  class_ids: [100,200,300],
        //  user: {
        //     id: 3215124,
        //     age: 18
        //  },
        //  class_ids_ptr: 0xdwa213abc234,
        //  user_ptr: 0xawdk213asv987dwa
        // }
        // 首先根据字段名，获取其内存偏移量
        long idOffset = layout.byteOffset(MemoryLayout.PathElement.groupElement("id"));
        // 根据偏移量，设置值
        long gradeId = 37281932112L;
        segment.set(ValueLayout.JAVA_LONG, idOffset, gradeId);

        // 为 `class_ids` 字段设置初始值 [100, 200, 300]
        long classIdsOffset = layout.byteOffset(MemoryLayout.PathElement.groupElement("class_ids"));
        for (int i = 0; i < 3; i++) {
            long value = (i + 1) * 100;
            segment.set(ValueLayout.JAVA_LONG, classIdsOffset + i * 8, value);
        }

        // 设置 `user` 嵌套结构
        long userOffset = layout.byteOffset(MemoryLayout.PathElement.groupElement("user"));
        long userIdOffset = userLayout.byteOffset(MemoryLayout.PathElement.groupElement("id"));
        long userAgeOffset = userLayout.byteOffset(MemoryLayout.PathElement.groupElement("age"));
        long userId = 3215124;
        long userAge = 18;
        segment.set(ValueLayout.JAVA_LONG, userOffset + userIdOffset, userId);
        segment.set(ValueLayout.JAVA_LONG, userOffset + userAgeOffset, userAge);

        // 设置 `class_ids_ptr` 指针类型字段
        // 创建 Long [] 布局
        int length = 5;
        MemoryLayout classIdsLayout = MemoryLayout.sequenceLayout(length, ValueLayout.JAVA_LONG);
        // 分配内存，并且赋值
        MemorySegment classIdsSegment = Arena.global().allocate(classIdsLayout);
        for (int i = 0; i < length; i++) {
            long value = (i + 1) * 1000;
            classIdsSegment.setAtIndex(ValueLayout.JAVA_LONG, i, value);
        }
        // 将指针 指向这块内存
        long classIdsPtrOffset = layout.byteOffset(MemoryLayout.PathElement.groupElement("class_ids_ptr"));
        segment.set(ValueLayout.ADDRESS, classIdsPtrOffset, classIdsSegment);


        // 设置 `user_prt` 指针类型
        MemorySegment userSegment = Arena.global().allocate(userLayout);
        userSegment.set(ValueLayout.JAVA_LONG, 0, userId);
        userSegment.set(ValueLayout.JAVA_LONG, 8, userAge);
        long userPtrOffset = layout.byteOffset(MemoryLayout.PathElement.groupElement("user_ptr"));
        segment.set(ValueLayout.ADDRESS, userPtrOffset, userSegment);


        // TODO ================ 读取每个字段的值 ================
        VarHandle idHandle = layout.varHandle(MemoryLayout.PathElement.groupElement("id"));
        long gradeId_x = (long) idHandle.get(segment, 0);
        System.out.println("grade id: " + gradeId_x);

        // TODO 操作数据结构
        VarHandle classIdsTwoElemHandle = layout.varHandle(
                MemoryLayout.PathElement.groupElement("class_ids"), // 找到数组字段
                MemoryLayout.PathElement.sequenceElement(/*index*/ 1) // 根据索引获取元素
        );
        long classIdsTwoElemValue = (long) classIdsTwoElemHandle.get(segment, 0);
        System.out.println("classIds[2]: " + classIdsTwoElemValue);

        // TODO 操作嵌套结构类型
        // 读取 user.id user.age
        VarHandle userIdHandle = layout.varHandle(MemoryLayout.PathElement.groupElement("user"), MemoryLayout.PathElement.groupElement("id"));
        long userId_x = (long) userIdHandle.get(segment, 0);
        VarHandle userAgeHandle = layout.varHandle(MemoryLayout.PathElement.groupElement("user"), MemoryLayout.PathElement.groupElement("age"));
        long userAge_x = (long) userAgeHandle.get(segment, 0);
        System.out.println("user id: " + userId_x);
        System.out.println("user age: " + userAge_x);


        // TODO 通过指针类型，获取指向的数组元素
        // 读取 class_ids_ptr 指向的数组的第五个元素
        VarHandle fiveElemHandle = layout.varHandle(
                MemoryLayout.PathElement.groupElement("class_ids_ptr"), // 找到该字段
                MemoryLayout.PathElement.dereferenceElement(), // 由于字段是指针，所以需要解引用
                MemoryLayout.PathElement.sequenceElement(4) // 根据索引获取元素
        );
        long fiveElemValue = (long) fiveElemHandle.get(segment, 0);
        System.out.println("class_ids_ptr [5]: " + fiveElemValue);

        // TODO 通过指针类型，获取指向结构体字段值
        VarHandle userIdByPtrHandle = layout.varHandle(
                MemoryLayout.PathElement.groupElement("user_ptr"),
                MemoryLayout.PathElement.dereferenceElement(),
                MemoryLayout.PathElement.groupElement("id")
        );
        long userPtrId = (long) userIdByPtrHandle.get(segment, 0);
        System.out.println("user id: " + userPtrId);
    }
}
