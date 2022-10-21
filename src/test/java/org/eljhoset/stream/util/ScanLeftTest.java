package org.eljhoset.stream.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScanLeftTest {
    @Mock
    Predicate<Integer> predicate;
    @Mock
    Supplier<Integer> supplier;
    @Mock
    BinaryOperator<Integer> op;

    @Test
    @DisplayName("Should stop traversing the stream if the condition in the predicate is met using the element")
    public void scanLeft() {
        Stream<Integer> stream = Stream.of(1, 2, 3, 4);
        when(supplier.get()).thenReturn(0);
        when(op.apply(anyInt(), anyInt())).thenAnswer(this::sumParams);
        when(predicate.test(anyInt())).thenReturn(false);
        when(predicate.test(eq(4))).thenReturn(true);

        Stream<Integer> result = ScanLeft.scanLeft(stream, supplier, predicate, op);

        verify(predicate, times(4)).test(anyInt());
        verify(op, times(3)).apply(anyInt(), anyInt());
        assertEquals(Stream.of(1, 3, 5).toList(), result.toList());
    }

    @Test
    @DisplayName("Should return a new stream ignoring predicate")
    public void scanLeftIgnoringPredicate() {
        Stream<Integer> stream = Stream.of(1, 2, 3, 4);
        when(supplier.get()).thenReturn(0);
        when(op.apply(anyInt(), anyInt())).thenAnswer(this::sumParams);

        Stream<Integer> result = ScanLeft.scanLeft(stream, supplier, op);

        verify(op, times(4)).apply(anyInt(), anyInt());
        assertEquals(Stream.of(1, 3, 5, 7).toList(), result.toList());
    }

    private int sumParams(InvocationOnMock invocationOnMock) {
        Object[] arguments = invocationOnMock.getArguments();
        return Stream.of(arguments[0], arguments[1])
                .map(Object::toString)
                .map(Integer::valueOf)
                .mapToInt(e -> e)
                .sum();
    }
}