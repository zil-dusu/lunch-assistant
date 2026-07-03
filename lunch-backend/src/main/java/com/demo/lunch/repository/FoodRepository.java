package com.demo.lunch.repository;

import com.demo.lunch.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

    /** 按分类筛选菜品 */
    List<Food> findByCategory(String category);

    /** 查询热度最高的前5个菜品 */
    List<Food> findTop5ByOrderByHotCountDesc();
}
