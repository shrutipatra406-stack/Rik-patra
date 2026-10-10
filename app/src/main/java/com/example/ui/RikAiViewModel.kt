package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ChatMessage
import com.example.data.LanguageMode
import com.example.data.MessageRole
import com.example.data.PromptSuggestion
import com.example.data.RikAiService
import com.example.data.auth.AppDatabase
import com.example.data.auth.AuthRepository
import com.example.data.auth.SavedItemEntity
import com.example.data.auth.UserEntity
import com.example.ui.components.TtsHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
  CHAT,
  PRODUCTIVITY,
  RECIPES,
  PROMPT_STUDIO,
  SAVED
}

enum class AuthScreenMode {
  LOGIN,
  SIGN_UP,
  FORGOT_PASSWORD
}

sealed class AuthUiState {
  object Idle : AuthUiState()
  object Loading : AuthUiState()
  data class Success(val message: String) : AuthUiState()
  data class Error(val errorMessage: String) : AuthUiState()
  data class ResetCodeSent(val email: String, val testCodeNotice: String) : AuthUiState()
}

class RikAiViewModel(application: Application) : AndroidViewModel(application) {

  private val database = AppDatabase.getDatabase(application)
  private val authRepository = AuthRepository(application, database.userDao())
  private val savedItemDao = database.savedItemDao()
  private val rikAiService = RikAiService()
  private val ttsHelper = TtsHelper(application)

  val currentUser: StateFlow<UserEntity?> = authRepository.currentUser
  val isPlayingId: StateFlow<String?> = ttsHelper.isPlayingId

  private val _authScreenMode = MutableStateFlow(AuthScreenMode.LOGIN)
  val authScreenMode: StateFlow<AuthScreenMode> = _authScreenMode.asStateFlow()

  private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
  val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

  private val _showAuthDialog = MutableStateFlow(false)
  val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

  private val _showProfileDialog = MutableStateFlow(false)
  val showProfileDialog: StateFlow<Boolean> = _showProfileDialog.asStateFlow()

  private val _currentTab = MutableStateFlow(MainTab.CHAT)
  val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

  private val _selectedLanguage = MutableStateFlow(LanguageMode.AUTO)
  val selectedLanguage: StateFlow<LanguageMode> = _selectedLanguage.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
  val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

  val savedItems: StateFlow<List<SavedItemEntity>> = savedItemDao.getAllSavedItems()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Pomodoro Timer State
  private val _pomodoroSecondsLeft = MutableStateFlow(25 * 60)
  val pomodoroSecondsLeft: StateFlow<Int> = _pomodoroSecondsLeft.asStateFlow()

  private val _pomodoroIsRunning = MutableStateFlow(false)
  val pomodoroIsRunning: StateFlow<Boolean> = _pomodoroIsRunning.asStateFlow()

  private val _isPomodoroBreak = MutableStateFlow(false)
  val isPomodoroBreak: StateFlow<Boolean> = _isPomodoroBreak.asStateFlow()

  private var pomodoroJob: Job? = null

  init {
    loadInitialGreeting()
  }

  fun setTab(tab: MainTab) {
    _currentTab.value = tab
  }

  fun setLanguage(mode: LanguageMode) {
    _selectedLanguage.value = mode
  }

  fun setAuthScreenMode(mode: AuthScreenMode) {
    _authScreenMode.value = mode
    _authUiState.value = AuthUiState.Idle
  }

  fun showAuth(show: Boolean, initialMode: AuthScreenMode = AuthScreenMode.LOGIN) {
    _authScreenMode.value = initialMode
    _authUiState.value = AuthUiState.Idle
    _showAuthDialog.value = show
  }

  fun showProfile(show: Boolean) {
    _showProfileDialog.value = show
  }

  fun dismissAuthMessage() {
    _authUiState.value = AuthUiState.Idle
  }

  // --- AUTHENTICATION FLOWS ---

  fun signUp(name: String, email: String, password: String, confirmPass: String) {
    if (password != confirmPass) {
      _authUiState.value = AuthUiState.Error("Passwords do not match.")
      return
    }
    viewModelScope.launch {
      _authUiState.value = AuthUiState.Loading
      val result = authRepository.signUp(name, email, password)
      result.onSuccess { user ->
        _authUiState.value = AuthUiState.Success("Account created successfully! Welcome, ${user.name}.")
        _showAuthDialog.value = false
        // Update greeting with user's name
        sendPersonalizedGreeting(user.name)
      }.onFailure { err ->
        _authUiState.value = AuthUiState.Error(err.message ?: "Sign up failed. Please try again.")
      }
    }
  }

  fun logIn(email: String, password: String) {
    viewModelScope.launch {
      _authUiState.value = AuthUiState.Loading
      val result = authRepository.logIn(email, password)
      result.onSuccess { user ->
        _authUiState.value = AuthUiState.Success("Welcome back, ${user.name}!")
        _showAuthDialog.value = false
        sendPersonalizedGreeting(user.name)
      }.onFailure { err ->
        _authUiState.value = AuthUiState.Error(err.message ?: "Login failed. Please check your credentials.")
      }
    }
  }

  fun logOut() {
    viewModelScope.launch {
      authRepository.logOut()
      _showProfileDialog.value = false
      _messages.value = listOf(
        ChatMessage(
          role = MessageRole.MODEL,
          content = "You have been logged out safely. I am still here as RIK AI whenever you need me! Log in anytime to sync your personal profile."
        )
      )
    }
  }

