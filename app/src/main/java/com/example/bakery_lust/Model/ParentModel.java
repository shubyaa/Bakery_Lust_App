package com.example.bakery_lust.Model;

import java.util.ArrayList;

public class ParentModel {
    private String categoryName;
    private String categoryId;
    private long cost;
    private String description;
    private String rating;


    public ParentModel() {

    }

    public ParentModel(String categoryName, String categoryId, long cost, String description, String rating) {
        this.categoryName = categoryName;
        this.categoryId = categoryId;
        this.cost = cost;
        this.description = description;
        this.rating = rating;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }
}

