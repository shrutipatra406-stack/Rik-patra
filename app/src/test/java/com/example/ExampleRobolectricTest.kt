package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ChatMessage
import com.example.data.auth.PasswordUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("RIK AI", appName)
  }

  @Test
  fun `test password hashing and verification`() {
    val password = "StrongPassword123!"
    val salt = PasswordUtils.generateSalt()
    val hash = PasswordUtils.hashPassword(password, salt)

    assertTrue(PasswordUtils.verifyPassword(password, salt, hash))
    assertTrue(!PasswordUtils.verifyPassword("WrongPassword", salt, hash))
  }

  @Test
  fun `test image prompt extraction`() {
    val messageWithPrompt = "Here is your requested image prompt:\n[IMAGE_PROMPT: A futuristic neon skyline of Kolkata at sunset]\nEnjoy generating it!"
    val extracted = ChatMessage.extractImagePrompt(messageWithPrompt)
    assertNotNull(extracted)
    assertEquals("A futuristic neon skyline of Kolkata at sunset", extracted)
  }
}
