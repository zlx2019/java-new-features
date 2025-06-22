package com.zero.panama.example.ffm;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;

/**
 * 通过签名对 {@link MemoryLayout} 的使用，你会发现JDK并且没有提供内存的自动对齐，也就是完全依照C语言标准来的，还需要我们手动来对齐。
 *
 * @author Zero.
 * <p> Created on 2025/6/21 10:53 </p>
 */
public class MemoryLayoutExample3 {


    /**
     * 计算并且自动对齐布局
     *
     * @param layoutSequence 布局序列
     * @return 对齐后的布局
     */
    public static StructLayout calcAutoAlignLayout(MemoryLayout ...layoutSequence) {
        long size = 0;
        long align = 1;
        ArrayList<MemoryLayout> layouts = new ArrayList<>();
        for (MemoryLayout memoryLayout : layoutSequence) {
            //当前布局是否与size对齐
            if (size % memoryLayout.byteAlignment() == 0) {
                size = Math.addExact(size, memoryLayout.byteSize());
                align = Math.max(align, memoryLayout.byteAlignment());
                layouts.add(memoryLayout);
                continue;
            }
            long multiple = size / memoryLayout.byteAlignment();
            //计算填充
            long padding = (multiple + 1) * memoryLayout.byteAlignment() - size;
            size = Math.addExact(size, padding);
            //添加填充
            layouts.add(MemoryLayout.paddingLayout(padding));
            //添加当前布局
            layouts.add(memoryLayout);
            size = Math.addExact(size, memoryLayout.byteSize());
            align = Math.max(align, memoryLayout.byteAlignment());
        }
        //尾部对齐
        if (size % align != 0) {
            long multiple = size / align;
            long padding = (multiple + 1) * align - size;
            size = Math.addExact(size, padding);
            layouts.add(MemoryLayout.paddingLayout(padding));
        }
        return MemoryLayout.structLayout(layouts.toArray(MemoryLayout[]::new));
    }

    public static void main(String[] args) {
        StructLayout structLayout = calcAutoAlignLayout(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_FLOAT, ValueLayout.JAVA_DOUBLE);
        System.out.println(structLayout.byteSize());
    }
}
