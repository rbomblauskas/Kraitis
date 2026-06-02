package io.github.rbomblauskas.kraitis.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothingDao {
    @Query("SELECT * FROM clothing_items ORDER BY id DESC")
    fun getAllItems(): Flow<List<ClothingItem>>

    @Query("SELECT * FROM clothing_items WHERE id = :id")
    fun getItem(id: Long): Flow<ClothingItem?>

    @Insert
    suspend fun insert(item: ClothingItem): Long

    @Update
    suspend fun update(item: ClothingItem)

    @Query("UPDATE clothing_items SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: ClothingStatus)

    @Query("UPDATE clothing_items SET photoPath = :photoPath WHERE id = :id")
    suspend fun updatePhoto(id: Long, photoPath: String?)
}
