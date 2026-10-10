package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.auth.UserEntity
import com.example.ui.RikAiViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UserProfileDialog(
  viewModel: RikAiViewModel,
  user: UserEntity?,
  onDismiss: () -> Unit
) {
  var showConfirmLogout by remember { mutableStateOf(false) }
  val savedItems by viewModel.savedItems.collectAsState()

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .clip(RoundedCornerShape(24.dp))
        .testTag("user_profile_dialog"),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "User Account",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          IconButton(onClick = onDismiss, modifier = Modifier.testTag("profile_close_button")) {
            Icon(Icons.Default.Close, contentDescription = "Close Profile")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (user != null) {
          // Avatar Initial
          val initial = user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
          Box(
            modifier = Modifier
              .size(72.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = initial,
              color = MaterialTheme.colorScheme.onPrimary,
              fontSize = 32.sp,
              fontWeight = FontWeight.ExtraBold
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = user.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = user.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(20.dp))

          // Information Details Card
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  Icons.Default.Security,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                  tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                val formattedDate = remember(user.createdAt) {
                  SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(user.createdAt))
                }
                Text(
                  text = "Member Since: $formattedDate",
                  style = MaterialTheme.typography.bodySmall
                )
              }

              Spacer(modifier = Modifier.height(10.dp))

              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  Icons.Default.Bookmark,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                  tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Saved Recipes & Prompts: ${savedItems.size}",
                  style = MaterialTheme.typography.bodySmall
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(24.dp))

          if (!showConfirmLogout) {
            OutlinedButton(
              onClick = { showConfirmLogout = true },
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("logout_button"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
              )
            ) {
              Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Log Out", fontWeight = FontWeight.Bold)
            }
          } else {
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = "Are you sure you want to log out?",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End
                ) {
                  OutlinedButton(
                    onClick = { showConfirmLogout = false },
                    modifier = Modifier.testTag("cancel_logout_button")
                  ) {
                    Text("Cancel")
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  androidx.compose.material3.Button(
                    onClick = {
                      viewModel.logOut()
                      onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_logout_button")
                  ) {
                    Text("Yes, Log Out")
                  }
                }
              }
            }
          }
        } else {
          // Guest State
          Text(
            text = "You are currently exploring RIK AI as a Guest.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(16.dp))
          androidx.compose.material3.Button(
            onClick = {
              onDismiss()
              viewModel.showAuth(true)
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("guest_login_button"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Up or Log In", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
