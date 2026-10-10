package com.example.data.auth

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Entity(tableName = "saved_items")
data class SavedItemEntity(
  @PrimaryKey
  val id: String = UUID.randomUUID().toString(),
  val userId: String = "local_user",
  val title: String,
  val category: String, // RECIPE, IMAGE_PROMPT, JOKE, PRODUCTIVITY, NOTE
  val content: String,
  val imagePrompt: String? = null,
  val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface SavedItemDao {
  @Query("SELECT * FROM saved_items ORDER BY createdAt DESC")
  fun getAllSavedItems(): Flow<List<SavedItemEntity>>

  @Query("SELECT * FROM saved_items WHERE userId = :userId ORDER BY createdAt DESC")
  fun getSavedItemsForUser(userId: String): Flow<List<SavedItemEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertItem(item: SavedItemEntity)

  @Delete
  suspend fun deleteItem(item: SavedItemEntity)

  @Query("DELETE FROM saved_items WHERE id = :id")
  suspend fun deleteById(id: String)
}
