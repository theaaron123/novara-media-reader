package com.aaronbaker.novaramediareader;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Article {

    @PrimaryKey(autoGenerate = true)
    public long uid;
    @ColumnInfo(name = "title")
    private String title;
    @ColumnInfo(name = "description")
    private String description;
    @ColumnInfo(name = "permalink")
    private String permalink;
    @ColumnInfo(name = "image")
    private String image;

    public Article(String name, String date, String permalink, String image) {
        this.title = name;
        this.description = date;
        this.permalink = permalink;
        this.image = image;
    }

    public Article() {
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPermalink() {
        return permalink;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }
}
