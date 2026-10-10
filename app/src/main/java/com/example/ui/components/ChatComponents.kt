package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessage
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple

@Composable
fun MessageBubble(
  message: ChatMessage,
  isPlaying: Boolean,
  onSpeakToggle: () -> Unit,
  onSaveItem: (title: String, category: String, content: String, prompt: String?) -> Unit
) {
  val context = LocalContext.current

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 6.dp),
    horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
    verticalAlignment = Alignment.Top
  ) {
    if (!message.isUser) {
      // RIK AI Avatar
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
          contentDescription = "RIK AI",
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
    }

    Column(
      modifier = Modifier.widthIn(max = 320.dp),
      horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
      // Main Message Card
      Surface(
        shape = RoundedCornerShape(
          topStart = 18.dp,
          topEnd = 18.dp,
          bottomStart = if (message.isUser) 18.dp else 4.dp,
          bottomEnd = if (message.isUser) 4.dp else 18.dp
        ),
        color = if (message.isUser) {
          MaterialTheme.colorScheme.primary
        } else {
          MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
        },
        tonalElevation = if (message.isUser) 4.dp else 1.dp,
        modifier = Modifier
          .testTag(if (message.isUser) "user_message_bubble" else "rik_message_bubble")
          .then(
            if (!message.isUser) {
              Modifier.border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
              )
            } else Modifier
          )
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          if (!message.isUser) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(bottom = 6.dp)
            ) {
              Text(
                text = "RIK AI",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(NeonCyan)
              )
            }
          }

          if (message.imageBitmap != null) {
            Image(
              bitmap = message.imageBitmap.asImageBitmap(),
              contentDescription = "Visual input for RIK AI",
              modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 220.dp)
                .clip(RoundedCornerShape(12.dp))
                .padding(bottom = 8.dp),
              contentScale = ContentScale.Crop
            )
          }

          // Formatted text
          RichFormattedText(
            text = message.content,
            isUser = message.isUser
          )

          // If AI response has an image prompt, render the dedicated Image Prompt Card
          if (!message.isUser && message.imagePrompt != null) {
            Spacer(modifier = Modifier.height(12.dp))
            ImagePromptCard(
              prompt = message.imagePrompt,
              onCopyPrompt = {
                copyToClipboard(context, message.imagePrompt, "Image prompt copied to clipboard!")
              },
              onSavePrompt = {
                onSaveItem("AI Image Prompt", "IMAGE_PROMPT", message.content, message.imagePrompt)
                Toast.makeText(context, "Saved to your Prompt Studio!", Toast.LENGTH_SHORT).show()
              }
            )
          }
        }
      }

      // Action toolbar for AI messages
      if (!message.isUser) {
        Row(
          modifier = Modifier
            .padding(top = 4.dp, start = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Listen / Speak button
          IconButton(
            onClick = onSpeakToggle,
            modifier = Modifier
              .size(32.dp)
              .testTag("tts_button")
          ) {
            Icon(
              imageVector = if (isPlaying) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
              contentDescription = if (isPlaying) "Stop reading" else "Listen aloud",
              tint = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
          }

          // Copy text button
          IconButton(
            onClick = {
              copyToClipboard(context, message.content, "Message copied to clipboard!")
            },
            modifier = Modifier
              .size(32.dp)
              .testTag("copy_message_button")
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "Copy message",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
          }

          // Bookmark / Save button
          IconButton(
            onClick = {
              val category = when {
                message.imagePrompt != null -> "IMAGE_PROMPT"
                message.content.contains("রেসিপি") || message.content.contains("Recipe") -> "RECIPE"
                message.content.contains("কৌতুক") || message.content.contains("joke") -> "JOKE"
                message.content.contains("Pomodoro") || message.content.contains("Productivity") -> "PRODUCTIVITY"
                else -> "NOTE"
              }
              onSaveItem("RIK AI Insight", category, message.content, message.imagePrompt)
              Toast.makeText(context, "Saved to your collection!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
              .size(32.dp)
              .testTag("save_message_button")
          ) {
            Icon(
              imageVector = Icons.Default.BookmarkAdd,
              contentDescription = "Save message",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }

    if (message.isUser) {
      Spacer(modifier = Modifier.width(8.dp))
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Person,
          contentDescription = "User",
          tint = MaterialTheme.colorScheme.onPrimaryContainer,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
fun ImagePromptCard(
  prompt: String,
  onCopyPrompt: () -> Unit,
  onSavePrompt: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("image_prompt_card"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = androidx.compose.foundation.BorderStroke(
      width = 1.dp,
      brush = Brush.horizontalGradient(listOf(NeonCyan, NeonPurple))
    )
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "AI Image Generator Prompt",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // The Prompt in styled quote container
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = prompt,
          fontFamily = FontFamily.Monospace,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(10.dp)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = onCopyPrompt,
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier
            .weight(1f)
            .height(36.dp)
            .testTag("copy_prompt_button")
        ) {
          Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Copy Prompt", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
          onClick = onSavePrompt,
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier
            .height(36.dp)
            .testTag("save_prompt_to_studio_button")
        ) {
          Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Save", fontSize = 12.sp)
        }
      }
    }
  }
}

@Composable
fun RichFormattedText(text: String, isUser: Boolean) {
  val defaultTextColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
  val boldTextColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary

  // Parse markdown: **bold**, headings, bullet points
  val annotated = buildAnnotatedString {
    var cursor = 0
    val boldPattern = Regex("""\*\*(.*?)\*\*""")
    val matches = boldPattern.findAll(text).toList()

    for (match in matches) {
      if (match.range.first > cursor) {
        append(text.substring(cursor, match.range.first))
      }
      withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = boldTextColor)) {
        append(match.groupValues[1])
      }
      cursor = match.range.last + 1
    }
    if (cursor < text.length) {
      append(text.substring(cursor))
    }
  }

  Text(
    text = annotated,
    style = MaterialTheme.typography.bodyMedium,
    color = defaultTextColor,
    lineHeight = 22.sp
  )
}

@Composable
fun TypingIndicator() {
  val transition = rememberInfiniteTransition(label = "typing")
  val alpha1 by transition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
    label = "dot1"
  )
  val alpha2 by transition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(600, delayMillis = 200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
    label = "dot2"
  )
  val alpha3 by transition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(600, delayMillis = 400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
    label = "dot3"
  )

  Row(
    modifier = Modifier
      .padding(start = 16.dp, bottom = 8.dp)
      .testTag("typing_indicator"),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = "RIK AI is thinking",
      fontSize = 12.sp,
      color = MaterialTheme.colorScheme.primary,
      fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.width(6.dp))
    Box(
      modifier = Modifier
        .size(6.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha1))
    )
    Spacer(modifier = Modifier.width(4.dp))
    Box(
      modifier = Modifier
        .size(6.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha2))
    )
    Spacer(modifier = Modifier.width(4.dp))
    Box(
      modifier = Modifier
        .size(6.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha3))
    )
  }
}

private fun copyToClipboard(context: Context, text: String, toastMessage: String) {
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  val clip = ClipData.newPlainText("RIK AI", text)
  clipboard.setPrimaryClip(clip)
  Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
}
