package com.aaronbaker.novaramediareader

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ArticleDao {

    @Query("SELECT * FROM article")
    fun getAll(): List<Article?>?

    @Query("SELECT * FROM article WHERE title LIKE :title " + "LIMIT 1")
    fun findByTitle(title: String?): Article?

    @Insert
    fun insertAll(vararg articles: Article?)

    @Insert
    fun insertArticle(article: Article?): Long

    @Delete
    fun delete(article: Article?)

    @Query("DELETE FROM article WHERE title LIKE :title ")
    fun deleteByTitle(title: String?)
}