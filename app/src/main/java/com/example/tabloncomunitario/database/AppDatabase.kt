package com.example.tabloncomunitario.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tabloncomunitario.User
import com.example.tabloncomunitario.Announcement
import com.example.tabloncomunitario.Comment

@Database(entities = [User::class, Announcement::class, Comment::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun commentDao(): CommentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tablon_comunitario_db"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}