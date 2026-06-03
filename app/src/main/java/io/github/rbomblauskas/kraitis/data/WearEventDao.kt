package io.github.rbomblauskas.kraitis.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WearEventDao {
    @Query("SELECT * FROM wear_events ORDER BY wornAt DESC")
    fun getAllEvents(): Flow<List<WearEvent>>

    @Insert
    suspend fun insert(event: WearEvent): Long
}
