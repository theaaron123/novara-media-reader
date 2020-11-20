package com.aaronbaker.novaramediareader

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Article::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao?

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        @JvmStatic
        fun getDatabaseInstance(context: Context): AppDatabase? {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "article-db"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
