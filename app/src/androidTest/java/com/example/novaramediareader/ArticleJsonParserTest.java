package com.example.novaramediareader;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ArticleJsonParserTest {
    @Test
    public void testAddItemsFromJSON() {
        List<Articles> articlesList = ArticleJsonParser.addItemsFromJSON(InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().openRawResource(R.raw.articles));

        assertEquals("Article count should be 5", 10, articlesList.size());
    }
}
