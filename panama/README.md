# What is Panama?
- JEP

Panama 是一个旨在简化 Java 与本地代码（C/C++）交互的项目。尤其是在高性能和原生库的应用场景下，例如图形处理、科学计算和游戏开发等领域。传统的 Java Native Interface (JNI) 有许多缺点，例如复杂性高、错误处理不够友好等，而 Panama 的目标是简化这一过程。


## 概念
- MethodHandle：类似于 Reflection API 中的 `Method`, 但它比后者更底层同时JIT对其优化更加良好。
- ValHandle：类似于 Reflection API 的 `Field`，但它的实现更贴近于VM相关的内存操作，不仅限于属性的操作。
- MemorySegment：主要用于在Java中堆外内存的管理。