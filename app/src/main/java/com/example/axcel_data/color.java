package com.example.axcel_data;

import java.util.HashMap;

public class color {
    String name;
    HashMap<String,Integer> sizes;

    public color(String name, HashMap<String, Integer> sizes) {
        this.name = name;
        this.sizes = sizes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, Integer> getSizes() {
        return sizes;
    }

    public void setSizes(HashMap<String, Integer> sizes) {
        this.sizes = sizes;
    }
}
