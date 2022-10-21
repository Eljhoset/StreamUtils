package org.eljhoset.stream.util;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.eljhoset.stream.util.FoldLeft.foldLeft;

interface ScanLeft {
    static <T> Stream<T> scanLeft(Stream<T> stream, Supplier<T> init, Predicate<T> breaker, BinaryOperator<T> op) {
        Supplier<Stream<T>> s = Stream::empty;
        BiPredicate<Stream<T>, T> p = (u, t) -> breaker.test(t);
        FoldLeft.TriFunction<Optional<T>, Stream<T>, T, Stream<T>> f = (prevOpt, u, t) -> Stream.concat(u,Stream.of( op.apply(prevOpt.orElse(init.get()), t)));
        return foldLeft(stream, s, p, f);
    }
    static <T> Stream<T> scanLeft(Stream<T> stream, Supplier<T> init, BinaryOperator<T> op) {
        return scanLeft(stream, init, t -> false, op);
    }
}
