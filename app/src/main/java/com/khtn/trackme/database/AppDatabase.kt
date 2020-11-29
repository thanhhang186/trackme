package com.khtn.trackme.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.khtn.trackme.model.Location
import com.khtn.trackme.model.Track

/**
 * Created by NguyenHang on 11/23/2020.
 */

@Database(entities = [Track::class, Location::class], version = AppDatabase.DB_VERSION, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        private const val DB_NAME = "trackme.db"
        const val DB_VERSION = 1

        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase? {
            if (instance == null) {
                synchronized(AppDatabase::class) {
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java, DB_NAME
                    ).build()
                }
            }
            return instance
        }

        fun destroyInstance() {
            if (instance?.isOpen == true) {
                if (instance?.inTransaction() != true) {
                    instance?.close()
                    instance = null
                }
            }
        }
    }

    abstract fun trackDao(): TrackDao
    abstract fun locationDao(): LocationDao
}