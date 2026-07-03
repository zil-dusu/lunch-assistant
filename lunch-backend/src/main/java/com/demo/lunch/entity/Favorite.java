package com.demo.lunch.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "favorite")
@IdClass(FavoriteId.class)
public class Favorite {

    @Id
    @Column(name = "user_id", length = 64)
    private String userId;

    @Id
    @Column(name = "food_id")
    private Long foodId;

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
