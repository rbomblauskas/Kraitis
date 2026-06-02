package io.github.rbomblauskas.kraitis.data

import androidx.room.TypeConverter

class KraitisTypeConverters {
    @TypeConverter
    fun statusToString(status: ClothingStatus): String = status.name

    @TypeConverter
    fun stringToStatus(value: String): ClothingStatus = ClothingStatus.valueOf(value)

    @TypeConverter
    fun categoryToString(category: ClothingCategory): String = category.name

    // old rows can still have free text from before the enums
    @TypeConverter
    fun stringToCategory(value: String): ClothingCategory =
        ClothingCategory.entries.firstOrNull { it.name == value } ?: ClothingCategory.OTHER

    @TypeConverter
    fun conditionToString(condition: ClothingCondition): String = condition.name

    @TypeConverter
    fun stringToCondition(value: String): ClothingCondition =
        ClothingCondition.entries.firstOrNull { it.name == value } ?: ClothingCondition.GOOD
}
