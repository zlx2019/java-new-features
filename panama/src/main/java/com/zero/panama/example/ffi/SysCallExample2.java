package com.zero.panama.example.ffi;

import static com.zero.panama.generate.unix.unistd_h.getpid;


/**
 * 本章通过 Java jextract 根据指定的头文件，生成已经封装好的ABI映射函数。
 * 生成命令：
 *  jextract --output src/main/java -t com.zero.panama.generate.unix -I /Library/Developer/CommandLineTools/SDKs/MacOSX.sdk/usr/include /Library/Developer/CommandLineTools/SDKs/MacOSX.sdk/usr/include/unistd.h
 *
 * @author Zero.
 * <p> Created on 2025/6/21 21:34 </p>
 */
public class SysCallExample2 {
    public static void main(String[] args) {
        // 调用 jextract 生成的本地方法封装
        int pid = getpid();
        System.out.println("pid = " + pid);
    }
}
