package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.LanguageMode
import com.example.ui.auth.AuthDialog
import com.example.ui.auth.UserProfileDialog
import com.example.ui.components.ChefView
import com.example.ui.components.MessageBubble
import com.example.ui.components.ProductivityView
import com.example.ui.components.PromptStudioView
import com.example.ui.components.SavedItemsView
import com.example.ui.components.TypingIndicator
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: RikAiViewModel) {
  val currentTab by viewModel.currentTab.collectAsState()
  val currentUser by viewModel.currentUser.collectAsState()
  val showAuthDialog by viewModel.showAuthDialog.collectAsState()
  val showProfileDialog by viewModel.showProfileDialog.collectAsState()
  val selectedLanguage by viewModel.selectedLanguage.collectAsState()

  var showLanguageMenu by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                  Brush.linearGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, NeonCyan)
                  )
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = "RIK AI Logo",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "RIK AI",
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 18.sp,
                  letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = MaterialTheme.colorScheme.primaryContainer
                ) {
                  Text(
                    text = selectedLanguage.flag,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                  )
                }
              }
              Text(
                text = "Multilingual • Creative • Life Coach",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        actions = {
          // Language selector
          Box {
            IconButton(
              onClick = { showLanguageMenu = true },
              modifier = Modifier.testTag("language_selector_button")
            ) {
              Icon(
                imageVector = Icons.Default.Language,
                contentDescription = "Select Language",
                tint = MaterialTheme.colorScheme.primary
              )
            }
            DropdownMenu(
              expanded = showLanguageMenu,
              onDismissRequest = { showLanguageMenu = false }
            ) {
              LanguageMode.values().forEach { mode ->
                DropdownMenuItem(
                  text = {
                    Text("${mode.flag} ${mode.displayName} (${mode.nativeName})")
                  },
                  onClick = {
                    viewModel.setLanguage(mode)
                    showLanguageMenu = false
                  }
                )
              }
            }
          }

          // Clear Chat Action
          if (currentTab == MainTab.CHAT) {
            IconButton(
              onClick = { viewModel.clearChat() },
              modifier = Modifier.testTag("clear_chat_button")
            ) {
              Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = "Clear Chat",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          // User Profile / Sign In button
          IconButton(
            onClick = {
              if (currentUser != null) {
                viewModel.showProfile(true)
              } else {
                viewModel.showAuth(true, AuthScreenMode.LOGIN)
              }
            },
            modifier = Modifier.testTag("user_auth_button")
          ) {
            if (currentUser != null) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = currentUser?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                  color = MaterialTheme.colorScheme.onPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                )
              }
            } else {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Sign In / Sign Up",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("bottom_nav_bar")
      ) {
        NavigationBarItem(
          selected = currentTab == MainTab.CHAT,
          onClick = { viewModel.setTab(MainTab.CHAT) },
          icon = { Icon(Icons.Default.ChatBubble, contentDescription = "Chat") },
          label = { Text("Chat", fontSize = 11.sp) },
          modifier = Modifier.testTag("nav_tab_chat")
        )
        NavigationBarItem(
          selected = currentTab == MainTab.PRODUCTIVITY,
          onClick = { viewModel.setTab(MainTab.PRODUCTIVITY) },
          icon = { Icon(Icons.Default.Timer, contentDescription = "Productivity") },
          label = { Text("Focus", fontSize = 11.sp) },
          modifier = Modifier.testTag("nav_tab_productivity")
        )
        NavigationBarItem(
          selected = currentTab == MainTab.RECIPES,
          onClick = { viewModel.setTab(MainTab.RECIPES) },
          icon = { Icon(Icons.Default.Restaurant, contentDescription = "Chef Recipes") },
          label = { Text("Kitchen", fontSize = 11.sp) },
          modifier = Modifier.testTag("nav_tab_recipes")
        )
        NavigationBarItem(
          selected = currentTab == MainTab.PROMPT_STUDIO,
          onClick = { viewModel.setTab(MainTab.PROMPT_STUDIO) },
          icon = { Icon(Icons.Default.Image, contentDescription = "Prompt Studio") },
          label = { Text("Prompts", fontSize = 11.sp) },
          modifier = Modifier.testTag("nav_tab_prompts")
        )
        NavigationBarItem(
          selected = currentTab == MainTab.SAVED,
          onClick = { viewModel.setTab(MainTab.SAVED) },
          icon = { Icon(Icons.Default.Bookmark, contentDescription = "Saved Items") },
          label = { Text("Saved", fontSize = 11.sp) },
          modifier = Modifier.testTag("nav_tab_saved")
        )
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      when (currentTab) {
        MainTab.CHAT -> ChatView(viewModel = viewModel)
        MainTab.PRODUCTIVITY -> ProductivityView(
          viewModel = viewModel,
          onNavigateToChat = { prompt ->
            viewModel.setTab(MainTab.CHAT)
            viewModel.sendMessage(prompt)
          }
        )
        MainTab.RECIPES -> ChefView(
          viewModel = viewModel,
          onNavigateToChat = { prompt ->
            viewModel.setTab(MainTab.CHAT)
            viewModel.sendMessage(prompt)
          }
        )
        MainTab.PROMPT_STUDIO -> PromptStudioView(
          viewModel = viewModel,
          onNavigateToChat = { prompt ->
            viewModel.setTab(MainTab.CHAT)
            viewModel.sendMessage(prompt)
          }
        )
        MainTab.SAVED -> SavedItemsView(viewModel = viewModel)
      }
    }
  }

  // Auth Dialog (Sign up, Log in, Forgot/Reset Password)
  if (showAuthDialog) {
    AuthDialog(
      viewModel = viewModel,
      onDismiss = { viewModel.showAuth(false) }
    )
  }

  // Profile Dialog (Logged in details, Logout)
  if (showProfileDialog) {
    UserProfileDialog(
      viewModel = viewModel,
      user = currentUser,
      onDismiss = { viewModel.showProfile(false) }
    )
  }
}

