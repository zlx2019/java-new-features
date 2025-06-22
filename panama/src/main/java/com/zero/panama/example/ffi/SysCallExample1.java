package com.zero.panama.example.ffi;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

/**
 * 简单写几个示例，如何通过 Panama 手动调用本地函数（系统提供的C语言函数）
 *
 * @author Zero.
 * <p> Created on 2025/6/21 11:16 </p>
 */
public class SysCallExample1 {
    public static void main(String[] args) throws Throwable {
        // 首先，获取本地链接器，这是Java方法和本地方法沟通的桥梁
        Linker nativeLinker = Linker.nativeLinker();
        // 从链接器中获取查找器，用于查找本地函数在本地内存中的地址。
        SymbolLookup lookup = nativeLinker.defaultLookup();

        // TODO 调用 getpid 本地函数：获取当前进程ID
        // 获取该本地函数的内存地址
        MemorySegment getPidSegment = lookup.find("getpid").orElseThrow();
        // 创建函数的描述(函数返回值，函数参数列表)
        // int getpid()
        FunctionDescriptor getPidDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT.withByteAlignment(4));
        // 根据函数描述，以及函数的内存地址，创建函数句柄。
        MethodHandle getPidHandle = nativeLinker.downcallHandle(getPidSegment, getPidDescriptor);
        // 执行函数句柄
        int pid = (int) getPidHandle.invokeExact();
        System.out.println("current process id: " + pid);

        // TODO 调用 strlen 本地函数
        // size_t strlen(const char*)
        MemorySegment strlenSegment = lookup.findOrThrow("strlen");
        FunctionDescriptor strlenDesc = FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS);
        MethodHandle strlenHandle = nativeLinker.downcallHandle(strlenSegment, strlenDesc);
        try(Arena arena = Arena.ofConfined()){
            String jStr = "Hello, Panama!";
            MemorySegment cStr = arena.allocateFrom(jStr);
            long length = (long) strlenHandle.invokeExact(cStr);
            System.out.println("length（C）: " + length);
            System.out.println("length（Java）: " + jStr.length());
        }

        // TODO 调用 C语言 printf 函数
        // void printf(const char*);


    }
}
