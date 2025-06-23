package com.zero.panama.example.ffi;

import java.io.File;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

/**
 * 通过 Panama 调用本地函数（Rust）
 *
 * @author Zero.
 * <p> Created on 2025/6/22 15:20 </p>
 */
public class SysCallRustExample4 {
    static {
        // 加载动态库
        System.load(new File("panama/src/main/resources/lib_rust_dyn.dylib").getAbsolutePath());
    }

    public static void main(String[] args) throws Throwable {
        int sum = callAdd(100, 200);
        System.out.println("call add result: " + sum);

        String res = callStrToUpper("Hello, world!");
        System.out.println("Java receive: " + res);
    }

    /**
     * 调用 动态中的 add 函数
     */
    public static int callAdd(int a, int b) throws Throwable {
        // 调用简单方法
        MemorySegment addSgm = SymbolLookup.loaderLookup().findOrThrow("add");
        FunctionDescriptor addDesc = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
        MethodHandle addHandle = Linker.nativeLinker().downcallHandle(addSgm, addDesc);
        return  (int) addHandle.invokeExact(a, b);
    }

    /**
     * 调用 Rust 动态库中的 str_to_upper 函数
     * 传入一个字符串，接收一个新的字符串
     * @param input 参数内容
     */
    public static String callStrToUpper(String input) throws Throwable {
        // 查找函数地址
        MemorySegment toUpperSgm = SymbolLookup.loaderLookup().findOrThrow("str_to_upper");
        // 函数描述
        FunctionDescriptor toUpperDesc = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS);
        // 绑定函数
        MethodHandle toUpperHandle = Linker.nativeLinker().downcallHandle(toUpperSgm, toUpperDesc);
        // 构建函数参数（使用堆外内存）
        MemorySegment inputSegment = Arena.global().allocateFrom(input);
        // 调用函数
        MemorySegment resultSegment = (MemorySegment) toUpperHandle.invoke(inputSegment);

        // 返回值是字符串（指针类型），从该地址读取转换为字符串
        String resStr = resultSegment.reinterpret(Integer.MAX_VALUE).getString(0);

        // 释放Rust字符串内存
        freeRustStr(resultSegment);
        return resStr;
    }

    public static void freeRustStr(MemorySegment segment) throws Throwable {
        MemorySegment freeRustStrSegment = SymbolLookup.loaderLookup().findOrThrow("free_str");
        FunctionDescriptor freeRustStrDesc = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS);
        MethodHandle freeHandle = Linker.nativeLinker().downcallHandle(freeRustStrSegment, freeRustStrDesc);
        freeHandle.invokeExact(segment);
    }

    public static void callGradePrint(){
        MemoryLayout user = MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withName("id"), ValueLayout.JAVA_LONG.withName("age"));
        MemoryLayout grade = MemoryLayout.structLayout(
                ValueLayout.JAVA_LONG.withName("id"),
                // 数组类型
                MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_LONG).withName("class_ids"),
                user.withName("user"),
                // 数组指针类型
                ValueLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(Integer.MAX_VALUE, ValueLayout.JAVA_LONG)).withName("class_ids_ptr"),
                ValueLayout.ADDRESS.withTargetLayout(user).withName("user_ptr")
        );
        MemorySegment segment = SymbolLookup.loaderLookup().findOrThrow("grade_print");
        try(Arena arena = Arena.ofConfined()){
            MemorySegment gradeSegment = arena.allocate(grade);
        }

    }
}
