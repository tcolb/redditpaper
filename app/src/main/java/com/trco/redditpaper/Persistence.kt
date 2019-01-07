package com.trco.redditpaper

import androidx.annotation.NonNull
import androidx.room.*
import java.net.URL

@Entity(tableName = "sub_r_posts")
data class Post(var url: String) {
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid")
    var uid: Int = 0
    @ColumnInfo(name = "post_url")
    var postUrl: String = url
    //TODO add more fields for robustness
}

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertPost(vararg posts: Post)

    @Delete
    fun deletePost(vararg posts: Post)

    @Query("SELECT * FROM sub_r_posts")
    fun getAllPosts(): Array<Post>

    @Query("SELECT * FROM sub_r_posts ORDER BY `rowid` LIMIT 1")
    fun getNextPost(): Post
    // TODO check if returning just Post works

    @Query("SELECT COUNT(*) FROM sub_r_posts")
    fun numPosts(): Int
}

@Database( entities = arrayOf(Post::class), version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun postDao(): PostDao
}