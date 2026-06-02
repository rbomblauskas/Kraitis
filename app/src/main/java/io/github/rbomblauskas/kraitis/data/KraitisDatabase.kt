package io.github.rbomblauskas.kraitis.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ClothingItem::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(KraitisTypeConverters::class)
abstract class KraitisDatabase : RoomDatabase() {
    abstract fun clothingDao(): ClothingDao

    companion object {
        @Volatile
        private var instance: KraitisDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE clothing_items ADD COLUMN photoPath TEXT")
            }
        }

        fun getDatabase(context: Context): KraitisDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    KraitisDatabase::class.java,
                    "kraitis.db"
                ).addMigrations(MIGRATION_1_2)
                    .build().also { instance = it }
            }
        }
    }
}
