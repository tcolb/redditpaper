package com.trco.redditpaper

import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
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

        var tv = findViewById<TextView>(R.id.mainText)

        var grabTask = FetchSubredditTask().execute(URL("https://www.reddit.com/r/analog.json"))
        tv.text = grabTask.get()
    }

    private class FetchSubredditTask : AsyncTask<URL, Int, String>() {
        val TAG = "FetchSubredditTask"

        override fun onPostExecute(result: String?) {
            Log.i(TAG, "reddit api (to JSON) call completed\n" + result)
            super.onPostExecute(result)
        }

        override fun doInBackground(vararg params: URL?): String {
            var urlConnection: HttpsURLConnection = params[0]?.openConnection() as HttpsURLConnection
            var content: String
            try {
                var inStream: InputStream = BufferedInputStream(urlConnection.inputStream)
                var reader = inStream.bufferedReader().use(BufferedReader::readText)
                content = reader
            } finally {
                urlConnection.disconnect()
            }
            return content
        }
    }
}
