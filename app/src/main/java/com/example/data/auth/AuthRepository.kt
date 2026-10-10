package com.example.data.auth

import android.content.Context
import android.util.Patterns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthRepository(
  private val context: Context,
  private val userDao: UserDao
) {

  private val prefs = context.getSharedPreferences("rik_ai_auth_prefs", Context.MODE_PRIVATE)
  private val _currentUser = MutableStateFlow<UserEntity?>(null)
  val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

  companion object {
    private const val KEY_USER_ID = "logged_in_user_id"
    private const val RESET_CODE_VALIDITY_MS = 15 * 60 * 1000L // 15 minutes
  }

  init {
    // Restore saved session on launch
    val savedUserId = prefs.getString(KEY_USER_ID, null)
    if (!savedUserId.isNullOrEmpty()) {
      CoroutineScope(Dispatchers.IO).launch {
        val user = userDao.getUserByIdOnce(savedUserId)
        _currentUser.value = user
      }
    }
  }

  suspend fun signUp(
    name: String,
    email: String,
    password: String
  ): Result<UserEntity> = withContext(Dispatchers.IO) {
    val trimmedName = name.trim()
    val trimmedEmail = email.trim().lowercase()

    if (trimmedName.isEmpty()) {
      return@withContext Result.failure(IllegalArgumentException("Please enter your name."))
    }
    if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
      return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address."))
    }
    if (password.length < 6) {
      return@withContext Result.failure(IllegalArgumentException("Password must be at least 6 characters long."))
    }

    val existing = userDao.getUserByEmail(trimmedEmail)
    if (existing != null) {
      return@withContext Result.failure(IllegalStateException("An account with this email already exists."))
    }

    val salt = PasswordUtils.generateSalt()
    val passwordHash = PasswordUtils.hashPassword(password, salt)

    val newUser = UserEntity(
      name = trimmedName,
      email = trimmedEmail,
      passwordHash = passwordHash,
      salt = salt
    )

    try {
      userDao.insertUser(newUser)
      saveSession(newUser.id)
      _currentUser.value = newUser
      Result.success(newUser)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun logIn(
    email: String,
    password: String
  ): Result<UserEntity> = withContext(Dispatchers.IO) {
    val trimmedEmail = email.trim().lowercase()

    if (trimmedEmail.isEmpty()) {
      return@withContext Result.failure(IllegalArgumentException("Please enter your email."))
    }
    if (password.isEmpty()) {
      return@withContext Result.failure(IllegalArgumentException("Please enter your password."))
    }

    val user = userDao.getUserByEmail(trimmedEmail)
      ?: return@withContext Result.failure(IllegalArgumentException("No account found with this email."))

    val isValid = PasswordUtils.verifyPassword(password, user.salt, user.passwordHash)
    if (!isValid) {
      return@withContext Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
    }

    saveSession(user.id)
    _currentUser.value = user
    Result.success(user)
  }

  suspend fun logOut(): Unit = withContext(Dispatchers.IO) {
    prefs.edit().remove(KEY_USER_ID).apply()
    _currentUser.value = null
  }

  suspend fun initiatePasswordReset(email: String): Result<String> = withContext(Dispatchers.IO) {
    val trimmedEmail = email.trim().lowercase()
    val user = userDao.getUserByEmail(trimmedEmail)
      ?: return@withContext Result.failure(IllegalArgumentException("No account found with this email."))

    val resetCode = PasswordUtils.generateResetCode()
    val expiry = System.currentTimeMillis() + RESET_CODE_VALIDITY_MS

    val updatedUser = user.copy(
      resetCode = resetCode,
      resetCodeExpiry = expiry
    )
    userDao.updateUser(updatedUser)

    Result.success(resetCode)
  }

  suspend fun completePasswordReset(
    email: String,
    code: String,
    newPassword: String
  ): Result<Unit> = withContext(Dispatchers.IO) {
    val trimmedEmail = email.trim().lowercase()
    val trimmedCode = code.trim()

    if (newPassword.length < 6) {
      return@withContext Result.failure(IllegalArgumentException("Password must be at least 6 characters long."))
    }

    val user = userDao.getUserByEmail(trimmedEmail)
      ?: return@withContext Result.failure(IllegalArgumentException("User not found."))

    if (user.resetCode == null || user.resetCode != trimmedCode) {
      return@withContext Result.failure(IllegalArgumentException("Invalid verification code. Please check and try again."))
    }

    val now = System.currentTimeMillis()
    if (user.resetCodeExpiry == null || now > user.resetCodeExpiry) {
      return@withContext Result.failure(IllegalArgumentException("Verification code has expired. Please request a new one."))
    }

    val newSalt = PasswordUtils.generateSalt()
    val newHash = PasswordUtils.hashPassword(newPassword, newSalt)

    val updatedUser = user.copy(
      passwordHash = newHash,
      salt = newSalt,
      resetCode = null,
      resetCodeExpiry = null
    )
    userDao.updateUser(updatedUser)

    // If current session was this user, update state
    if (_currentUser.value?.id == user.id) {
      _currentUser.value = updatedUser
    }

    Result.success(Unit)
  }

  private fun saveSession(userId: String) {
    prefs.edit().putString(KEY_USER_ID, userId).apply()
  }
}
