package com.example.data

import java.util.UUID

enum class SavedType {
  IMAGE_PROMPT,
  RECIPE,
  JOKE,
  PRODUCTIVITY,
  NOTE
}

enum class LanguageMode(val displayName: String, val nativeName: String, val flag: String) {
  AUTO("Auto / All", "স্বয়ংক্রিয়", "🌐"),
  ENGLISH("English", "English", "🇬🇧"),
  BENGALI("Bengali", "বাংলা", "🇧🇩"),
  HINDI("Hindi", "हिंदी", "🇮🇳")
}

data class ChatMessage(
  val id: String = UUID.randomUUID().toString(),
  val role: MessageRole,
  val content: String,
  val timestamp: Long = System.currentTimeMillis(),
  val isError: Boolean = false,
  val imagePrompt: String? = extractImagePrompt(content),
  val isStreaming: Boolean = false,
  val imageBitmap: android.graphics.Bitmap? = null
) {
  val isUser: Boolean get() = role == MessageRole.USER

  companion object {
    fun extractImagePrompt(text: String): String? {
      val regex = Regex("""\[IMAGE_PROMPT:\s*(.*?)]""", RegexOption.DOT_MATCHES_ALL)
      val match = regex.find(text)
      return match?.groupValues?.getOrNull(1)?.trim()
    }
  }
}

enum class MessageRole {
  USER,
  MODEL
}

data class SavedItem(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val type: SavedType,
  val content: String,
  val imagePrompt: String? = null,
  val timestamp: Long = System.currentTimeMillis()
)

data class PromptSuggestion(
  val title: String,
  val prompt: String,
  val language: LanguageMode,
  val iconEmoji: String,
  val category: String
)
