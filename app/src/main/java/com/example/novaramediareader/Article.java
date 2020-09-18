package com.example.novaramediareader;

public class Article {

    private final String titles;
    private final String description;

    public Article(String name, String date) {
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
