package com.marekmacko.busapi.models;

import java.util.ArrayList;
import java.util.List;

public class Category {

    private String description;
    private List<Line> lines = new ArrayList<>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }
}
