// Generated by jextract

package com.zero.panama.generate.unix;

import java.lang.invoke.*;
import java.lang.foreign.*;
import java.nio.ByteOrder;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.lang.foreign.ValueLayout.*;
import static java.lang.foreign.MemoryLayout.PathElement.*;

/**
 * {@snippet lang=c :
 * struct timeval {
 *     __darwin_time_t tv_sec;
 *     __darwin_suseconds_t tv_usec;
 * }
 * }
 */
public class timeval {

    timeval() {
        // Should not be called directly
    }

    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
        unistd_h.C_LONG.withName("tv_sec"),
        unistd_h.C_INT.withName("tv_usec"),
        MemoryLayout.paddingLayout(4)
    ).withName("timeval");

    /**
     * The layout of this struct
     */
    public static final GroupLayout layout() {
        return $LAYOUT;
    }

    private static final OfLong tv_sec$LAYOUT = (OfLong)$LAYOUT.select(groupElement("tv_sec"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * __darwin_time_t tv_sec
     * }
     */
    public static final OfLong tv_sec$layout() {
        return tv_sec$LAYOUT;
    }

    private static final long tv_sec$OFFSET = 0;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * __darwin_time_t tv_sec
     * }
     */
    public static final long tv_sec$offset() {
        return tv_sec$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * __darwin_time_t tv_sec
     * }
     */
    public static long tv_sec(MemorySegment struct) {
        return struct.get(tv_sec$LAYOUT, tv_sec$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * __darwin_time_t tv_sec
     * }
     */
    public static void tv_sec(MemorySegment struct, long fieldValue) {
        struct.set(tv_sec$LAYOUT, tv_sec$OFFSET, fieldValue);
    }

    private static final OfInt tv_usec$LAYOUT = (OfInt)$LAYOUT.select(groupElement("tv_usec"));

    /**
     * Layout for field:
     * {@snippet lang=c :
     * __darwin_suseconds_t tv_usec
     * }
     */
    public static final OfInt tv_usec$layout() {
        return tv_usec$LAYOUT;
    }

    private static final long tv_usec$OFFSET = 8;

    /**
     * Offset for field:
     * {@snippet lang=c :
     * __darwin_suseconds_t tv_usec
     * }
     */
    public static final long tv_usec$offset() {
        return tv_usec$OFFSET;
    }

    /**
     * Getter for field:
     * {@snippet lang=c :
     * __darwin_suseconds_t tv_usec
     * }
     */
    public static int tv_usec(MemorySegment struct) {
        return struct.get(tv_usec$LAYOUT, tv_usec$OFFSET);
    }

    /**
     * Setter for field:
     * {@snippet lang=c :
     * __darwin_suseconds_t tv_usec
     * }
     */
    public static void tv_usec(MemorySegment struct, int fieldValue) {
        struct.set(tv_usec$LAYOUT, tv_usec$OFFSET, fieldValue);
    }

    /**
     * Obtains a slice of {@code arrayParam} which selects the array element at {@code index}.
     * The returned segment has address {@code arrayParam.address() + index * layout().byteSize()}
     */
    public static MemorySegment asSlice(MemorySegment array, long index) {
        return array.asSlice(layout().byteSize() * index);
    }

    /**
     * The size (in bytes) of this struct
     */
    public static long sizeof() { return layout().byteSize(); }

    /**
     * Allocate a segment of size {@code layout().byteSize()} using {@code allocator}
     */
    public static MemorySegment allocate(SegmentAllocator allocator) {
        return allocator.allocate(layout());
    }

    /**
     * Allocate an array of size {@code elementCount} using {@code allocator}.
     * The returned segment has size {@code elementCount * layout().byteSize()}.
     */
    public static MemorySegment allocateArray(long elementCount, SegmentAllocator allocator) {
        return allocator.allocate(MemoryLayout.sequenceLayout(elementCount, layout()));
    }

    /**
     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction} (if any).
     * The returned segment has size {@code layout().byteSize()}
     */
    public static MemorySegment reinterpret(MemorySegment addr, Arena arena, Consumer<MemorySegment> cleanup) {
        return reinterpret(addr, 1, arena, cleanup);
    }

    /**
     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction} (if any).
     * The returned segment has size {@code elementCount * layout().byteSize()}
     */
    public static MemorySegment reinterpret(MemorySegment addr, long elementCount, Arena arena, Consumer<MemorySegment> cleanup) {
        return addr.reinterpret(layout().byteSize() * elementCount, arena, cleanup);
    }
}

