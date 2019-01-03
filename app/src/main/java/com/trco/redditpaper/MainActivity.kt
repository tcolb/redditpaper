package com.trco.redditpaper

import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val grabTask = FetchSubredditTask().execute(URL("https://www.reddit.com/r/analog.json"))
        val srJSON = JSONObject(grabTask.get())
        var parseTask = ParseJSONTask().execute(srJSON)

        val tv = findViewById<TextView>(R.id.mainText)
        tv.text = srJSON.toString()

    }

    private class FetchSubredditTask : AsyncTask<URL, Int, String>() {
        val TAG = "FetchSubredditTask"

        override fun onPostExecute(result: String?) {
            Log.v(TAG, "reddit api (to JSON) fetch completed")
            super.onPostExecute(result)
        }

        override fun doInBackground(vararg params: URL?): String {
            val urlConnection: HttpsURLConnection = params[0]?.openConnection() as HttpsURLConnection
            val content: String
            try {
                val inStream: InputStream = BufferedInputStream(urlConnection.inputStream)
                val reader = inStream.bufferedReader().use(BufferedReader::readText)
                content = reader
            } finally {
                urlConnection.disconnect()
            }
            return content
        }
    }

    private class ParseJSONTask: AsyncTask<JSONObject, Int, ArrayList<URL>>() {
        val TAG = "ParseJSONTask"

        override fun onPostExecute(result: ArrayList<URL>?) {
            Log.v(TAG, "finished parsing reddit JSONObject")
            super.onPostExecute(result)
        }

        override fun doInBackground(vararg params: JSONObject): ArrayList<URL> {
            val imgURLs = arrayListOf<URL>()

            val data = params[0].getJSONObject("data")
            val posts = data.getJSONArray("children")
            for (i in 1..(posts.length()-1)) {
                val post: JSONObject = posts.get(i) as JSONObject
                val postData = post.getJSONObject("data")
                val url = URL(postData.getString("url"))
                Log.v(TAG, "got url: " + url.toString())
                imgURLs.add(url)
            }
            return imgURLs
        }
    }
}
