package com.example.basketballshoesandroidshop.Domain;

import java.io.Serializable;

public class CategoryModel implements Serializable {
    private String title;
    private int id;
    private String picUrl;

    public CategoryModel() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }
}
