package com.shayanhaque.politicstracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.shayanhaque.politicstracker.data.local.entity.FavoriteEntity
import com.shayanhaque.politicstracker.data.local.entity.MarketEntity
import com.shayanhaque.politicstracker.data.local.entity.PricePointEntity

@Database(
    entities = [MarketEntity::class, FavoriteEntity::class, PricePointEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class MarketDatabase : RoomDatabase() {
    abstract fun marketDao(): MarketDao

    companion object {
        private const val DB_NAME = "politics_tracker.db"

        fun create(context: Context): MarketDatabase =
            Room.databaseBuilder(context.applicationContext, MarketDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration()
                .build()
    }
}
