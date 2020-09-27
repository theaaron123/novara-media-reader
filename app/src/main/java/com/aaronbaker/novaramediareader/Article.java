package com.aaronbaker.novaramediareader;

public class Article {

    private final String titles;
    private final String description;
    private final String permalink;
    private final String imagelink;

    public Article(String name, String date, String permalink, String imagelink) {
        this.titles = name;
        this.description = date;
        this.permalink = permalink;
        this.imagelink = imagelink;
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

    public String getImagelink() {
        return imagelink;
    }
}
