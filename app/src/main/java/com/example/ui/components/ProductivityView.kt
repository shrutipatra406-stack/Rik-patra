package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainTab
import com.example.ui.RikAiViewModel
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple

@Composable
fun ProductivityView(
  viewModel: RikAiViewModel,
  onNavigateToChat: (prompt: String) -> Unit
) {
  val secondsLeft by viewModel.pomodoroSecondsLeft.collectAsState()
  val isRunning by viewModel.pomodoroIsRunning.collectAsState()
  val isBreak by viewModel.isPomodoroBreak.collectAsState()

  val minutes = secondsLeft / 60
  val seconds = secondsLeft % 60
  val timeFormatted = String.format("%02d:%02d", minutes, seconds)
  val totalDuration = if (isBreak) 5 * 60 else 25 * 60
  val progress = 1f - (secondsLeft.toFloat() / totalDuration.toFloat())

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
      .testTag("productivity_view"),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Title Banner
    Text(
      text = "RIK AI Life Coach & Productivity",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground
    )
    Text(
      text = "Actionable routines, Pomodoro focus cycles, and time-mastery",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Interactive Pomodoro Timer Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("pomodoro_card"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
      )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = if (isBreak) Icons.Default.HourglassBottom else Icons.Default.Timer,
            contentDescription = null,
            tint = if (isBreak) NeonCyan else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isBreak) "Rest & Rejuvenation Break" else "Deep Focus Session",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            color = if (isBreak) NeonCyan else MaterialTheme.colorScheme.primary
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Circular Timer Display
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.size(180.dp)
        ) {
          CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(180.dp),
            strokeWidth = 10.dp,
            color = if (isBreak) NeonCyan else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
          )

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = timeFormatted,
              fontSize = 42.sp,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (isRunning) "IN PROGRESS" else "PAUSED",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Timer Controls
        Row(
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Button(
            onClick = { viewModel.togglePomodoro() },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .height(48.dp)
              .testTag("pomodoro_toggle_button")
          ) {
            Icon(
              imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = null
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isRunning) "Pause" else "Start Focus", fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.width(12.dp))

          OutlinedButton(
            onClick = { viewModel.resetPomodoro() },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .height(48.dp)
              .testTag("pomodoro_reset_button")
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Reset")
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Quick AI Coaching Plans
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Ask RIK AI Life Coach",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    val coachingPrompts = listOf(
      "Plan my day using the Pomodoro technique and time-blocking for maximum focus.",
      "Explain the 2-minute rule and how I can eliminate daily procrastination.",
      "Design a healthy morning routine for high mental clarity and sustained energy.",
      "বাংলায় একটি আদর্শ উৎপাদনশীল কাজের রুটিন এবং সময় ব্যবস্থাপনার টিপস দিন।",
      "हिंदी में एक कुशल समय प्रबंधन और दैनिक दिनचर्या का प्लान बताइए।"
    )

    coachingPrompts.forEach { prompt ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = prompt,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.width(8.dp))
          IconButton(
            onClick = {
              viewModel.setTab(MainTab.CHAT)
              viewModel.sendMessage(prompt)
            },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Send,
              contentDescription = "Ask RIK AI",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    }
  }
}
