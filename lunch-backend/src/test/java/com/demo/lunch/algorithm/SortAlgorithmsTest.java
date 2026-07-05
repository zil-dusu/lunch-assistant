package com.demo.lunch.algorithm;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** 排序算法测试 — 重点测归并排序 */
class SortAlgorithmsTest {
    private final Comparator<Integer> nat = Comparator.naturalOrder();

    @Test
    void mergeSort_ShouldSortCorrectly() {
        List<Integer> list = new ArrayList<>(List.of(7, 2, 9, 1, 5));
        SortAlgorithms.mergeSort(list, nat);
        assertEquals(List.of(1, 2, 5, 7, 9), list);
    }

    @Test
    void mergeSort_EmptyList_NoError() {
        List<Integer> empty = new ArrayList<>();
        assertDoesNotThrow(() -> SortAlgorithms.mergeSort(empty, nat));
    }

    @Test
    void mergeSort_SingleElement() {
        List<Integer> list = new ArrayList<>(List.of(42));
        SortAlgorithms.mergeSort(list, nat);
        assertEquals(List.of(42), list);
    }

    @Test
    void mergeSort_AlreadySorted() {
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        SortAlgorithms.mergeSort(list, nat);
        assertEquals(List.of(1, 2, 3, 4, 5), list);
    }

    @Test
    void mergeSort_ReverseOrder() {
        List<Integer> list = new ArrayList<>(List.of(5, 4, 3, 2, 1));
        SortAlgorithms.mergeSort(list, nat);
        assertEquals(List.of(1, 2, 3, 4, 5), list);
    }

    @Test
    void measureTime_ShouldReturnPositive() {
        long nanos = SortAlgorithms.measureTime(() -> {
            List<Integer> list = new ArrayList<>(List.of(5, 3, 8, 1));
            SortAlgorithms.mergeSort(list, nat);
        });
        assertTrue(nanos > 0);
    }
}
