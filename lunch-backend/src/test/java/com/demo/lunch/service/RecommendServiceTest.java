package com.demo.lunch.service;

import com.demo.lunch.entity.Food;
import com.demo.lunch.repository.FavoriteRepository;
import com.demo.lunch.repository.FoodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** RecommendService 测试 — Mock 数据库层 */
@ExtendWith(MockitoExtension.class)
class RecommendServiceTest {

    @Mock private FoodRepository foodRepo;
    @Mock private FavoriteRepository favRepo;
    @InjectMocks private RecommendService service;

    private Food food1, food2;

    @BeforeEach
    void setUp() {
        food1 = makeFood(1L, "红烧肉套餐", "米饭", "不辣", "一食堂", 10);
        food2 = makeFood(2L, "麻辣香锅", "米饭", "辣", "二食堂", 25);
    }

    // ===== 随机推荐 =====

    @Test
    void randomRecommend_NoFilter_ShouldReturnFood() {
        when(foodRepo.findAll()).thenReturn(List.of(food1, food2));
        when(foodRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Food result = service.randomRecommend(null, null, "u1", null);
        assertNotNull(result);
        verify(foodRepo).save(any()); // hotCount+1
    }

    @Test
    void randomRecommend_BySpicy_ShouldFilter() {
        when(foodRepo.findAll()).thenReturn(List.of(food1, food2));
        when(foodRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Food result = service.randomRecommend(null, "不辣", "u1", null);
        assertEquals("不辣", result.getSpicy());
    }

    @Test
    void randomRecommend_ByCategory_ShouldFilter() {
        when(foodRepo.findAll()).thenReturn(List.of(food1, food2));

        Food spicyFood = makeFood(3L, "酸辣粉", "面食", "辣", "三食堂", 5);
        when(foodRepo.findAll()).thenReturn(List.of(food1, food2, spicyFood));
        when(foodRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Food result = service.randomRecommend("面食", null, "u1", null);
        assertEquals("面食", result.getCategory());
    }

    @Test
    void randomRecommend_NoCandidate_ReturnsNull() {
        when(foodRepo.findAll()).thenReturn(Collections.emptyList());
        assertNull(service.randomRecommend(null, null, "u1", null));
    }

    @Test
    void randomRecommend_ExcludeId_ShouldNotReturnSame() {
        when(foodRepo.findAll()).thenReturn(List.of(food1, food2));
        when(foodRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Food result = service.randomRecommend(null, null, "u1", 1L);
        assertNotEquals(1L, result.getId());
    }

    // ===== 收藏切换 =====

    @Test
    void toggleFavorite_Add_ShouldReturnTrue() {
        when(favRepo.existsByUserIdAndFoodId("u1", 1L)).thenReturn(false);
        assertTrue(service.toggleFavorite("u1", 1L));
        verify(favRepo).save(any());
    }

    @Test
    void toggleFavorite_Remove_ShouldReturnFalse() {
        when(favRepo.existsByUserIdAndFoodId("u1", 1L)).thenReturn(true);
        assertFalse(service.toggleFavorite("u1", 1L));
        verify(favRepo).deleteByUserIdAndFoodId("u1", 1L);
    }

    // ===== 热度榜 =====

    @Test
    void getHotRank_ShouldReturnTop5() {
        when(foodRepo.findTop5ByOrderByHotCountDesc()).thenReturn(List.of(food2, food1));
        List<Food> rank = service.getHotRank();
        assertEquals(2, rank.size());
        assertEquals("麻辣香锅", rank.get(0).getName());
    }

    // ===== 归并排序 =====

    @Test
    void sortByMerge_ShouldReturnSorted() {
        when(foodRepo.findAll()).thenReturn(List.of(food1, food2));
        Map<String, Object> result = service.sortByMerge("hotCount", false);
        assertNotNull(result.get("data"));
        assertEquals("归并排序 (Merge Sort) O(n log n)", result.get("algorithm"));
    }

    // ===== 工具方法 =====

    private Food makeFood(Long id, String name, String cat, String spicy, String src, int hot) {
        Food f = new Food();
        f.setId(id); f.setName(name); f.setCategory(cat);
        f.setSpicy(spicy); f.setSource(src); f.setHotCount(hot);
        return f;
    }
}
