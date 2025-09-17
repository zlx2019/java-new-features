package com.zero.vt.example;

public class VirtualThreadUtils {
    
    public static class ThreadInfo {
        private final String name;
        private final long id;
        private final boolean isVirtual;
        private final String carrierInfo;
        
        public ThreadInfo(String name, long id, boolean isVirtual, String carrierInfo) {
            this.name = name;
            this.id = id;
            this.isVirtual = isVirtual;
            this.carrierInfo = carrierInfo;
        }
        
        // getter 方法...
        
        @Override
        public String toString() {
            return String.format("ThreadInfo{name='%s', id=%d, isVirtual=%s, carrier='%s'}", 
                name, id, isVirtual, carrierInfo);
        }
    }
    
    public static ThreadInfo getCurrentThreadInfo() {
        Thread current = Thread.currentThread();
        String carrierInfo = extractCarrierInfo(current);
        
        return new ThreadInfo(
            current.getName(),
            current.threadId(),
            current.isVirtual(),
            carrierInfo
        );
    }
    
    private static String extractCarrierInfo(Thread thread) {
        if (!thread.isVirtual()) {
            return "N/A (平台线程)";
        }
        
        // 从toString中提取信息
        String threadStr = thread.toString();
        // 解析载体线程信息
        if (threadStr.contains("@")) {
            return threadStr.substring(threadStr.indexOf("@"));
        }
        
        return "未知";
    }
}