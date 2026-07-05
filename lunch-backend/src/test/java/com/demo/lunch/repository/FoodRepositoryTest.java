package com.demo.lunch.repository;

import com.demo.lunch.entity.Food;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** FoodRepository 数据层测试 — H2 内存数据库 */
@DataJpaTest
@TestPropertySource(properties = {"spring.sql.init.mode=never"})
class FoodRepositoryTest {

    @Autowired private FoodRepository foodRepo;
    @Autowired private TestEntityManager em;

    @Test
    void save_ShouldPersistWithSpicy() {
        Food food = new Food();
        food.setName("黄焖鸡米饭");
        food.setCategory("米饭");
        food.setSpicy("辣");
        food.setSource("商业街");
        food.setHotCount(0);

        Food saved = foodRepo.save(food);
        assertNotNull(saved.getId());
        assertEquals("辣", saved.getSpicy());
        assertEquals("米饭", saved.getCategory());
    }

    @Test
    void findByCategory_ShouldFilter() {
        em.persist(createFood("红烧肉", "米饭", "不辣"));
        em.persist(createFood("麻辣香锅", "米饭", "辣"));
        em.persist(createFood("兰州拉面", "面食", "不辣"));
        em.flush();

        List<Food> rice = foodRepo.findByCategory("米饭");
        assertEquals(2, rice.size());
    }

    @Test
    void findTop5ByHotCount_ShouldReturnOrdered() {
        em.persist(createFood("A", "米饭", "不辣", 5));
        em.persist(createFood("B", "米饭", "辣", 30));
        em.persist(createFood("C", "面食", "辣", 15));
        em.flush();

        List<Food> top = foodRepo.findTop5ByOrderByHotCountDesc();
        assertEquals(30, top.get(0).getHotCount());
    }

    @Test
    void findAll_ShouldReturnAllWithSpicy() {
        em.persist(createFood("A", "米饭", "辣"));
        em.persist(createFood("B", "小吃", "不辣"));
        em.flush();

        List<Food> all = foodRepo.findAll();
        assertEquals(2, all.size());
        assertNotNull(all.get(0).getSpicy());
    }

    private Food createFood(String name, String category, String spicy) {
        return createFood(name, category, spicy, 0);
    }

    private Food createFood(String name, String category, String spicy, int hot) {
        Food f = new Food();
        f.setName(name); f.setCategory(category); f.setSpicy(spicy);
        f.setSource("测试"); f.setHotCount(hot);
        return f;
    }
}
