//该类为service类,用于接收前端发送的请求并处理
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
    public Food randomRecommend(String category, String spicy, String userId, Long excludeId) {
        List<Food> candidates = foodRepo.findAll();

        // 按分类（米饭/面食）过滤
        if (category != null && !category.isEmpty()) {
            candidates = candidates.stream()
                .filter(f -> category.equals(f.getCategory()))
                .toList();
        }

        // 按辣度过滤
        if (spicy != null && !spicy.isEmpty()) {
            candidates = candidates.stream()
                .filter(f -> spicy.equals(f.getSpicy()))
                .toList();
        }

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

    // ===== 归并排序演示 =====

    /**
     * 使用归并排序对所有菜品排序 O(n log n)
     */
    public Map<String, Object> sortByMerge(String by, boolean desc) {
        List<Food> foods = new ArrayList<>(foodRepo.findAll());
        Comparator<Food> cmp = buildComparator(by, desc);

        long nanos = SortAlgorithms.measureTime(() -> SortAlgorithms.mergeSort(foods, cmp));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("algorithm", "归并排序 (Merge Sort) O(n log n)");
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

    // ===== 工具方法 =====

    private Comparator<Food> buildComparator(String by, boolean desc) {
        Comparator<Food> cmp = switch (by) {
            case "name"     -> Comparator.comparing(Food::getName);
            case "category" -> Comparator.comparing(Food::getCategory);
            case "spicy"    -> Comparator.comparing(Food::getSpicy);
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
