package com.demo.lunch.algorithm;

import java.util.*;

/**
 * 手动实现的小顶堆（MinHeap），用于高效取 Top-K。
 *
 * 原理：
 * - 维护一个大小为 K 的最小堆
 * - 遍历所有元素，比堆顶大就替换并下沉
 * - 最终堆中就是最大的 K 个元素
 * - 时间复杂度 O(N log K)，空间复杂度 O(K)
 *
 * 对比：全部排序 O(N log N) → Top-K 用堆 O(N log K)，K 很小时优势明显
 *
 * @param <T> 元素类型
 */
public class TopKHeap<T> {

    private final int k;
    private final List<T> heap;
    private final Comparator<T> cmp;

    public TopKHeap(int k, Comparator<T> cmp) {
        this.k = k;
        this.heap = new ArrayList<>(k);
        this.cmp = cmp;
    }

    /** 添加一个元素，保持堆大小不超过 K */
    public void offer(T item) {
        if (heap.size() < k) {
            heap.add(item);
            siftUp(heap.size() - 1);
        } else if (cmp.compare(item, heap.get(0)) > 0) {
            // 比堆顶（第K大）大，替换堆顶并下沉
            heap.set(0, item);
            siftDown(0);
        }
    }

    /** 批量添加 */
    public void offerAll(Collection<T> items) {
        for (T item : items) offer(item);
    }

    /** 获取 Top-K 结果（降序排列：最大 → 最小） */
    public List<T> getTopK() {
        // 把堆中元素取出来排序（O(K log K)）
        List<T> result = new ArrayList<>(heap);
        result.sort(cmp.reversed()); // 降序
        return result;
    }

    public int size() { return heap.size(); }

    // ===== 堆操作 =====

    /** 上浮：新元素从底部向上调整 */
    private void siftUp(int idx) {
        while (idx > 0) {
            int parent = (idx - 1) / 2;
            if (cmp.compare(heap.get(idx), heap.get(parent)) >= 0) break;
            Collections.swap(heap, idx, parent);
            idx = parent;
        }
    }

    /** 下沉：堆顶元素向下调整 */
    private void siftDown(int idx) {
        int size = heap.size();
        while (true) {
            int smallest = idx;
            int left = 2 * idx + 1;
            int right = 2 * idx + 2;
            if (left < size && cmp.compare(heap.get(left), heap.get(smallest)) < 0) smallest = left;
            if (right < size && cmp.compare(heap.get(right), heap.get(smallest)) < 0) smallest = right;
            if (smallest == idx) break;
            Collections.swap(heap, idx, smallest);
            idx = smallest;
        }
    }
}
