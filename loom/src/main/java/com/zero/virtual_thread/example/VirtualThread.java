package com.zero.virtual_thread.example;

import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;

import java.util.concurrent.Executor;

/**
 * 虚拟线程原理:
 *
 * 角色梳理：
 *      - {@link Runnable} 一个待执行的任务。
 *      - {@link Continuation} 是在虚拟线程中对于协程的映射,提供了虚拟线程暂停/继续的能力。
 *      - {@link ContinuationScope}：协程的上下文控制器。
 *      - {@link Executor} Scheduler 调度器。
 *
 *  VirtualThread = Continuation + Scheduler + Runnable
 *
 *  Runnable 是一个具体要执行的任务，会被包装到一个 Continuation 实例中。
 *      - 当任务需要阻塞挂起的时候，会调用 Continuation 的 yield 操作进行阻塞，虚拟线程会从平台线程卸载。
 *      - 当任务解除阻塞继续执行的时候，调用 Continuation.run 会从阻塞点继续执行。
 *  Scheduler 调度器，负责虚拟线程和平台线程的交互。
 *      - 挂载(mount)：将虚拟线程挂载到平台线程，将虚拟线程中的 Continuation 堆栈数据拷贝到平台线程栈，这是一个将堆数据复制到栈的过程。
 *      - 卸载(unmount)：将虚拟线程从平台线程上卸载，此时虚拟线程还未执行完成，所以将虚拟线程中的 Continuation 堆栈数据依然保留在堆内存中。
 * @author Zero.
 * <p> Created on 2025/6/18 14:35 </p>
 */
public class VirtualThread {

}
