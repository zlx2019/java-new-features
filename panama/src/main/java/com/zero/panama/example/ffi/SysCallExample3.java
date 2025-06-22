package com.zero.panama.example.ffi;

import java.io.File;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * 通过 Panama 调用本地函数（C语言）
 *
 *
 * @author Zero.
 * <p> Created on 2025/6/21 22:11 </p>
 */
public class SysCallExample3 {
    static {
        // 加载第三方动态库
        System.load(new File("panama/src/main/resources/lib_c.dylib").getAbsolutePath());
    }

    public static void main(String[] args) throws Throwable {
        callCDynLibrary();
    }

    /**
     * 调用C语言生成的动态链接库
     * void say_hello(char* str);
     */
    public static void callCDynLibrary() throws Throwable {
        // 查找动态库的函数地址
        MemorySegment sayHelloSgm = SymbolLookup.loaderLookup().findOrThrow("say_hello");
        // 构建库函数的描述
        FunctionDescriptor sayHelloDesc = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS);
        // 构建函数句柄
        MethodHandle sayHelloHandle = Linker.nativeLinker().downcallHandle(sayHelloSgm, sayHelloDesc);
        try(Arena arena = Arena.ofConfined()){
            MemorySegment str = arena.allocateFrom("world!");
            sayHelloHandle.invokeExact(str);
        }
    }


}
