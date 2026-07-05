package com.demo.lunch.algorithm;

import org.junit.jupiter.api.Test;
import java.util.Comparator;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Top-K 堆测试 */
class TopKHeapTest {
    private final Comparator<Integer> nat = Comparator.naturalOrder();

    @Test
    void top3_ShouldReturnDescending() {
        TopKHeap<Integer> heap = new TopKHeap<>(3, nat);
        heap.offerAll(List.of(5, 2, 8, 1, 9, 3));
        assertEquals(List.of(9, 8, 5), heap.getTopK());
    }

    @Test
    void fewerThanK_ShouldReturnAll() {
        TopKHeap<Integer> heap = new TopKHeap<>(5, nat);
        heap.offerAll(List.of(3, 1, 2));
        assertEquals(3, heap.getTopK().size());
    }

    @Test
    void empty_ShouldReturnEmpty() {
        TopKHeap<Integer> heap = new TopKHeap<>(3, nat);
        assertTrue(heap.getTopK().isEmpty());
    }
}
