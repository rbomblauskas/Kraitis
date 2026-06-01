package io.github.rbomblauskas.kraitis.data

import androidx.room.TypeConverter

class KraitisTypeConverters {
    @TypeConverter
    fun statusToString(status: ClothingStatus): String = status.name

    @TypeConverter
    fun stringToStatus(value: String): ClothingStatus = ClothingStatus.valueOf(value)
}