  fun requestPasswordReset(email: String) {
    viewModelScope.launch {
      _authUiState.value = AuthUiState.Loading
      val result = authRepository.initiatePasswordReset(email)
      result.onSuccess { code ->
        _authUiState.value = AuthUiState.ResetCodeSent(
          email = email,
          testCodeNotice = "Verification code generated: $code"
        )
      }.onFailure { err ->
        _authUiState.value = AuthUiState.Error(err.message ?: "Could not initiate reset. Check email address.")
      }
    }
  }

  fun confirmPasswordReset(email: String, code: String, newPass: String, confirmPass: String) {
    if (newPass != confirmPass) {
      _authUiState.value = AuthUiState.Error("New passwords do not match.")
      return
    }
    viewModelScope.launch {
      _authUiState.value = AuthUiState.Loading
      val result = authRepository.completePasswordReset(email, code, newPass)
      result.onSuccess {
        _authUiState.value = AuthUiState.Success("Password reset successfully! Please log in with your new password.")
        _authScreenMode.value = AuthScreenMode.LOGIN
      }.onFailure { err ->
        _authUiState.value = AuthUiState.Error(err.message ?: "Password reset failed. Invalid or expired code.")
      }
    }
  }

  // --- CHAT FLOWS ---

  fun sendMessage(userText: String, imageBitmap: android.graphics.Bitmap? = null) {
    val text = userText.trim()
    if ((text.isEmpty() && imageBitmap == null) || _isLoading.value) return

    val userMessage = ChatMessage(
      role = MessageRole.USER,
      content = text.ifEmpty { "Analyze this image" },
      imageBitmap = imageBitmap
    )

    _messages.value = _messages.value + userMessage
    _isLoading.value = true

    viewModelScope.launch {
      val result = rikAiService.sendMessage(_messages.value, text, imageBitmap)
      _isLoading.value = false
      result.onSuccess { aiResponse ->
        val aiMessage = ChatMessage(
          role = MessageRole.MODEL,
          content = aiResponse
        )
        _messages.value = _messages.value + aiMessage
      }.onFailure { err ->
        val errorMessage = ChatMessage(
          role = MessageRole.MODEL,
          content = "Sorry, I encountered an issue: ${err.message}. Please try again.",
          isError = true
        )
        _messages.value = _messages.value + errorMessage
      }
    }
  }

  fun clearChat() {
    ttsHelper.stop()
    loadInitialGreeting()
  }

  fun speakMessage(message: ChatMessage) {
    ttsHelper.speak(message.id, message.content)
  }

  fun stopSpeaking() {
    ttsHelper.stop()
  }

  // --- SAVED ITEMS ---

  fun saveItem(title: String, category: String, content: String, imagePrompt: String? = null) {
    viewModelScope.launch {
      val user = currentUser.value
      val item = SavedItemEntity(
        userId = user?.id ?: "guest",
        title = title.ifEmpty { "Saved ${category.replace("_", " ")}" },
        category = category,
        content = content,
        imagePrompt = imagePrompt
      )
      savedItemDao.insertItem(item)
    }
  }

  fun deleteSavedItem(id: String) {
    viewModelScope.launch {
      savedItemDao.deleteById(id)
    }
  }

  // --- POMODORO TIMER ---

  fun togglePomodoro() {
    if (_pomodoroIsRunning.value) {
      pausePomodoro()
    } else {
      startPomodoro()
    }
  }

  private fun startPomodoro() {
    _pomodoroIsRunning.value = true
    pomodoroJob?.cancel()
    pomodoroJob = viewModelScope.launch {
      while (_pomodoroIsRunning.value && _pomodoroSecondsLeft.value > 0) {
        delay(1000)
        _pomodoroSecondsLeft.value -= 1
      }
      if (_pomodoroSecondsLeft.value == 0) {
        // Switch modes
        _isPomodoroBreak.value = !_isPomodoroBreak.value
        _pomodoroSecondsLeft.value = if (_isPomodoroBreak.value) 5 * 60 else 25 * 60
        _pomodoroIsRunning.value = false
      }
    }
  }

  private fun pausePomodoro() {
    _pomodoroIsRunning.value = false
    pomodoroJob?.cancel()
  }

  fun resetPomodoro() {
    pausePomodoro()
    _isPomodoroBreak.value = false
    _pomodoroSecondsLeft.value = 25 * 60
  }

  private fun loadInitialGreeting() {
    val userName = currentUser.value?.name
    val greeting = if (userName != null) {
      "Welcome back, **$userName**! I am **RIK AI** — your versatile, multilingual companion.\n\n" +
        "How can I assist you today? Ask me for:\n" +
        "• 🌐 Multilingual conversations in English, Bengali (বাংলা), & Hindi (हिंदी)\n" +
        "• 🎨 Photorealistic AI image prompts\n" +
        "• 🎭 Bengali, Hindi, or English humor & jokes\n" +
        "• ⏱️ Productivity coaching & Pomodoro routines\n" +
        "• 🍳 Detailed culinary recipes with prep steps"
    } else {
      "Hello! I am **RIK AI**, your intelligent, multilingual AI companion.\n\n" +
        "I am fully fluent in English, Bengali (বাংলা), Hindi (हिंदी), and all major languages.\n" +
        "Ask me to generate a photorealistic image prompt, share a witty joke, structure your day with productivity hacks, or cook a delicious gourmet meal!"
    }

    _messages.value = listOf(
      ChatMessage(
        role = MessageRole.MODEL,
        content = greeting
      )
    )
  }

  private fun sendPersonalizedGreeting(userName: String) {
    val personalizedMsg = ChatMessage(
      role = MessageRole.MODEL,
      content = "Great to have you here, **$userName**! Your session is active and secure. What would you like to explore together today?"
    )
    _messages.value = _messages.value + personalizedMsg
  }

  override fun onCleared() {
    super.onCleared()
    ttsHelper.shutdown()
    pomodoroJob?.cancel()
  }
}
