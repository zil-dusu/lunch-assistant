package com.demo.lunch.repository;

import com.demo.lunch.entity.Favorite;
import com.demo.lunch.entity.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {

    /** 查询某用户的所有收藏 */
    List<Favorite> findByUserId(String userId);

    /** 删除某用户的某个收藏 */
    @Modifying
    void deleteByUserIdAndFoodId(String userId, Long foodId);

    /** 检查某用户是否已收藏某菜品 */
    boolean existsByUserIdAndFoodId(String userId, Long foodId);
}
