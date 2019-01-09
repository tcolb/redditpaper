package com.trco.redditpaper

import android.app.DownloadManager
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
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
        setSupportActionBar(findViewById(R.id.parent_toolbar))


        // WorkManager setting up recurring work
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

        // testing button
        val button = findViewById<Button>(R.id.test_button)
        button.setOnClickListener {
            val wallpaperNextBuild =
                    OneTimeWorkRequestBuilder<PaperWorker>()
                        .setConstraints(wallpaperCycleConstraints)
                        .addTag("paperSingle")
                        .build()
            WorkManager.getInstance().enqueueUniqueWork(
                "RedditPaperCycle_user_request",
                ExistingWorkPolicy.KEEP,
                wallpaperNextBuild)
            button.text = "Clicked!"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_buttons, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when(item?.itemId) {
        R.id.action_settings -> {
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingsIntent)
            true
        } else -> {
            super.onOptionsItemSelected(item)
        }
    }
}
