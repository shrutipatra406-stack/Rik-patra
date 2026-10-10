package com.example.data.auth

import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.random.Random

object PasswordUtils {

  private val secureRandom = SecureRandom()

  fun generateSalt(): String {
    val bytes = ByteArray(16)
    secureRandom.nextBytes(bytes)
    return bytes.joinToString("") { "%02x".format(it) }
  }

  fun hashPassword(password: String, salt: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val saltedInput = "$password:$salt"
    val digest = md.digest(saltedInput.toByteArray(Charsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
  }

  fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
    val computed = hashPassword(password, salt)
    return computed == expectedHash
  }

  fun generateResetCode(): String {
    val code = Random.nextInt(100000, 999999)
    return code.toString()
  }
}
