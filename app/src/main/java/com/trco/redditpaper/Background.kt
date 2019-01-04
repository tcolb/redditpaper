package com.trco.redditpaper

import android.app.WallpaperManager
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.net.URL

class PaperWorker(context: Context, params: WorkerParameters): Worker(context, params) {
    override fun doWork(): Result {
        val wpManager: WallpaperManager = applicationContext.getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager

        // get bitmap of next url in queue
        val bmpTask = UrlBitmapTask().execute(URL("https://i.redd.it/l8mareg8l8821.jpg"))
        wpManager.setBitmap(bmpTask.get())

        return Result.success()
    }
}