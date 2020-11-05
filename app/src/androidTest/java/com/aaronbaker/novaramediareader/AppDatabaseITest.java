package com.aaronbaker.novaramediareader;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;

@RunWith(AndroidJUnit4.class)
public class AppDatabaseITest {

    private AppDatabase db;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void writeArticleAndReadByTitle() {
        Article article = new Article();
        article.setTitle("Title article");
        db.articleDao().insertArticle(article);
        Article savedArticle = db.articleDao().findByTitle("Title article");
        Assert.assertThat(savedArticle.getTitle(), equalTo(article.getTitle()));
    }
}
