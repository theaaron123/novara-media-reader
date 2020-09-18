package com.example.novaramediareader;

public class Article {

    private final String titles;
    private final String description;
    private final String permalink;

    public Article(String name, String date, String permalink) {
        this.titles = name;
        this.description = date;
        this.permalink = permalink;
    }

    public String getTitle() {
        return titles;
    }

    public String getDescription() {
        return description;
    }

    public String getPermalink() {
        return permalink;
    }
}
