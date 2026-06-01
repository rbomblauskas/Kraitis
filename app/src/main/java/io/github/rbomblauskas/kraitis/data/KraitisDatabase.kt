package io.github.rbomblauskas.kraitis.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ClothingItem::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(KraitisTypeConverters::class)
abstract class KraitisDatabase : RoomDatabase() {
    abstract fun clothingDao(): ClothingDao

    companion object {
        @Volatile
        private var instance: KraitisDatabase? = null

        fun getDatabase(context: Context): KraitisDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    KraitisDatabase::class.java,
                    "kraitis.db"
                ).build().also { instance = it }
            }
        }
    }
}
