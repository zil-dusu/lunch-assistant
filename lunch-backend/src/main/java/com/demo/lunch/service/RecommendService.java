package com.demo.lunch.service;

import com.demo.lunch.algorithm.LRUCache;
import com.demo.lunch.algorithm.SortAlgorithms;
import com.demo.lunch.algorithm.TopKHeap;
import com.demo.lunch.entity.Favorite;
import com.demo.lunch.entity.Food;
import com.demo.lunch.repository.FavoriteRepository;
import com.demo.lunch.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class RecommendService {

    @Autowired
    private FoodRepository foodRepo;

    @Autowired
    private FavoriteRepository favRepo;

    // ===== LRU 缓存：记录每个用户最近推荐的菜品（容量5），防重复 =====
    private final Map<String, LRUCache<Long, Food>> userHistory = new HashMap<>();

    /**
     * 普通随机推荐
     * 使用 LRU 缓存记录该用户最近推荐过的菜品，避免短时间内重复
     */
    public Food randomRecommend(String category, String userId, Long excludeId) {
        List<Food> candidates = (category == null || category.isEmpty())
            ? foodRepo.findAll()
            : foodRepo.findByCategory(category);

        if (excludeId != null) {
            Long exId = excludeId;
            candidates = candidates.stream().filter(f -> !f.getId().equals(exId)).toList();
        }

        // LRU 防重复：排除最近推荐过的
        LRUCache<Long, Food> history = getUserHistory(userId);
        List<Food> historyFoods = history.values();
        if (candidates.size() > historyFoods.size()) {
            Set<Long> histIds = new HashSet<>();
            for (Food hf : historyFoods) histIds.add(hf.getId());
            candidates = candidates.stream().filter(f -> !histIds.contains(f.getId())).toList();
        }

        if (candidates.isEmpty()) return null;

        int idx = ThreadLocalRandom.current().nextInt(candidates.size());
        Food selected = candidates.get(idx);

        // 更新 LRU 缓存
        history.put(selected.getId(), selected);

        selected.setHotCount(selected.getHotCount() + 1);
        return foodRepo.save(selected);
    }

    public Food randomFromFavorites(String userId, Long excludeId) {
        List<Favorite> favs = favRepo.findByUserId(userId);
        if (favs.isEmpty()) return null;

        List<Food> favFoods = favs.stream()
            .map(fav -> foodRepo.findById(fav.getFoodId()).orElse(null))
            .filter(Objects::nonNull)
            .filter(f -> excludeId == null || !f.getId().equals(excludeId))
            .toList();

        if (favFoods.isEmpty()) return null;

        int idx = ThreadLocalRandom.current().nextInt(favFoods.size());
        Food selected = favFoods.get(idx);

        selected.setHotCount(selected.getHotCount() + 1);
        return foodRepo.save(selected);
    }

    // ===== LRU 管理 =====

    private LRUCache<Long, Food> getUserHistory(String userId) {
        return userHistory.computeIfAbsent(userId, k -> new LRUCache<>(5));
    }

    /** 查看用户推荐历史（LRU 顺序：最近→最早） */
    public List<Food> getRecommendHistory(String userId) {
        LRUCache<Long, Food> history = userHistory.get(userId);
        return history != null ? history.values() : Collections.emptyList();
    }

    // ===== 多种排序算法演示 =====

    /**
     * 使用指定排序算法对所有菜品排序
     * @param algorithm 算法名：bubble / quick / merge / heap / counting
     * @param by        排序字段：hotCount / name / category
     * @param desc      是否降序
     * @return 排序结果 + 耗时信息
     */
    public Map<String, Object> getFoodsSorted(String algorithm, String by, boolean desc) {
        List<Food> foods = new ArrayList<>(foodRepo.findAll());

        Comparator<Food> cmp = buildComparator(by, desc);

        long nanos;
        switch (algorithm.toLowerCase()) {
            case "bubble":
                nanos = SortAlgorithms.measureTime(() -> SortAlgorithms.bubbleSort(foods, cmp));
                break;
            case "merge":
                nanos = SortAlgorithms.measureTime(() -> SortAlgorithms.mergeSort(foods, cmp));
                break;
            case "heap":
                nanos = SortAlgorithms.measureTime(() -> SortAlgorithms.heapSort(foods, cmp));
                break;
            case "counting":
                // 计数排序仅适用于整型字段
                if ("hotCount".equals(by)) {
                    nanos = SortAlgorithms.measureTime(() ->
                        SortAlgorithms.countingSortByInt(foods, f -> desc ? -f.getHotCount() : f.getHotCount()));
                } else {
                    nanos = SortAlgorithms.measureTime(() -> SortAlgorithms.quickSort(foods, cmp));
                }
                break;
            default: // "quick"
                nanos = SortAlgorithms.measureTime(() -> SortAlgorithms.quickSort(foods, cmp));
                break;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("algorithm", algorithm);
        result.put("timeNanos", nanos);
        result.put("timeMillis", String.format("%.3f ms", nanos / 1_000_000.0));
        result.put("data", foods);
        return result;
    }

    // ===== Top-K 堆排序演示 =====

    /**
     * 使用小顶堆获取热度最高的 Top-K 菜品
     * 对比：全部排序 O(N log N) → 堆 O(N log K)
     */
    public Map<String, Object> getTopKByHeap(int k, String by) {
        List<Food> all = foodRepo.findAll();
        if (all.isEmpty()) {
            return Map.of("data", Collections.emptyList(), "total", 0);
        }

        // 使用自定义小顶堆
        TopKHeap<Food> heap = new TopKHeap<>(k,
            Comparator.comparingInt(Food::getHotCount));

        long nanos = SortAlgorithms.measureTime(() -> heap.offerAll(all));
        List<Food> topK = heap.getTopK();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("algorithm", "MinHeap (Top-K)");
        result.put("k", k);
        result.put("totalItems", all.size());
        result.put("timeNanos", nanos);
        result.put("timeMillis", String.format("%.3f ms", nanos / 1_000_000.0));
        result.put("complexity", String.format("O(N log K) = O(%d log %d)", all.size(), k));
        result.put("data", topK);
        return result;
    }

    // ===== 算法对比：一次性跑所有排序算法，比较耗时 =====

    public Map<String, Object> compareAllSortAlgorithms(String by, boolean desc) {
        Comparator<Food> cmp = buildComparator(by, desc);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sortedBy", by);
        result.put("descending", desc);
        Map<String, String> times = new LinkedHashMap<>();

        for (String algo : List.of("bubble", "quick", "merge", "heap")) {
            List<Food> copy = new ArrayList<>(foodRepo.findAll());
            long nanos;
            switch (algo) {
                case "bubble" -> nanos = SortAlgorithms.measureTime(() -> SortAlgorithms.bubbleSort(copy, cmp));
                case "merge"  -> nanos = SortAlgorithms.measureTime(() -> SortAlgorithms.mergeSort(copy, cmp));
                case "heap"   -> nanos = SortAlgorithms.measureTime(() -> SortAlgorithms.heapSort(copy, cmp));
                default       -> nanos = SortAlgorithms.measureTime(() -> SortAlgorithms.quickSort(copy, cmp));
            }
            times.put(algo, String.format("%.3f ms", nanos / 1_000_000.0));
        }
        result.put("timings", times);
        return result;
    }

    // ===== 工具方法 =====

    private Comparator<Food> buildComparator(String by, boolean desc) {
        Comparator<Food> cmp = switch (by) {
            case "name"     -> Comparator.comparing(Food::getName);
            case "category" -> Comparator.comparing(Food::getCategory);
            default         -> Comparator.comparingInt(Food::getHotCount); // hotCount
        };
        return desc ? cmp.reversed() : cmp;
    }

    public boolean toggleFavorite(String userId, Long foodId) {
        if (favRepo.existsByUserIdAndFoodId(userId, foodId)) {
            favRepo.deleteByUserIdAndFoodId(userId, foodId);
            return false;
        } else {
            Favorite fav = new Favorite();
            fav.setUserId(userId);
            fav.setFoodId(foodId);
            favRepo.save(fav);
            return true;
        }
    }

    public List<Food> getFavorites(String userId) {
        return favRepo.findByUserId(userId).stream()
            .map(fav -> foodRepo.findById(fav.getFoodId()).orElse(null))
            .filter(Objects::nonNull)
            .toList();
    }

    public List<Food> getHotRank() {
        return foodRepo.findTop5ByOrderByHotCountDesc();
    }
}
