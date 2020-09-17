package com.example.novaramediareader;

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
import java.util.List;

public class ArticleJsonParser {
    static List<Articles> addItemsFromJSON(InputStream inputStream) {
        String jsonDataString = null;
        try {
            jsonDataString = readJSONDataFromFile(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addItemsFromJSON(jsonDataString);
    }

    static List<Articles> addItemsFromJSON(String jsonDataString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonDataString);
            JSONArray jsonArray = jsonObject.getJSONArray("posts");
            List<Articles> articlesList = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject itemObj = jsonArray.getJSONObject(i);

                String name = itemObj.getString("title");
                String shortDesc = itemObj.getString("short_desc");

                articlesList.add(new Articles(name, Html.fromHtml(shortDesc).toString().replaceAll("\n", "").trim()));
            }
            return articlesList;
        } catch (JSONException e) {
            Log.d(MainActivity.class.getName(), "addItemsFromJSON: ", e);
        }
        return null;
    }

    private static String readJSONDataFromFile(InputStream inputStream) throws IOException {
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
}
