# What is Panama?
- JEP

Panama 是一个旨在简化 Java 与本地代码（C/C++）交互的项目。尤其是在高性能和原生库的应用场景下，例如图形处理、科学计算和游戏开发等领域。传统的 Java Native Interface (JNI) 有许多缺点，例如复杂性高、错误处理不够友好等，而 Panama 的目标是简化这一过程。

Panama两大特性：
- FFM(Foreign Function Memory)：本地内存操作。
- FFI(Foreign Function Interface)：本地函数操作，即调用当前系统平台的ABI动态库函数。

## FFM 相关概念
### 1. MethodHandle 方法句柄
`java.lang.invoke.MethodHandle` 是一个类似于反射中的 `java.lang.reflect.Method`的产物,
但它比后者更底层同时JIT对其优化更加良好,
其所在的java.lang.invoke是一组强大的代码生成和方法动态调用的工具。


> A method handle is a typed, directly executable reference to an  underlying method, constructor, field, or similar low-level operation, with  optional transformations of arguments or return values

从官方的注释来看，方法句柄具有如下特性：
- 类型化：方法句柄是有类型的，意味着它知道自己操作的方法的参数类型和返回值类型。
- 可执行：方法句柄可以直接执行，不需要额外的解释或者编译等步骤。
- 底层级别操作：方法句柄引用的是底层方法，构造函数、字段类似的低级操作。
- 可转换：方法句柄支持对参数或返回值进行自定义转换。
- 不可变性：对于方法句柄的转换会产生一个新的句柄。

### 2. ValHandle
`java.lang.invoke.VarHandle`也是 Panama 中非常重要的角色，与其对标的是早期反射中的`java.lang.reflect.Field`。 但前者远比后者要强大,
前者可以操作堆外内存的数据，所以从实现来看它更加贴近于VM内存的操作。可以理解为是`Unsafe`类封装的一套更安全的内存操作。

### 3. MemorySegment
`java.lang.foreign.MemorySegment` 内存段，表示一段连续的可访问内存。可以是堆内存，也可以是堆外内存。可通过特定的API对这段内存进行操作。

### 4. MemoryLayout
`java.lang.foreign.MemoryLayout` 内存布局，可以用于描述 `MemorySegment` 内存段的布局内存。如大小、对齐和字节序等。



## FFI 相关概念
### 1. Linker
`java.lang.foreign.Linker` 链接器, 负责将Java方法句柄和本地函数进行绑定，是跨语言调用的桥梁。核心方法：
- `downcallHandle()` 创建从Java调用本地函数的方法句柄。
- `upcallStub()` 创建从本地代码回调Java的存根。
### 2. SymbolLookup
符号查找器，用于查找本地库中的函数或变量地址。

### 3. FunctionDescriptor
`java.lang.foreign.FunctionDescriptor` 函数描述符吗，用于描述本地函数的签名信息。如函数返回值、参数列表类型等元信息。

了解完以上相关知识后，就可以步入重头戏了，也就是 Panama 提供的FFI功能：使用纯Java代码调用符合当前平台的abi。

## Java jextract
[安装](https://jdk.java.net/jextract/)
生成ffi函数命令:
```shell
jextract --output src/main/java -t com.zero.panama.generate.unistd /Library/Developer/CommandLineTools/SDKs/MacOSX.sdk/usr/include/unistd.h
```
`--output` 指定输出目录。
`-t` 指定输出的文件类的package。
