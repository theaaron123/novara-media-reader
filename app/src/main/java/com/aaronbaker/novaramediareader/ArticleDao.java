package com.aaronbaker.novaramediareader;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ArticleDao {

    @Query("SELECT * FROM article")
    List<Article> getAll();

    @Query("SELECT * FROM article WHERE title LIKE :title " + "LIMIT 1")
    Article findByTitle(String title);

    @Insert
    void insertAll(Article... articles);

    @Insert
    long insertArticle(Article article);

    @Delete
    void delete(Article article);

    @Query("DELETE FROM article WHERE title LIKE :title ")
    void deleteByTitle(String title);
}
