package com.zero.panama.example.ffi.rust;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;

/**
 * 通过 Java Panama 包装调用 Rust 动态库
 *
 * @author Zero.
 * <p> Created on 2025/6/24 11:52 </p>
 */
public class RustDynLibraryWrapper {
    /// 堆外内存管理器
    private final Arena arena = Arena.global();
    /// 链接器
    private final Linker linker = Linker.nativeLinker();
    /// 库
    private final SymbolLookup library;


    /// fn say_hello(rwa_str: *const c_char)
    ///
    ///
    private final MethodHandle sayHelloHandle;
    public void sayHello(String str) throws Throwable {
        MemorySegment strSegment = this.arena.allocateFrom(str);
        this.sayHelloHandle.invokeExact(strSegment);
    }

    /// fn ret_hello() -> *mut c_char
    ///
    ///
    private final MethodHandle retHelloHandle;
    public String retHello() throws Throwable {
        MemorySegment retSegment = (MemorySegment) this.retHelloHandle.invokeExact();
        String retValue = retSegment.reinterpret(100).getString(0);
        // 释放 Rust 内存
        this.free(retSegment);
//        System.out.printf("value: %s, address: 0x%x \n", retValue, retSegment.address());
        return retValue;
    }

    /// fn str_to_upper(input: *const c_char) -> *mut c_char
    ///
    ///
    private final MethodHandle strToUpperHandle;
    private String strToUpper(String str) throws Throwable {
        MemorySegment argSegment = this.arena.allocateFrom(str);
        MemorySegment retSegment = (MemorySegment) this.strToUpperHandle.invokeExact(argSegment);
        String retValue = retSegment.reinterpret(Integer.MAX_VALUE).getString(0);
        this.free(retSegment); // 释放内存
        return retValue;
    }



    /// 释放 Rust分配出来的内存
    /// fn free_mem(ptr: *mut c_char)
    private void free(MemorySegment addr) throws Throwable {
        this.freeHandle.invokeExact(addr);
    }
    private final MethodHandle freeHandle;



    /// add 函数句柄
    private final MethodHandle addHandle;

    public RustDynLibraryWrapper (String libPath) {
        // 加载动态库
        library = SymbolLookup.libraryLookup(Path.of(libPath), this.arena);

        // 绑定本地函数的方法句柄
        this.addHandle = bindNativeFunc("add", FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
        this.sayHelloHandle = bindNativeFunc("say_hello", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
        this.retHelloHandle = bindNativeFunc("ret_hello", FunctionDescriptor.of(ValueLayout.ADDRESS));
        this.freeHandle = bindNativeFunc("free_mem", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
        this.strToUpperHandle = bindNativeFunc("str_to_upper", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    }


    public int add(int a, int b) throws Throwable {
        return (int) this.addHandle.invokeExact(a, b);
    }

    // 从动态库中查找函数，并且绑定为描述生成句柄。
    private MethodHandle bindNativeFunc(String name, FunctionDescriptor descriptor) {
        MemorySegment segment = library.find(name)
                .orElseThrow(() -> new RuntimeException("Function not found " + name));
        return this.linker.downcallHandle(segment, descriptor);
    }

    public static void main(String[] args) throws Throwable {
        RustDynLibraryWrapper wrapper = new RustDynLibraryWrapper("panama/src/main/resources/librust.dylib");
        // call say_hello func
        wrapper.sayHello("I am Panama.");

        // call ret_hello func
        String retValue = wrapper.retHello();
        System.out.println("ret_hello: " + retValue);

        // call str_to_upper func
        String upperValue = wrapper.strToUpper("java, rust, golang");
        System.out.println("str_to_upper: " + upperValue);

        // call add func
        int sum = wrapper.add(100, 200);
        System.out.println("a + b = " + sum);
    }
}
