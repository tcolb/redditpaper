package com.trco.redditpaper

import android.app.WallpaperManager
import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.room.Room
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.json.JSONObject
import java.net.URL

//TODO error handling and post notifications of errors / fetching
class PaperWorker(context: Context, params: WorkerParameters): Worker(context, params) {
    val TAG = "PaperWorker"

    override fun doWork(): Result {
        val prefs = getDefaultSharedPreferences(applicationContext)
        val wpManager = applicationContext.getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "user-database"
        ).build()
        val dbDao = db.postDao()

        val subredditUrl: String =
                applicationContext.getString(R.string.reddit_url) +
                        prefs.getString("setting_subreddit_name", "pics") + "/" +
                        prefs.getString("setting_post_ordering", "hot") + ".json?limit=" +
                        prefs.getString("setting_number_posts_fetched", "25")

        Log.v(TAG, "num posts in db: " + dbDao.numPosts())
        //TODO while or if?
        while (dbDao.numPosts() == 0) {
            val fetchTask = FetchSubredditTask().execute(URL(subredditUrl))
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
            Log.e(TAG, "failed creating bitmap, deleting url from db and continuing")
            dbDao.deletePost(fetchedPost)
            db.close()
            return Result.retry()
        }

        // set wallpaper as next url
        if (bmpTask.get().result == null || bmpTask.get().error != null) {
            Log.e(TAG, "failed setting wall paper with bitmap, is invalid, probably a non image link")
            dbDao.deletePost(fetchedPost)
            db.close()
            return Result.retry()
        }

        wpManager.setBitmap(bmpTask.get().result)
        dbDao.deletePost(fetchedPost)
        db.close()

        return Result.success()
    }
}

