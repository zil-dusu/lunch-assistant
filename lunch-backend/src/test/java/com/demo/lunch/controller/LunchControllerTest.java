package com.demo.lunch.controller;

import com.demo.lunch.entity.Food;
import com.demo.lunch.service.RecommendService;
import com.demo.lunch.repository.FoodRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Controller 接口测试 */
@WebMvcTest(LunchController.class)
class LunchControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper om;
    @MockBean private RecommendService recommendService;
    @MockBean private FoodRepository foodRepo;

    // ===== GET /api/foods =====

    @Test
    void getFoods_ShouldReturnList() throws Exception {
        Food f = makeFood(1L, "红烧肉", "米饭", "不辣", "支持校园送");
        when(foodRepo.findAll()).thenReturn(List.of(f));

        mockMvc.perform(get("/api/foods"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].spicy").value("不辣"))
            .andExpect(jsonPath("$[0].name").value("红烧肉"));
    }

    // ===== GET /api/categories =====

    @Test
    void getCategories_ShouldReturnDistinct() throws Exception {
        when(foodRepo.findAll()).thenReturn(List.of(
            makeFood(1L, "A", "米饭", "辣", ""),
            makeFood(2L, "B", "面食", "不辣", ""),
            makeFood(3L, "C", "米饭", "辣", "")
        ));
        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    // ===== GET /api/spicies =====

    @Test
    void getSpicies_ShouldReturnOptions() throws Exception {
        when(foodRepo.findAll()).thenReturn(List.of(
            makeFood(1L, "A", "", "辣", ""),
            makeFood(2L, "B", "", "不辣", "")
        ));
        mockMvc.perform(get("/api/spicies"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    // ===== POST /api/recommend =====

    @Test
    void recommend_Success() throws Exception {
        Food f = makeFood(1L, "黄焖鸡", "米饭", "辣", "支持校园送");
        when(recommendService.randomRecommend(isNull(), isNull(), eq("u1"), isNull()))
            .thenReturn(f);

        mockMvc.perform(post("/api/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("userId", "u1"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("黄焖鸡"))
            .andExpect(jsonPath("$.spicy").value("辣"));
    }

    @Test
    void recommend_WithSpicy() throws Exception {
        Food f = makeFood(1L, "蛋炒饭", "米饭", "不辣", "");
        when(recommendService.randomRecommend(isNull(), eq("不辣"), eq("u1"), isNull()))
            .thenReturn(f);

        mockMvc.perform(post("/api/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("userId", "u1", "spicy", "不辣"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.spicy").value("不辣"));
    }

    @Test
    void recommend_NotFound_Return400() throws Exception {
        when(recommendService.randomRecommend(any(), any(), any(), any())).thenReturn(null);

        mockMvc.perform(post("/api/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("userId", "u1"))))
            .andExpect(status().isBadRequest());
    }

    // ===== POST /api/favorite/toggle =====

    @Test
    void toggleFavorite_Add() throws Exception {
        when(recommendService.toggleFavorite("u1", 1L)).thenReturn(true);
        mockMvc.perform(post("/api/favorite/toggle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("userId", "u1", "foodId", 1))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.favorited").value(true));
    }

    @Test
    void toggleFavorite_Remove() throws Exception {
        when(recommendService.toggleFavorite("u1", 1L)).thenReturn(false);
        mockMvc.perform(post("/api/favorite/toggle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("userId", "u1", "foodId", 1))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.favorited").value(false));
    }

    // ===== GET /api/hotrank =====

    @Test
    void getHotRank() throws Exception {
        when(recommendService.getHotRank()).thenReturn(List.of(
            makeFood(1L, "A", "米饭", "辣", "")
        ));
        mockMvc.perform(get("/api/hotrank"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    // ===== GET /api/algorithm/sort =====

    @Test
    void sortByMerge() throws Exception {
        when(recommendService.sortByMerge("hotCount", true))
            .thenReturn(Map.of("algorithm", "归并排序 (Merge Sort) O(n log n)"));
        mockMvc.perform(get("/api/algorithm/sort").param("by", "hotCount"))
            .andExpect(status().isOk());
    }

    // ===== 工具 =====

    private Food makeFood(Long id, String name, String cat, String spicy, String src) {
        Food f = new Food();
        f.setId(id); f.setName(name); f.setCategory(cat);
        f.setSpicy(spicy); f.setSource(src); f.setHotCount(0);
        return f;
    }
}
