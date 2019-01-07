package com.trco.redditpaper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStream
import java.lang.Exception
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class FetchSubredditTask : AsyncTask<URL, Int, String>() {
    val TAG = "FetchSubredditTask"

    override fun onPostExecute(result: String?) {
        Log.v(TAG, "reddit api (to JSON) fetch completed")
        super.onPostExecute(result)
    }

    override fun doInBackground(vararg params: URL?): String {
        val urlConnection: HttpsURLConnection = params[0]?.openConnection() as HttpsURLConnection
        val content: String
        try {
            val inStream: InputStream = BufferedInputStream(urlConnection.inputStream) as InputStream
            val reader = inStream.bufferedReader().use(BufferedReader::readText)
            content = reader
        } finally {
            urlConnection.disconnect()
        }
        return content
    }
}

class ParseJSONTask: AsyncTask<JSONObject, Int, ArrayList<String>>() {
    val TAG = "ParseJSONTask"

    override fun onPostExecute(result: ArrayList<String>?) {
        Log.v(TAG, "finished parsing reddit JSONObject")
        super.onPostExecute(result)
    }

    override fun doInBackground(vararg params: JSONObject): ArrayList<String> {
        val imgURLs = arrayListOf<String>()

        val data = params[0].getJSONObject("data")
        val posts = data.getJSONArray("children")
        for (i in 1..(posts.length()-1)) {
            val post: JSONObject = posts.get(i) as JSONObject
            val postData = post.getJSONObject("data")
            val strUrl = postData.getString("url")
            Log.v(TAG, "got url: " + strUrl)
            imgURLs.add(strUrl)
        }
        return imgURLs
    }
}

class UrlBitmapTask: AsyncTask<URL, Int, Bitmap>() {
    val TAG = "UrlBitmapTask"

    override fun onPostExecute(result: Bitmap?) {
        Log.v(TAG, "finished decoding bitmap from url")
        super.onPostExecute(result)
    }

    override fun doInBackground(vararg params: URL?): Bitmap {
        lateinit var bitmap: Bitmap
        val urlConnection: HttpsURLConnection = params[0]?.openConnection() as HttpsURLConnection
        try {
            val inStream: InputStream = BufferedInputStream(urlConnection.inputStream)
            bitmap = BitmapFactory.decodeStream(inStream)
        } catch (e: Exception){
            Log.e(TAG, e.toString())
        } finally {
            urlConnection.disconnect()
        }
        return bitmap
    }
}