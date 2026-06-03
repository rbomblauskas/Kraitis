package io.github.rbomblauskas.kraitis.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wear_events")
data class WearEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: Long,
    val wornAt: Long
)
