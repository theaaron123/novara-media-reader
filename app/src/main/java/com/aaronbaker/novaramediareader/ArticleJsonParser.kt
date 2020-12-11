package com.aaronbaker.novaramediareader

import android.text.Html
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

object ArticleJsonParser {
    @JvmStatic
    fun addItemsFromJSONArticle(inputStream: InputStream?): List<Article>? {
        var jsonDataString: String? = null
        try {
            jsonDataString = readJSONDataFromFile(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return addItemsFromJSONArticle(jsonDataString)
    }

    @JvmStatic
    fun addItemsFromJSONArticle(jsonDataString: String?): List<Article>? {
        try {
            val jsonObject = JSONObject(jsonDataString)
            val jsonArray = jsonObject.getJSONArray("posts")
            val articleList: MutableList<Article> = ArrayList()
            for (i in 0 until jsonArray.length()) {
                val itemObj = jsonArray.getJSONObject(i)
                val name = itemObj.getString("title")
                val shortDesc = itemObj.getString("short_desc")
                val permalink = itemObj.getString("permalink")
                val imageLink = itemObj.getString("thumb_medium")
                articleList.add(Article(name, stripHTML(shortDesc), permalink, imageLink, null))
            }
            return articleList
        } catch (e: JSONException) {
            Log.d(ArticleListFragment::class.java.name, "addItemsFromJSON: ", e)
        }
        return null
    }

    @JvmStatic
    fun addItemsFromJSONSearch(searchResponse: String?): List<Article> {
        val articleList: MutableList<Article> = ArrayList()
        try {
            val jsonArray = JSONArray(searchResponse)
            for (i in 0 until jsonArray.length()) {
                val itemObj = jsonArray.getJSONObject(i)
                val name = itemObj.getJSONObject("title").getString("rendered")
                val shortDesc = itemObj.getJSONObject("excerpt").getString("rendered")
                val permalink = itemObj.getString("link")
                val imageLink = itemObj.getString("featured_media")
                articleList.add(Article(stripHTML(name), stripHTML(shortDesc), permalink, imageLink, null))
            }
            return articleList
        } catch (e: JSONException) {
            Log.d(ArticleListFragment::class.java.name, "addItemsFromJSONSearch: ", e)
        }
        return articleList
    }

    @JvmStatic
    fun parseImageUrlFromJSON(mediaJSON: String?): String {
        try {
            return JSONObject(mediaJSON).getJSONObject("media_details").getJSONObject("sizes").getJSONObject("medium").getString("source_url")
        } catch (e: JSONException) {
            Log.d(ArticleListFragment::class.java.name, "parseImageUrlFromJSON: ", e)
        }
        return ""
    }

    @JvmStatic
    @Throws(IOException::class)
    fun readJSONDataFromFile(inputStream: InputStream?): String {
        val builder = StringBuilder()
        inputStream.use { _ ->
            var jsonString: String?
            val bufferedReader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            while (bufferedReader.readLine().also { jsonString = it } != null) {
                builder.append(jsonString)
            }
        }
        return String(builder)
    }

    private fun stripHTML(html: String): String {
        return Html.fromHtml(html).toString().replace("\n".toRegex(), "").trim { it <= ' ' }
    }
}