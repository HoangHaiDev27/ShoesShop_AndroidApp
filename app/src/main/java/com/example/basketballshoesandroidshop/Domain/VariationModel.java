package com.example.basketballshoesandroidshop.Domain;

import com.google.firebase.Timestamp;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.List;

public class VariationModel implements Serializable {
    public String id;
    @Exclude
//    public List<ImageModel> images;
    public int inventory;
    public String name;
}
