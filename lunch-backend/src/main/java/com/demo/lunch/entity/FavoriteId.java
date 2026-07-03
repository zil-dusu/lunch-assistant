package com.demo.lunch.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Favorite 联合主键类
 * 必须实现 Serializable，并重写 equals 和 hashCode
 */
public class FavoriteId implements Serializable {

    private String userId;
    private Long foodId;

    public FavoriteId() {}

    public FavoriteId(String userId, Long foodId) {
        this.userId = userId;
        this.foodId = foodId;
    }

    // ====== equals & hashCode ======

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FavoriteId that = (FavoriteId) o;
        return Objects.equals(userId, that.userId)
            && Objects.equals(foodId, that.foodId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, foodId);
    }

    // ====== Getters & Setters ======

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getFoodId() {
        return foodId;
    }

    public void setFoodId(Long foodId) {
        this.foodId = foodId;
    }
}
