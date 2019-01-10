package com.trco.redditpaper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.Exception


class AsyncTaskResult<T> {
    var error: Exception? = null
    var result: T? = null

    constructor(e: Exception) {
        this.error = e
    }

    constructor(res: T?) {
        this.result = res
    }
}

class FetchSubredditTask : AsyncTask<URL, Int, AsyncTaskResult<String>>() {
    val TAG = "FetchSubredditTask"

    override fun onPostExecute(result: AsyncTaskResult<String>?) {
        Log.v(TAG, "reddit api (to JSON) fetch completed")
        super.onPostExecute(result)
    }

    override fun doInBackground(vararg params: URL?): AsyncTaskResult<String> {
        var result: AsyncTaskResult<String>
        Log.v(TAG,"fetching posts from subreddit url: " + params[0])
        val urlConnection: HttpsURLConnection = params[0]?.openConnection() as HttpsURLConnection
        try {
            val inStream: InputStream = BufferedInputStream(urlConnection.inputStream) as InputStream
            val reader = inStream.bufferedReader().use(BufferedReader::readText)
            result = AsyncTaskResult(reader)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            result = AsyncTaskResult(e)
        } finally {
            urlConnection.disconnect()
        }
        return result
    }
}

class ParseJSONTask: AsyncTask<JSONObject, Int, AsyncTaskResult<ArrayList<String>>>() {
    val TAG = "ParseJSONTask"

    override fun onPostExecute(result: AsyncTaskResult<ArrayList<String>>) {
        Log.v(TAG, "finished parsing reddit JSONObject")
        super.onPostExecute(result)
    }

    override fun doInBackground(vararg params: JSONObject): AsyncTaskResult<ArrayList<String>> {
        //TODO only get image urls, not text
        var result: AsyncTaskResult<ArrayList<String>>
        val imgURLs = arrayListOf<String>()

        try {
            val data = params[0].getJSONObject("data")
            val posts = data.getJSONArray("children")
            for (i in 1..(posts.length() - 1)) {
                val post: JSONObject = posts.get(i) as JSONObject
                val postData = post.getJSONObject("data")
                val strUrl = postData.getString("url")
                Log.v(TAG, "got url: " + strUrl)
                imgURLs.add(strUrl)
            }
            result = AsyncTaskResult(imgURLs)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            result = AsyncTaskResult(e)
        }

        return result
    }
}

class UrlBitmapTask: AsyncTask<URL, Int, AsyncTaskResult<Bitmap>>() {
    val TAG = "UrlBitmapTask"

    override fun onPostExecute(result: AsyncTaskResult<Bitmap>) {
        Log.v(TAG, "finished decoding bitmap from url")
        super.onPostExecute(result)
    }

    override fun doInBackground(vararg params: URL?): AsyncTaskResult<Bitmap> {
        //TODO check for https , will throw cast exception otherwise
        var result: AsyncTaskResult<Bitmap>
        val urlConnection: HttpsURLConnection = params[0]?.openConnection() as HttpsURLConnection
        try {
            val inStream: InputStream = BufferedInputStream(urlConnection.inputStream)
            result = AsyncTaskResult(BitmapFactory.decodeStream(inStream))
        } catch (e: Exception){
            Log.e(TAG, e.toString())
            result = AsyncTaskResult(e)
        } finally {
            urlConnection.disconnect()
        }

        return result
    }
}