package com.aaronbaker.novaramediareader;

import android.text.Html;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ArticleJsonParser {
    static List<Article> addItemsFromJSONArticle(InputStream inputStream) {
        String jsonDataString = null;
        try {
            jsonDataString = readJSONDataFromFile(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addItemsFromJSONArticle(jsonDataString);
    }

    static List<Article> addItemsFromJSONArticle(String jsonDataString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonDataString);
            JSONArray jsonArray = jsonObject.getJSONArray("posts");
            List<Article> articleList = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject itemObj = jsonArray.getJSONObject(i);

                String name = itemObj.getString("title");
                String shortDesc = itemObj.getString("short_desc");
                String permalink = itemObj.getString("permalink");
                String imageLink = itemObj.getString("thumb_medium");

                articleList.add(new Article(name, stripHTML(shortDesc), permalink, imageLink, ""));
            }
            return articleList;
        } catch (JSONException e) {
            Log.d(ArticleListFragment.class.getName(), "addItemsFromJSON: ", e);
        }
        return null;
    }

    static List<Article> addItemsFromJSONSearch(String searchResponse) {
        List<Article> articleList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(searchResponse);

            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject itemObj = jsonArray.getJSONObject(i);

                String name = itemObj.getJSONObject("title").getString("rendered");
                String shortDesc = itemObj.getJSONObject("excerpt").getString("rendered");
                String permalink = itemObj.getString("link");
                String imageLink = itemObj.getString("featured_media");

                articleList.add(new Article(stripHTML(name), stripHTML(shortDesc), permalink, imageLink, ""));
            }
            return articleList;
        } catch (JSONException e) {
            Log.d(ArticleListFragment.class.getName(), "addItemsFromJSONSearch: ", e);
        }
        return articleList;
    }

    static String parseImageUrlFromJSON(String mediaJSON) {
        try {
            return new JSONObject(mediaJSON).getJSONObject("media_details").getJSONObject("sizes").getJSONObject("medium").getString("source_url");
        } catch (JSONException e) {
            Log.d(ArticleListFragment.class.getName(), "parseImageUrlFromJSON: ", e);
        }
        return "";
    }

     static String readJSONDataFromFile(InputStream inputStream) throws IOException {
         StringBuilder builder = new StringBuilder();

         try {
             String jsonString;
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

             while ((jsonString = bufferedReader.readLine()) != null) {
                 builder.append(jsonString);
             }
         } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return new String(builder);
    }

    private static String stripHTML(String html) {
        return Html.fromHtml(html).toString().replaceAll("\n", "").trim();
    }
}
