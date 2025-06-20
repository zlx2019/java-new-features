package com.zero.vt.example;

import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * 虚拟线程的核心是通过 {@link Continuation} 实现的，但是续体的作用可不仅仅如此，它的作用很广泛。
 * 本例演示通过 Continuation 实现一个生成器。
 *
 * @author Zero.
 * <p> Created on 2025/6/19 11:15 </p>
 */
public class ContinuationGeneratorExample {
    public static void main(String[] args) {
        // 创建生成器，生成A、B、C
        var generator = new Generator<String>(source -> {
            source.yield("A");
            source.yield("B");
            source.yield("C");

        });
        // 迭代生成元素
        while (generator.hasNext()) {
            System.out.println(generator.next());
        }
    }


    /**
     * 通过 {@link Continuation} 实现的可迭代生成器.
     * @param <T> 元素类型
     */
    public static class Generator<T> implements Iterator<T> {
        private final ContinuationScope scope;
        private final Continuation cont;
        private final Source source;

        public Generator(Consumer<Source> consumer) {
            scope = new ContinuationScope("generator-score");
            source = new Source();
            cont = new Continuation(scope, ()-> consumer.accept(source));
            cont.run();
        }
        @Override
        public boolean hasNext() {
            return !cont.isDone();
        }

        @Override
        public T next() {
            var currentVal = this.source.value;
            cont.run();// 推进获取下一个元素
            return currentVal;
        }

        public class Source {
            private T value;
            public void yield(T value) {
                this.value = value;
                Continuation.yield(scope);
            }
        }
    }
}
