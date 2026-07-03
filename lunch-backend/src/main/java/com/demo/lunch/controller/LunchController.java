//该类为controller,用于响应请求
package com.demo.lunch.controller;

import com.demo.lunch.entity.Food;
import com.demo.lunch.service.RecommendService;
import com.demo.lunch.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LunchController {

    @Autowired
    private RecommendService recommendService;

    @Autowired
    private FoodRepository foodRepo;

    // ==================== 菜品查询 ====================

    @GetMapping("/foods")
    public List<Food> getFoods(@RequestParam(required = false) String category) {
        return (category == null || category.isEmpty())
            ? foodRepo.findAll()
            : foodRepo.findByCategory(category);
    }

    @GetMapping("/categories")
    public List<String> getCategories() {
        return foodRepo.findAll().stream()
            .map(Food::getCategory)
            .distinct()
            .sorted()
            .toList();
    }

    // ==================== 推荐 ====================

    @PostMapping("/recommend")
    public ResponseEntity<?> recommend(@RequestBody Map<String, Object> body) {
        String userId = (String) body.getOrDefault("userId", "anonymous");
        String category = (String) body.getOrDefault("category", null);
        Boolean fromFav = (Boolean) body.getOrDefault("fromFav", false);

        Long excludeId = null;
        Object excludeObj = body.get("excludeId");
        if (excludeObj instanceof Number) {
            excludeId = ((Number) excludeObj).longValue();
        }

        Food result = fromFav
            ? recommendService.randomFromFavorites(userId, excludeId)
            : recommendService.randomRecommend(category, userId, excludeId);

        if (result == null) {
            return ResponseEntity.badRequest().body(
                Map.of("error", fromFav ? "您还没有收藏菜品，请先添加最爱！" : "没有符合条件的菜品！")
            );
        }
        return ResponseEntity.ok(result);
    }

    /** 查看推荐历史（LRU 缓存） */
    @GetMapping("/recommend/history")
    public List<Food> getRecommendHistory(@RequestParam String userId) {
        return recommendService.getRecommendHistory(userId);
    }

    // ==================== 收藏 ====================

    @PostMapping("/favorite/toggle")
    public ResponseEntity<?> toggleFavorite(@RequestBody Map<String, Object> body) {
        String userId = (String) body.get("userId");
        Long foodId = Long.valueOf(body.get("foodId").toString());

        boolean isFav = recommendService.toggleFavorite(userId, foodId);
        return ResponseEntity.ok(Map.of(
            "foodId", foodId,
            "favorited", isFav,
            "message", isFav ? "已收藏" : "已取消收藏"
        ));
    }

    @GetMapping("/favorites")
    public List<Food> getFavorites(@RequestParam String userId) {
        return recommendService.getFavorites(userId);
    }

    // ==================== 热度榜 ====================

    @GetMapping("/hotrank")
    public List<Food> getHotRank() {
        return recommendService.getHotRank();
    }

    // ==================== 算法演示 ====================

    /**
     * 用指定排序算法对所有菜品排序
     *
     * GET /api/algorithm/sort?algorithm=quick&by=hotCount&desc=true
     *
     * 支持的算法:
     *   bubble  — 冒泡排序 O(n²) 稳定
     *   quick   — 快速排序 O(n log n)
     *   merge   — 归并排序 O(n log n) 稳定
     *   heap    — 堆排序   O(n log n) 原地
     *   counting— 计数排序 O(n+k)（仅 by=hotCount 时有效）
     *
     * 排序字段 by: hotCount / name / category
     */
    @GetMapping("/algorithm/sort")
    public Map<String, Object> sortFoods(
            @RequestParam(defaultValue = "quick") String algorithm,
            @RequestParam(defaultValue = "hotCount") String by,
            @RequestParam(defaultValue = "true") boolean desc) {
        return recommendService.getFoodsSorted(algorithm, by, desc);
    }

    /**
     * 用小顶堆获取 Top-K（与全量排序对比效率）
     *
     * GET /api/algorithm/topk?k=5
     */
    @GetMapping("/algorithm/topk")
    public Map<String, Object> getTopK(
            @RequestParam(defaultValue = "5") int k,
            @RequestParam(defaultValue = "hotCount") String by) {
        return recommendService.getTopKByHeap(k, by);
    }

    /**
     * 一次性跑遍所有排序算法，对比耗时
     *
     * GET /api/algorithm/compare?by=hotCount
     */
    @GetMapping("/algorithm/compare")
    public Map<String, Object> compareAlgorithms(
            @RequestParam(defaultValue = "hotCount") String by,
            @RequestParam(defaultValue = "true") boolean desc) {
        return recommendService.compareAllSortAlgorithms(by, desc);
    }
}
