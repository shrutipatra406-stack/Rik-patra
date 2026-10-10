package com.example.data.auth

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

  @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
  suspend fun getUserByEmail(email: String): UserEntity?

  @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
  fun getUserById(id: String): Flow<UserEntity?>

  @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
  suspend fun getUserByIdOnce(id: String): UserEntity?

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insertUser(user: UserEntity)

  @Update
  suspend fun updateUser(user: UserEntity)

  @Delete
  suspend fun deleteUser(user: UserEntity)

  @Query("SELECT COUNT(*) FROM users")
  suspend fun getUserCount(): Int
}
