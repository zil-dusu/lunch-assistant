package com.demo.lunch.algorithm;

import java.util.*;
import java.util.function.ToLongFunction;

/**
 * 多种排序算法实现，用于菜品排序演示。
 * 每种算法都对 List<Food> 按指定字段排序，方便对比效率。
 */
public class SortAlgorithms {

    private SortAlgorithms() {}

    // ==================== 1. 冒泡排序 ====================

    /** O(n²) 稳定 — 适合小数据量，演示用 */
    public static <T> void bubbleSort(List<T> list, Comparator<T> cmp) {
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                if (cmp.compare(list.get(j), list.get(j + 1)) > 0) {
                    Collections.swap(list, j, j + 1);
                    swapped = true;
                }
            }
            if (!swapped) break; // 提前终止优化
        }
    }

    // ==================== 2. 快速排序 ====================

    /** O(n log n) 平均 — 实际应用最广泛 */
    public static <T> void quickSort(List<T> list, Comparator<T> cmp) {
        quickSort(list, cmp, 0, list.size() - 1);
    }

    private static <T> void quickSort(List<T> list, Comparator<T> cmp, int lo, int hi) {
        if (lo >= hi) return;
        int pivot = partition(list, cmp, lo, hi);
        quickSort(list, cmp, lo, pivot - 1);
        quickSort(list, cmp, pivot + 1, hi);
    }

    private static <T> int partition(List<T> list, Comparator<T> cmp, int lo, int hi) {
        T pivot = list.get(hi); // 选最右元素为基准
        int i = lo;
        for (int j = lo; j < hi; j++) {
            if (cmp.compare(list.get(j), pivot) <= 0) {
                Collections.swap(list, i, j);
                i++;
            }
        }
        Collections.swap(list, i, hi);
        return i;
    }

    // ==================== 3. 归并排序 ====================

    /** O(n log n) 稳定 — 适合需要稳定性的场景 */
    public static <T> void mergeSort(List<T> list, Comparator<T> cmp) {
        List<T> sorted = mergeSortRec(list, cmp);
        for (int i = 0; i < list.size(); i++) {
            list.set(i, sorted.get(i));
        }
    }

    private static <T> List<T> mergeSortRec(List<T> list, Comparator<T> cmp) {
        if (list.size() <= 1) return new ArrayList<>(list);
        int mid = list.size() / 2;
        List<T> left = mergeSortRec(list.subList(0, mid), cmp);
        List<T> right = mergeSortRec(list.subList(mid, list.size()), cmp);
        return merge(left, right, cmp);
    }

    private static <T> List<T> merge(List<T> left, List<T> right, Comparator<T> cmp) {
        List<T> result = new ArrayList<>();
        int i = 0, j = 0;
        while (i < left.size() && j < right.size()) {
            result.add(cmp.compare(left.get(i), right.get(j)) <= 0 ? left.get(i++) : right.get(j++));
        }
        while (i < left.size()) result.add(left.get(i++));
        while (j < right.size()) result.add(right.get(j++));
        return result;
    }

    // ==================== 4. 堆排序 ====================

    /** O(n log n) 原地 — 适合 Top-K 场景 */
    public static <T> void heapSort(List<T> list, Comparator<T> cmp) {
        int n = list.size();
        // 建堆
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(list, n, i, cmp);
        }
        // 依次取堆顶
        for (int i = n - 1; i > 0; i--) {
            Collections.swap(list, 0, i);
            heapify(list, i, 0, cmp);
        }
    }

    private static <T> void heapify(List<T> list, int heapSize, int root, Comparator<T> cmp) {
        int largest = root;
        int left = 2 * root + 1;
        int right = 2 * root + 2;
        if (left < heapSize && cmp.compare(list.get(left), list.get(largest)) > 0) largest = left;
        if (right < heapSize && cmp.compare(list.get(right), list.get(largest)) > 0) largest = right;
        if (largest != root) {
            Collections.swap(list, root, largest);
            heapify(list, heapSize, largest, cmp);
        }
    }

    // ==================== 5. 计数排序（仅适用于 hotCount 整数范围） ====================

    /** 适用于 hotCount 这种小范围整数排序，O(n + k) */
    public static <T> void countingSortByInt(List<T> list, ToLongFunction<T> keyExtractor) {
        if (list.isEmpty()) return;
        long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
        for (T item : list) {
            long v = keyExtractor.applyAsLong(item);
            if (v < min) min = v;
            if (v > max) max = v;
        }
        int range = (int) (max - min + 1);
        if (range > 1_000_000) return; // 范围太大，不适合

        List<T>[] buckets = new List[range];
        for (T item : list) {
            int idx = (int) (keyExtractor.applyAsLong(item) - min);
            if (buckets[idx] == null) buckets[idx] = new ArrayList<>();
            buckets[idx].add(item);
        }
        List<T> sorted = new ArrayList<>();
        for (List<T> bucket : buckets) {
            if (bucket != null) sorted.addAll(bucket);
        }
        for (int i = 0; i < list.size(); i++) {
            list.set(i, sorted.get(i));
        }
    }

    // ==================== 工具方法 ====================

    /** 测量排序耗时（纳秒） */
    public static <T> long measureTime(Runnable sortTask) {
        long start = System.nanoTime();
        sortTask.run();
        return System.nanoTime() - start;
    }
}
