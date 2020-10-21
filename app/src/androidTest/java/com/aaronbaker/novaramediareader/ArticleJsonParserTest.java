package com.aaronbaker.novaramediareader;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ArticleJsonParserTest {
    @Test
    public void testAddItemsFromJSON() {
        List<Article> articleList = ArticleJsonParser.addItemsFromJSONArticle(InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().openRawResource(R.raw.articles));

        assertEquals("Article count should be 10", 10, articleList.size());
    }

    @Test
    public void testAddItemsFromJSONSearch() throws IOException {
        List<Article> articleList = ArticleJsonParser.addItemsFromJSONSearch(ArticleJsonParser.readJSONDataFromFile(InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().openRawResource(R.raw.search)));

        assertEquals("Article count should be 10", 10, articleList.size());
    }

    @Test
    public void testParseImageUrlFromJSON() throws IOException {
        String imgUrl = ArticleJsonParser.parseImageUrlFromJSON(ArticleJsonParser.readJSONDataFromFile(InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().openRawResource(R.raw.media)));

        assertEquals("URL should be: ", "https://novaramedia.com/wp-content/uploads/2020/10/2020-10-19T052207Z_803280629_RC2HLJ9J0FER_RTRMADP_3_BOLIVIA-ELECTION-1-864x576.jpg", imgUrl);
    }
}
