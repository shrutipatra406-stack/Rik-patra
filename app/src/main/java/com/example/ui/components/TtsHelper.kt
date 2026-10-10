package com.example.ui.components

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsHelper(context: Context) : TextToSpeech.OnInitListener {

  private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
  private var isInitialized = false

  private val _isPlayingId = MutableStateFlow<String?>(null)
  val isPlayingId: StateFlow<String?> = _isPlayingId.asStateFlow()

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      isInitialized = true
      tts?.language = Locale.ENGLISH
    } else {
      Log.e("TtsHelper", "Failed to initialize TTS")
    }
  }

  fun speak(messageId: String, text: String) {
    if (!isInitialized || tts == null) return

    if (_isPlayingId.value == messageId) {
      stop()
      return
    }

    // Strip image prompt tags or markdown headers for cleaner speech
    val speechText = text
      .replace(Regex("""\[IMAGE_PROMPT:.*?\]"""), "Image prompt generated.")
      .replace(Regex("""[*#_`>~]"""), "")

    // Detect language script
    val locale = when {
      speechText.any { it in '\u0980'..'\u09FF' } -> Locale.forLanguageTag("bn-IN")
      speechText.any { it in '\u0900'..'\u097F' } -> Locale.forLanguageTag("hi-IN")
      else -> Locale.ENGLISH
    }

    tts?.language = locale
    _isPlayingId.value = messageId

    tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
      override fun onStart(utteranceId: String?) {}
      override fun onDone(utteranceId: String?) {
        _isPlayingId.value = null
      }
      override fun onError(utteranceId: String?) {
        _isPlayingId.value = null
      }
    })

    tts?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, messageId)
  }

  fun stop() {
    tts?.stop()
    _isPlayingId.value = null
  }

  fun shutdown() {
    tts?.stop()
    tts?.shutdown()
    tts = null
  }
}
