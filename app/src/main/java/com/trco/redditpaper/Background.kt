package com.trco.redditpaper

import android.app.WallpaperManager
import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.json.JSONObject
import java.net.URL

//TODO error handling and post notifications of errors / fetching
class PaperWorker(context: Context, params: WorkerParameters): Worker(context, params) {
    val TAG = "PaperWorker"

    override fun doWork(): Result {
        val wpManager: WallpaperManager = applicationContext.getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "user-database"
        ).build()
        val dbDao = db.postDao()
        Log.v(TAG, "num posts in db: " + dbDao.numPosts())
        //TODO while or if?
        while (dbDao.numPosts() == 0) {
            val fetchTask = FetchSubredditTask().execute(URL("https://www.reddit.com/r/analog/top.json"))
            val parseJSONTask = ParseJSONTask().execute(JSONObject(fetchTask.get().result))
            if (fetchTask.get().error != null || parseJSONTask.get().error != null) {
                // parsing json failed
                db.close()
                return Result.failure()
            }
            parseJSONTask.get().result?.forEach {
                val newPost = Post(it)
                dbDao.insertPost(newPost)
                Log.v(TAG, "inserted post url into database, url: $it")
            }
        }

        // get bitmap of next url in queue
        val fetchedPost = dbDao.getNextPost()
        Log.v(TAG, "accessed post from database, url: $" + fetchedPost.postUrl)
        val bmpTask = UrlBitmapTask().execute(URL(fetchedPost.postUrl))
        if (bmpTask.get().error != null) {
            // creating bitmap failed
            db.close()
            return Result.failure()
        }
        // set wallpaper as next url
        wpManager.setBitmap(bmpTask.get().result)
        dbDao.deletePost(fetchedPost)
        db.close()

        return Result.success()
    }
}

