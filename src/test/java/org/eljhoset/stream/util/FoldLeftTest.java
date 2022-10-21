package org.eljhoset.stream.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoldLeftTest {

    @Mock
    FoldLeft.TriFunction<Optional<Integer>, String, Integer, String> op;
    @Mock
    BiFunction<String, Integer, String> biFunction;
    @Mock
    BinaryOperator<Integer> binaryOperator;
    @Mock
    BiConsumer<String, Integer> biConsumer;
    @Mock
    BiPredicate<String, Integer> biPredicate;
    @Mock
    Predicate<Integer> predicate;
    @Mock
    Supplier<String> supplier;
    @Mock
    Supplier<Integer> supplierElement;

    @Test
    @DisplayName("Should stop traversing the stream if the condition in the predicate is met using the element")
    public void foldLeftWithPredicateByElement() {
        Stream<Integer> stream = Stream.of(1, 2, 3, 4);
        when(supplier.get()).thenReturn("");
        when(op.apply(any(), anyString(), anyInt())).thenAnswer(invocation -> concatParams(invocation,1,2));
        when(biPredicate.test(anyString(), anyInt())).thenReturn(false);
        when(biPredicate.test(anyString(), eq(3))).thenReturn(true);

        String result = FoldLeft.foldLeft(stream, supplier, biPredicate, op);

        verify(biPredicate, times(3)).test(anyString(), anyInt());
        verify(op, times(2)).apply(any(), anyString(), anyInt());
        assertEquals("1,2", result.trim());
    }

    @Test
    @DisplayName("Should stop traversing the stream if the condition in the predicate is met using the accumulated")
    public void foldLeftWithPredicateByAccumulated() {
        Stream<Integer> stream = Stream.of(1, 2, 3, 4);
        when(supplier.get()).thenReturn("0");
        when(op.apply(any(), anyString(), anyInt())).thenAnswer(invocationOnMock -> String.valueOf(sumParams(invocationOnMock, 1, 2)));
        when(biPredicate.test(anyString(), anyInt())).thenReturn(false);
        when(biPredicate.test(eq("3"),  anyInt())).thenReturn(true);

        String result = FoldLeft.foldLeft(stream, supplier, biPredicate, op);

        verify(biPredicate, times(3)).test(anyString(), anyInt());
        verify(op, times(2)).apply(any(), anyString(), anyInt());
        assertEquals("3", result);
    }
    @Test
    @DisplayName("Should preserve the previous element")
    public void foldLeftPreviousElement() {
        Stream<Integer> stream = Stream.of(1, 2, 3);
        when(supplier.get()).thenReturn("");
        when(op.apply(any(), anyString(), anyInt())).thenReturn("");
        when(biPredicate.test(anyString(), anyInt())).thenReturn(false);

        FoldLeft.foldLeft(stream, supplier, biPredicate, op);

        verify(op, times(1)).apply(eq(Optional.empty()), anyString(), anyInt());
        verify(op, times(1)).apply(eq(Optional.of(1)), anyString(), anyInt());
        verify(op, times(1)).apply(eq(Optional.of(2)), anyString(), anyInt());
    }
    @Test
    @DisplayName("Should call initial supplier only once")
    public void foldLeftInitialValue() {
        Stream<Integer> stream = Stream.of(1, 2, 3, 4);
        when(supplier.get()).thenReturn("0");
        when(op.apply(any(), anyString(), anyInt())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[1]);
        when(biPredicate.test(anyString(), anyInt())).thenReturn(false);

        String result = FoldLeft.foldLeft(stream, supplier, biPredicate, op);

        verify(supplier, only()).get();
        assertEquals("0", result);
    }
    @Test
    @DisplayName("Should call initial only once using an empty stream")
    public void foldLeftInitialValueEmptyStream() {
        Stream<Integer> stream = Stream.of();
        when(supplier.get()).thenReturn("0");

        String result = FoldLeft.foldLeft(stream, supplier, biPredicate, op);

        verify(supplier, only()).get();
        assertEquals("0", result);
    }
    @Test
    @DisplayName("Should call fold left ignoring the previous value")
    public void foldLeftBiFunction() {
        Stream<Integer> stream = Stream.of(1, 2, 3, 4);
        when(supplier.get()).thenReturn("0");
        when(biFunction.apply(anyString(), anyInt())).thenAnswer(invocationOnMock -> String.valueOf(sumParams(invocationOnMock, 0, 1)));
        when(biPredicate.test(anyString(), anyInt())).thenReturn(false);
        when(biPredicate.test(eq("3"),  anyInt())).thenReturn(true);

        String result = FoldLeft.foldLeft(stream, supplier, biPredicate, biFunction);

        verify(biPredicate, times(3)).test(anyString(), anyInt());
        verify(biFunction, times(2)).apply(anyString(), anyInt());
        assertEquals("3", result);
    }
    @Test
    @DisplayName("Should call fold left using a bi function ignoring the predicate")
    public void foldLeftBiFunctionWithoutPredicate() {
        Stream<Integer> stream = Stream.of(1, 2, 3, 4);
        when(supplier.get()).thenReturn("");
        when(biFunction.apply(anyString(), anyInt())).thenAnswer(invocation -> concatParams(invocation,0,1));

        String result = FoldLeft.foldLeft(stream, supplier, biFunction);
        verify(biFunction, times(4)).apply(anyString(), anyInt());
        assertEquals("1,2,3,4", result);
    }
    @Test
    @DisplayName("Should call fold left using a consumer")
    public void foldLeftConsumer() {
        Stream<Integer> stream = Stream.of(1, 2, 3, 4);
        when(supplier.get()).thenReturn("Initial");
        when(biPredicate.test(anyString(), anyInt())).thenReturn(false);
        when(biPredicate.test(anyString(), eq(3))).thenReturn(true);

        String result = FoldLeft.foldLeft(stream, supplier, biPredicate, biConsumer);
        verify(biConsumer, times(2)).accept(anyString(), anyInt());
        assertEquals("Initial", result);
    }
    @Test
    @DisplayName("Should call fold left using a consumer ignoring predicate")
    public void foldLeftConsumerWithoutPredicate() {
        Stream<Integer> stream = Stream.of(1, 2, 3, 4);
        when(supplier.get()).thenReturn("Initial");

        String result = FoldLeft.foldLeft(stream, supplier, biConsumer);
        verify(biConsumer, times(4)).accept(anyString(), anyInt());
        assertEquals("Initial", result);
    }

    @Test
    @DisplayName("Should stop traversing the stream if the condition in the predicate is met")
    public void foldLeftWithFunction() {
        Stream<Integer> stream = Stream.of(1, 2, 3, 4);
        when(supplierElement.get()).thenReturn(0);
        when(binaryOperator.apply(anyInt(), anyInt())).thenAnswer(invocation -> sumParams(invocation,0,1));
        when(predicate.test(anyInt())).thenReturn(false);
        when(predicate.test(eq(3))).thenReturn(true);

        Integer result = FoldLeft.foldLeft(stream, supplierElement, predicate, binaryOperator);

        verify(predicate, times(3)).test(anyInt());
        verify(binaryOperator, times(2)).apply(anyInt(), anyInt());
        assertEquals(3, result);
    }
    @Test
    @DisplayName("Should traverse the stream ignoring the predicate")
    public void foldLeftWithFunctionIgnoringPredicate() {
        Stream<Integer> stream = Stream.of(1, 2, 3, 4);
        when(supplierElement.get()).thenReturn(0);
        when(binaryOperator.apply(anyInt(), anyInt())).thenAnswer(invocation -> sumParams(invocation,0,1));

        Integer result = FoldLeft.foldLeft(stream, supplierElement,  binaryOperator);

        verify(binaryOperator, times(4)).apply(anyInt(), anyInt());
        assertEquals(10, result);
    }
    @Test
    @DisplayName("Should return a collector")
    public void foldLeftCollector() {
        Stream<Integer> stream = Stream.of(1, 2, 3, 4);
        when(supplierElement.get()).thenReturn(0);
        when(binaryOperator.apply(anyInt(), anyInt())).thenAnswer(invocation -> sumParams(invocation,0,1));

        Integer result = stream.collect(FoldLeft.foldLeft(supplierElement, binaryOperator));

        verify(binaryOperator, times(4)).apply(anyInt(), anyInt());
        assertEquals(10, result);
    }
    @Test
    @DisplayName("Should return a collector using a consumer")
    public void foldLeftCollectorConsumer() {
        Stream<Integer> stream = Stream.of(1, 2, 3, 4);
        when(supplier.get()).thenReturn("0");

        String result = stream.collect(FoldLeft.foldLeft(supplier, biConsumer));

        verify(biConsumer, times(4)).accept(anyString(), anyInt());
        assertEquals("0", result);
    }

    @Test
    @DisplayName("Should return a value wrapped in an optional")
    public void foldLeftOptionally() {
        Stream<Integer> stream = Stream.of(1, 2, 3, 4);
        when(supplier.get()).thenReturn("0");
        when(biFunction.apply(anyString(), anyInt())).thenAnswer(invocationOnMock -> String.valueOf(sumParams(invocationOnMock, 0, 1)));
        when(biPredicate.test(anyString(), anyInt())).thenReturn(false);
        when(biPredicate.test(eq("3"),  anyInt())).thenReturn(true);

        Optional<String> result = FoldLeft.foldLeftOptionally(stream, supplier, biPredicate, biFunction);

        verify(supplier, only()).get();
        verify(biPredicate, times(2)).test(anyString(), anyInt());
        verify(biFunction, times(2)).apply(anyString(), anyInt());
        assertEquals(Optional.of("3"), result);
    }
    @Test
    @DisplayName("Should return a value wrapped in an optional ignoring the predicate")
    public void foldLeftOptionallyIgnoringPredicate() {
        Stream<Integer> stream = Stream.of(1, 2, 3, 4);
        when(supplier.get()).thenReturn("0");
        when(biFunction.apply(anyString(), anyInt())).thenAnswer(invocationOnMock -> String.valueOf(sumParams(invocationOnMock, 0, 1)));

        Optional<String> result = FoldLeft.foldLeftOptionally(stream, supplier, biFunction);

        verify(biFunction, times(4)).apply(anyString(), anyInt());
        assertEquals(Optional.of("10"), result);
    }
    @Test
    @DisplayName("Should return initial value wrapped in an optional using a consumer")
    public void foldLeftOptionallyConsumer() {
        Stream<Integer> stream = Stream.of(1, 2, 3, 4);
        when(supplier.get()).thenReturn("Initial");
        when(biPredicate.test(anyString(), anyInt())).thenReturn(false);
        when(biPredicate.test(anyString(), eq(3))).thenReturn(true);

        Optional<String> result = FoldLeft.foldLeftOptionally(stream, supplier, biPredicate, biConsumer);
        verify(biConsumer, times(2)).accept(anyString(), anyInt());
        assertEquals(Optional.of("Initial"), result);
    }
    @Test
    @DisplayName("Should return initial value wrapped in an optional using a consumer ignoring the predicate")
    public void foldLeftOptionallyConsumerIgnoringPredicate() {
        Stream<Integer> stream = Stream.of(1, 2, 3, 4);
        when(supplier.get()).thenReturn("Initial");

        Optional<String> result = FoldLeft.foldLeftOptionally(stream, supplier, biConsumer);

        verify(biConsumer, times(4)).accept(anyString(), anyInt());
        assertEquals(Optional.of("Initial"), result);
    }

    @Test
    @DisplayName("Should return a collector with an option as a result")
    public void foldLeftCollectorOptionally() {
        Stream<Integer> stream = Stream.of(1, 2, 3, 4);
        when(supplierElement.get()).thenReturn(0);
        when(binaryOperator.apply(anyInt(), anyInt())).thenAnswer(invocation -> sumParams(invocation,0,1));

        Optional<Integer> result = stream.collect(FoldLeft.foldLeftOptionally(supplierElement, binaryOperator));

        verify(binaryOperator, times(4)).apply(anyInt(), anyInt());
        assertEquals(Optional.of(10), result);
    }
    @Test
    @DisplayName("Should return an empty optional")
    public void foldLeftCollectorOptionallyEmpty() {
        Stream<Integer> stream = Stream.of();
        Optional<Integer> result = stream.collect(FoldLeft.foldLeftOptionally(supplierElement, binaryOperator));
        assertEquals(Optional.empty(), result);
    }
    @Test
    @DisplayName("Should return a collector with an option as a result using a consumer")
    public void foldLeftCollectorOptionallyConsumer() {
        Stream<Integer> stream = Stream.of(1, 2, 3, 4);
        when(supplier.get()).thenReturn("0");
        Optional<String> result = stream.collect(FoldLeft.foldLeftOptionally(supplier, biConsumer));

        verify(biConsumer, times(4)).accept(anyString(), anyInt());
        assertEquals(Optional.of("0"), result);
    }

    private int sumParams(InvocationOnMock invocationOnMock, int x, int x1) {
        Object[] arguments = invocationOnMock.getArguments();
        return Stream.of(arguments[x], arguments[x1])
                .map(Object::toString)
                .map(Integer::valueOf)
                .mapToInt(e -> e)
                .sum();
    }
    private String concatParams(InvocationOnMock invocationOnMock, int x, int x1) {
        Object[] arguments = invocationOnMock.getArguments();
        Predicate<String> notBlank=Predicate.not(String::isBlank);
        return Stream.of(arguments[x], arguments[x1])
                .map(Object::toString)
                .filter(notBlank)
                .collect(Collectors.joining(","));
    }

}