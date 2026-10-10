package com.example.data.auth

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
  tableName = "users",
  indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
  @PrimaryKey
  val id: String = UUID.randomUUID().toString(),
  val name: String,
  val email: String,
  val passwordHash: String,
  val salt: String,
  val createdAt: Long = System.currentTimeMillis(),
  val resetCode: String? = null,
  val resetCodeExpiry: Long? = null
)
