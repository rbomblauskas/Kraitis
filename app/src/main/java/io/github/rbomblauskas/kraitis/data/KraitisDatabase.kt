package io.github.rbomblauskas.kraitis.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ClothingItem::class, WearEvent::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(KraitisTypeConverters::class)
abstract class KraitisDatabase : RoomDatabase() {
    abstract fun clothingDao(): ClothingDao

    abstract fun wearEventDao(): WearEventDao

    companion object {
        @Volatile
        private var instance: KraitisDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE clothing_items ADD COLUMN photoPath TEXT")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `wear_events` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`itemId` INTEGER NOT NULL, " +
                        "`wornAt` INTEGER NOT NULL)"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE clothing_items ADD COLUMN season TEXT NOT NULL DEFAULT 'ALL_YEAR'")
                db.execSQL("ALTER TABLE clothing_items ADD COLUMN sentimental INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE clothing_items ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): KraitisDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    KraitisDatabase::class.java,
                    "kraitis.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build().also { instance = it }
            }
        }
    }
}
