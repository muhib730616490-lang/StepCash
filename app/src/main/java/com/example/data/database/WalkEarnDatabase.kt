package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        User::class,
        StepRecord::class,
        TransactionRecord::class,
        TaskRecord::class,
        ShopItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WalkEarnDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun stepDao(): StepDao
    abstract fun transactionDao(): TransactionDao
    abstract fun taskDao(): TaskDao
    abstract fun shopDao(): ShopDao

    companion object {
        @Volatile
        private var INSTANCE: WalkEarnDatabase? = null

        fun getDatabase(context: Context): WalkEarnDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WalkEarnDatabase::class.java,
                    "walk_earn_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
