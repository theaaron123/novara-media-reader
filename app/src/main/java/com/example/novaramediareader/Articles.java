package com.example.novaramediareader;

public class Articles {

    private final String titles;
    private final String description;

    public Articles(String name, String date) {
        this.titles = name;
        this.description = date;
    }

    public String getTitle() {
        return titles;
    }

    public String getDescription() {
        return description;
    }
}
