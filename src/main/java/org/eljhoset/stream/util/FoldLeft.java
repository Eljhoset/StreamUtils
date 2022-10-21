package org.eljhoset.stream.util;

import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface FoldLeft {

    static <T, U> U foldLeft(Stream<T> stream, Supplier<U> init, BiPredicate<U, T> breaker, final TriFunction<Optional<T>, ? super U, ? super T, ? extends U> op) {
        Spliterator<T> split = stream.spliterator();
        BoxConsumer<T> box = new BoxConsumer<>();
        Optional<T> prev = Optional.empty();
        U u = init.get();
        while (split.tryAdvance(box) && !breaker.test(u, box.value)) {
            u = op.apply(prev, u, box.value);
            prev = Optional.ofNullable(box.value);
        }
        return u;
    }

    static <T, U> U foldLeft(Stream<T> stream, Supplier<U> init, BiPredicate<U, T> breaker, final BiFunction<? super U, ? super T, ? extends U> op) {
        return foldLeft(stream, init, breaker, (prev, u, t) -> op.apply(u, t));
    }

    static <T, U> U foldLeft(Stream<T> stream, Supplier<U> init, final BiFunction<? super U, ? super T, ? extends U> op) {
        return foldLeft(stream, init, (u, t) -> false, op);
    }

    static <T, U> U foldLeft(Stream<T> stream, Supplier<U> init, BiPredicate<U, T> breaker, final BiConsumer<U, T> op) {
        return foldLeft(stream, init, breaker, peek(op));
    }

    static <T, U> U foldLeft(Stream<T> stream, Supplier<U> init, final BiConsumer<U, T> op) {
        return foldLeft(stream, init, (u, t) -> false, op);
    }

    static <T> T foldLeft(Stream<T> stream, Supplier<T> init, Predicate<T> breaker, BinaryOperator<T> op) {
        BiPredicate<T, T> p = (u, t) -> breaker.test(t);
        TriFunction<Optional<T>, T, T, T> f = (prevOpt, u, t) -> op.apply(u, t);
        return foldLeft(stream, init, p, f);
    }

    static <T> T foldLeft(Stream<T> stream, Supplier<T> init, BinaryOperator<T> op) {
        return foldLeft(stream, init, t -> false, op);
    }

    static <T> T foldLeft(Stream<T> stream, Supplier<T> init, Consumer<T> op) {
        BinaryOperator<T> binaryOperator = (t, t2) -> peek(op).apply(t);
        return foldLeft(stream, init, t -> false, binaryOperator);
    }

    static <T, U> Collector<T, ?, U> foldLeft(Supplier<U> init, final BiFunction<? super U, ? super T, ? extends U> op) {
        return Collectors.collectingAndThen(
                Collectors.reducing(Function.<U>identity(), a -> b -> op.apply(b, a), Function::andThen),
                end -> end.apply(init.get())
        );
    }
    static <T, U> Collector<T, ?, U> foldLeft(Supplier<U> init, final BiConsumer<U,T> op) {
        return foldLeft(init,peek(op));
    }

    static <T, U> Optional<U> foldLeftOptionally(Stream<T> stream, Supplier<U> init, BiPredicate<U, T> breaker, final BiFunction<? super U, ? super T, ? extends U> op) {
        BiFunction<Optional<U>, T, Optional<U>> f = (opt, t) -> opt.or(() -> Optional.ofNullable(init.get())).map(u -> op.apply(u, t));
        BiPredicate<Optional<U>, T> p = (opt, t) -> opt.stream().anyMatch(u -> breaker.test(u, t));
        return foldLeft(stream, Optional::empty, p, f);
    }

    static <T, U> Optional<U> foldLeftOptionally(Stream<T> stream, Supplier<U> init, final BiFunction<? super U, ? super T, ? extends U> op) {
        return foldLeftOptionally(stream, init, (u, t) -> false, op);
    }

    static <T, U> Optional<U> foldLeftOptionally(Stream<T> stream, Supplier<U> init, BiPredicate<U, T> breaker, final BiConsumer<U, T> op) {
        return foldLeftOptionally(stream, init, breaker, peek(op));
    }

    static <T, U> Optional<U> foldLeftOptionally(Stream<T> stream, Supplier<U> init, final BiConsumer<U, T> op) {
        return foldLeftOptionally(stream, init, (u, t) -> false, op);
    }
    static <T, U> Collector<T, ?, Optional<U>> foldLeftOptionally(Supplier<U> init, final BiFunction<? super U, ? super T, ? extends U> op) {
       BiFunction<Optional<U>,T,Optional<U>> function = (opt, t) ->opt.or(() -> Optional.ofNullable(init.get()))
               .map(u -> op.apply(u,t));
       return foldLeft(Optional::empty,function);
    }
    static <T, U> Collector<T, ?, Optional<U>> foldLeftOptionally(Supplier<U> init, final BiConsumer<U, T> op) {
        return foldLeftOptionally(init,peek(op));
    }

    static <T> UnaryOperator<T> peek(Consumer<T> consumer) {
        return t -> {
            consumer.accept(t);
            return t;
        };
    }

    static <T, U> BiFunction<T, U, T> peek(BiConsumer<T, U> consumer) {
        return (t, u) -> {
            consumer.accept(t, u);
            return t;
        };
    }

    class BoxConsumer<T> implements Consumer<T> {
        T value = null;

        public void accept(T t) {
            value = t;
        }
    }

    @FunctionalInterface
    interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }
}
