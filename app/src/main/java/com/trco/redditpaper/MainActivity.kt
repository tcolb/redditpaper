package com.trco.redditpaper

import android.app.DownloadManager
import android.app.WallpaperManager
import android.content.Context
import android.content.res.AssetManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.work.*
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // workmanager setting up recurring work
        val wallpaperCycleConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val wallpaperCycleBuild =
            PeriodicWorkRequestBuilder<PaperWorker>(15, TimeUnit.MINUTES)
                .setConstraints(wallpaperCycleConstraints)
                .addTag("paperCycle")
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(
            "RedditPaperCycle",
            ExistingPeriodicWorkPolicy.KEEP,
            wallpaperCycleBuild)

        //val dlManager: DownloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val wpManager: WallpaperManager = getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager


        val grabTask = FetchSubredditTask().execute(URL("https://www.reddit.com/r/analog.json"))
        val srJSON = JSONObject(grabTask.get())
        var parseTask = ParseJSONTask().execute(srJSON)
        val bmpTask = UrlBitmapTask().execute(URL("https://i.redd.it/l8mareg8l8821.jpg"))


        /*
        var dlReq = DownloadManager.Request(Uri.parse("https://i.redd.it/l8mareg8l8821.jpg"))
        dlReq.setTitle("Example2!")
        dlReq.setDescription("Downloading")
        dlReq.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        dlReq.setVisibleInDownloadsUi(true)
        dlReq.setDestinationUri("")
        dlManager.enqueue(dlReq)


        for (url in parseTask.get()) {
            var uri = Uri.parse(url.toURI().toString())
            dlManager.enqueue(DownloadManager.Request(uri))
        }
        */

        //val tv = findViewById<TextView>(R.id.testText)
        //tv.text = srJSON.toString()
        val iv = findViewById<ImageView>(R.id.testImage)
        iv.setImageBitmap(bmpTask.get())

        //wpManager.setBitmap(bmpTask.get())
    }
}
