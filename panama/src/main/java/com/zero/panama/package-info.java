/**
 * {@link java.lang.invoke.MethodHandle} 方法句柄是一个比反射更加底层并且JIT对其优化更加良好的产物。
 *  - 类型化：方法句柄是有类型的，意味着它知道自己操作的方法的参数类型和返回值类型。
 *  - 直接可执行：方法句柄可以直接执行，不需要额外的解释或者编译等步骤。
 *  - 低级别操作：方法句柄引用的是底层方法，构造函数、字段类似的低级操作。
 *  - 可选转换：方法句柄支持对参数或返回值进行自定义转换。
 *  - 不可变性：对于方法句柄的转换会产生一个新的句柄。
 *  如果你对 C/C++ 语言比较了解，那么简单来说方法句柄其实就是一个函数指针。
 *
 *
 *  {@link java.lang.invoke.VarHandle} 该类有点类似于 Reflection API中的 Field, 但是它实际上是与 {@link java.lang.invoke.MethodHandle} 一样，
 *  是一个更底层的API，从它的实现上来看，它更贴近与VM的内存操作，从使用上来看它是 Unsafe 类中的内存操作的安全封装。
 *
 *  {@link java.lang.foreign.MemorySegment} 看似与 NIO 包下的 {@link java.nio.ByteBuffer} 差不多，都是支持将 byte[] 和 堆外内存封装为一个对象，
 *  实现在统一视图下的操作.
 *
 */
package com.zero.panama;
