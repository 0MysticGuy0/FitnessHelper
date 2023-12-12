package com.mygy.fitnesshelper.data;

import java.io.Serializable;
import java.util.HashMap;

public class Product implements Serializable {
     private String name;
     private float proteins;
     private float fats;
     private float carbs;
     private float ccals;

    public Product(String name, float proteins, float fats, float carbs, float ccals) {
        this.name = name;
        this.proteins = proteins;
        this.fats = fats;
        this.carbs = carbs;
        this.ccals = ccals;
    }
    public Product(HashMap<String ,Object> doc){
        name = (String) doc.get("name");
        try {
            proteins = ((Double) doc.get("proteins")).floatValue();
            fats = ((Double) doc.get("fats")).floatValue();
            carbs = ((Double) doc.get("carbs")).floatValue();
            ccals = ((Double) doc.get("ccals")).floatValue();
        }catch (NullPointerException ex){
            proteins = 0f;
            fats = 0f;
            carbs = 0f;
            ccals = 0f;
        }
    }

    public String getName() {
        return name;
    }

    public float getProteins() {
        return proteins;
    }

    public float getFats() {
        return fats;
    }

    public float getCarbs() {
        return carbs;
    }

    public float getCcals() {
        return ccals;
    }
}