@Composable
private fun ChatView(viewModel: RikAiViewModel) {
  val messages by viewModel.messages.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val isPlayingId by viewModel.isPlayingId.collectAsState()
  val selectedLanguage by viewModel.selectedLanguage.collectAsState()
  val context = LocalContext.current

  var inputText by remember { mutableStateOf("") }
  var attachedImage by remember { mutableStateOf<Bitmap?>(null) }
  val listState = rememberLazyListState()

  val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
    if (bitmap != null) {
      attachedImage = bitmap
    }
  }

  val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
    if (isGranted) {
      cameraLauncher.launch(null)
    }
  }

  // Scroll to bottom when message list changes
  LaunchedEffect(messages.size, isLoading) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .imePadding()
  ) {
    // Quick Suggestion Chips on top of chat
    QuickSuggestionsRow(
      languageMode = selectedLanguage,
      onSelectPrompt = { prompt ->
        viewModel.sendMessage(prompt)
      }
    )

    // Message List
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      contentPadding = PaddingValues(vertical = 8.dp)
    ) {
      items(messages, key = { it.id }) { message ->
        MessageBubble(
          message = message,
          isPlaying = isPlayingId == message.id,
          onSpeakToggle = { viewModel.speakMessage(message) },
          onSaveItem = { title, category, content, prompt ->
            viewModel.saveItem(title, category, content, prompt)
          }
        )
      }

      if (isLoading) {
        item {
          TypingIndicator()
        }
      }
    }

    // Input Bar (with optional image preview)
    Surface(
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 3.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Thumbnail preview if a photo was captured
        if (attachedImage != null) {
          Row(
            modifier = Modifier
              .padding(horizontal = 16.dp, vertical = 6.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Image(
              bitmap = attachedImage!!.asImageBitmap(),
              contentDescription = "Captured visual preview",
              modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
              contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Visual attached for RIK AI",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.weight(1f)
            )
            IconButton(
              onClick = { attachedImage = null },
              modifier = Modifier
                .size(28.dp)
                .testTag("remove_image_button")
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove photo",
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Camera capture button
          IconButton(
            onClick = {
              val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
              ) == PackageManager.PERMISSION_GRANTED
              if (hasPermission) {
                cameraLauncher.launch(null)
              } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
              }
            },
            modifier = Modifier
              .size(42.dp)
              .testTag("camera_button")
          ) {
            Icon(
              imageVector = Icons.Default.PhotoCamera,
              contentDescription = "Take photo for visual analysis",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(4.dp))

          OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            placeholder = {
              Text(
                text = when {
                  attachedImage != null -> "Ask RIK AI about this photo..."
                  selectedLanguage == LanguageMode.BENGALI -> "RIK AI কে বাংলায় কিছু জিজ্ঞেস করুন..."
                  selectedLanguage == LanguageMode.HINDI -> "RIK AI से हिंदी में पूछें..."
                  else -> "Message RIK AI in any language..."
                },
                fontSize = 14.sp
              )
            },
            modifier = Modifier
              .weight(1f)
              .testTag("chat_input_field"),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
              unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ),
            maxLines = 4
          )

          Spacer(modifier = Modifier.width(8.dp))

          FloatingActionButton(
            onClick = {
              if ((inputText.isNotBlank() || attachedImage != null) && !isLoading) {
                val promptToSend = inputText
                val imageToSend = attachedImage
                inputText = ""
                attachedImage = null
                viewModel.sendMessage(promptToSend, imageToSend)
              }
            },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
              .size(48.dp)
              .testTag("send_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Send,
              contentDescription = "Send Message",
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun QuickSuggestionsRow(
  languageMode: LanguageMode,
  onSelectPrompt: (String) -> Unit
) {
  val suggestions = remember(languageMode) {
    when (languageMode) {
      LanguageMode.BENGALI -> listOf(
        "🇧🇩 একটি খাঁটি বাঙালি কৌতুক বলো",
        "🎨 কলকাতার একটি সায়েন্স-ফিকশন ছবির সিনেম্যাটিক প্রম্পট বানাও",
        "🐟 খাঁটি সর্ষে ইলিশের রেসিপি দাও",
        "⏱️ দিনে কাজের গতি বাড়ানোর ৫টি কার্যকরী টিপস"
      )
      LanguageMode.HINDI -> listOf(
        "🇮🇳 एक मजेदार हिंदी चुटकुला सुनाओ",
        "🎨 भविष्य की दिल्ली का सिनेमैटिक इमेज प्रॉम्प्ट बनाओ",
        "🧀 पनीर बटर मसाला की स्वादिष्ट रेसिपी बताओ",
        "⏱️ पोमोडोरो तकनीक से समय कैसे बचाएं?"
      )
      else -> listOf(
        "🇧🇩 বাংলায় একটি মজার কৌতুক বলো",
        "🇮🇳 एक मजेदार हिंदी चुटकुला सुनाओ",
        "🎨 Generate an image of Cyberpunk Varanasi",
        "⏱️ Plan my day with Pomodoro productivity",
        "🍳 Authentic Shorshe Ilish recipe",
        "💡 Explain Quantum Physics simply"
      )
    }
  }

  LazyRow(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    contentPadding = PaddingValues(horizontal = 12.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    items(suggestions) { chipText ->
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier
          .clip(RoundedCornerShape(16.dp))
          .clickable {
            val actualPrompt = when {
              chipText.contains("কৌতুক") -> "বাংলায় একটি মজার কৌতুক বলো"
              chipText.contains("চুটকুला") || chipText.contains("चुटकुला") -> "एक मजेदार हिंदी चुटकुला सुनाओ"
              chipText.contains("প্রম্পট") || chipText.contains("ইমেজ") -> "Create a photorealistic image prompt of futuristic science fiction Kolkata street"
              chipText.contains("प्रॉम्प्ट") -> "Create a photorealistic image prompt of futuristic cyberpunk Delhi street"
              chipText.contains("Shorshe Ilish") || chipText.contains("ইলিশের") -> "Provide an authentic recipe for Shorshe Ilish with exact ingredients and cooking time"
              chipText.contains("पनीर") -> "पनीर बटर मसाला की पूरी सामग्री और रेसिपी बताओ"
              chipText.contains("Pomodoro") || chipText.contains("পোমোডোরো") -> "How do I use the Pomodoro technique for daily productivity?"
              else -> chipText.replace(Regex("^[^\u0000-\u007F]+ "), "")
            }
            onSelectPrompt(actualPrompt)
          }
      ) {
        Text(
          text = chipText,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.Medium,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
      }
    }
  }
}
