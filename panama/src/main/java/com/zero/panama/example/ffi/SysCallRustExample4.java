package com.zero.panama.example.ffi;

import java.io.File;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

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
        System.out.println("call upper result: " + res);
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
     * 调用 Rust 动态库中的 字符串处理函数
     * @param input 参数内容
     */
    public static String callStrToUpper(String input) throws Throwable {
        MemorySegment toUpperSgm = SymbolLookup.loaderLookup().findOrThrow("str_to_upper");
        FunctionDescriptor toUpperDesc = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS);
        MethodHandle toUpperHandle = Linker.nativeLinker().downcallHandle(toUpperSgm, toUpperDesc);
        // 函数参数
        MemorySegment inputSegment = Arena.global().allocateFrom(input);
        // 函数返回值
        MemorySegment resultSegment = (MemorySegment) toUpperHandle.invokeExact(inputSegment);
//        String result = resultSegment.getString(0);
//        System.out.println(result);
        return null;
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
