package io.github.rbomblauskas.kraitis.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clothing_items")
data class ClothingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val condition: String,
    val priceCents: Long? = null,
    val status: ClothingStatus = ClothingStatus.ACTIVE
)
