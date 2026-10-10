package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.AuthScreenMode
import com.example.ui.AuthUiState
import com.example.ui.RikAiViewModel

@Composable
fun AuthDialog(
  viewModel: RikAiViewModel,
  onDismiss: () -> Unit
) {
  val authMode by viewModel.authScreenMode.collectAsState()
  val authState by viewModel.authUiState.collectAsState()

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(vertical = 24.dp)
        .clip(RoundedCornerShape(24.dp))
        .testTag("auth_dialog"),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Top Row: Title + Close Button
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Auth Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = when (authMode) {
                  AuthScreenMode.LOGIN -> "Welcome to RIK AI"
                  AuthScreenMode.SIGN_UP -> "Create Account"
                  AuthScreenMode.FORGOT_PASSWORD -> "Reset Password"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = when (authMode) {
                  AuthScreenMode.LOGIN -> "Sign in to access your saved items & chat history"
                  AuthScreenMode.SIGN_UP -> "Join RIK AI to unlock personal life coach features"
                  AuthScreenMode.FORGOT_PASSWORD -> "Recover your account credentials"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("auth_close_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close Auth Dialog",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // State Feedback Cards (Error / Success)
        when (val state = authState) {
          is AuthUiState.Error -> {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Info,
                  contentDescription = "Error",
                  tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = state.errorMessage,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onErrorContainer
                )
              }
            }
          }
          is AuthUiState.Success -> {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7))
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = "Success",
                  tint = Color(0xFF16A34A)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = state.message,
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFF14532D)
                )
              }
            }
          }
          is AuthUiState.ResetCodeSent -> {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = "Reset Code Sent",
                    tint = MaterialTheme.colorScheme.secondary
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Verification Code Generated!",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = state.testCodeNotice,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
                Text(
                  text = "Please enter this 6-digit code below along with your new password.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSecondaryContainer
                )
              }
            }
          }
          else -> {}
        }

        // Form based on Auth Mode
        when (authMode) {
          AuthScreenMode.LOGIN -> {
            LoginForm(
              isLoading = authState is AuthUiState.Loading,
              onLogin = { email, pass -> viewModel.logIn(email, pass) },
              onNavigateToSignUp = { viewModel.setAuthScreenMode(AuthScreenMode.SIGN_UP) },
              onNavigateToForgot = { viewModel.setAuthScreenMode(AuthScreenMode.FORGOT_PASSWORD) }
            )
          }
          AuthScreenMode.SIGN_UP -> {
            SignUpForm(
              isLoading = authState is AuthUiState.Loading,
              onSignUp = { name, email, pass, confirm -> viewModel.signUp(name, email, pass, confirm) },
              onNavigateToLogin = { viewModel.setAuthScreenMode(AuthScreenMode.LOGIN) }
            )
          }
          AuthScreenMode.FORGOT_PASSWORD -> {
            ForgotPasswordForm(
              isLoading = authState is AuthUiState.Loading,
              codeSentEmail = (authState as? AuthUiState.ResetCodeSent)?.email,
              onRequestCode = { email -> viewModel.requestPasswordReset(email) },
              onConfirmReset = { email, code, newPass, confirm ->
                viewModel.confirmPasswordReset(email, code, newPass, confirm)
              },
              onNavigateToLogin = { viewModel.setAuthScreenMode(AuthScreenMode.LOGIN) }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun LoginForm(
  isLoading: Boolean,
  onLogin: (String, String) -> Unit,
  onNavigateToSignUp: () -> Unit,
  onNavigateToForgot: () -> Unit
) {
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
      value = email,
      onValueChange = { email = it },
      label = { Text("Email Address") },
      placeholder = { Text("your.email@example.com") },
      leadingIcon = {
        Icon(Icons.Default.Email, contentDescription = "Email Icon")
      },
      singleLine = true,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("login_email_input")
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
      value = password,
      onValueChange = { password = it },
      label = { Text("Password") },
      placeholder = { Text("Enter your password") },
      leadingIcon = {
        Icon(Icons.Default.Lock, contentDescription = "Password Icon")
      },
      trailingIcon = {
        IconButton(onClick = { passwordVisible = !passwordVisible }) {
          Icon(
            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            contentDescription = if (passwordVisible) "Hide password" else "Show password"
          )
        }
      },
      visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
      singleLine = true,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
      keyboardActions = KeyboardActions(onDone = { onLogin(email, password) }),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("login_password_input")
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.End
    ) {
      TextButton(
        onClick = onNavigateToForgot,
        modifier = Modifier.testTag("forgot_password_button")
      ) {
        Text(
          text = "Forgot Password?",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.primary
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Button(
      onClick = { onLogin(email, password) },
      enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .testTag("login_submit_button"),
      shape = RoundedCornerShape(12.dp)
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.size(24.dp),
          color = MaterialTheme.colorScheme.onPrimary,
          strokeWidth = 2.dp
        )
      } else {
        Text("Log In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Don't have an account?",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      TextButton(
        onClick = onNavigateToSignUp,
        modifier = Modifier.testTag("switch_to_signup_button")
      ) {
        Text(
          text = "Sign Up",
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      }
    }
  }
}

@Composable
private fun SignUpForm(
  isLoading: Boolean,
  onSignUp: (String, String, String, String) -> Unit,
  onNavigateToLogin: () -> Unit
) {
  var name by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
      value = name,
      onValueChange = { name = it },
      label = { Text("Full Name") },
      placeholder = { Text("e.g. Shruti Patra") },
      leadingIcon = {
        Icon(Icons.Default.Person, contentDescription = "Name Icon")
      },
      singleLine = true,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("signup_name_input")
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
      value = email,
      onValueChange = { email = it },
      label = { Text("Email Address") },
      placeholder = { Text("name@example.com") },
      leadingIcon = {
        Icon(Icons.Default.Email, contentDescription = "Email Icon")
      },
      singleLine = true,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("signup_email_input")
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
      value = password,
      onValueChange = { password = it },
      label = { Text("Password (Min 6 chars)") },
      placeholder = { Text("Create strong password") },
      leadingIcon = {
        Icon(Icons.Default.Lock, contentDescription = "Password Icon")
      },
      trailingIcon = {
        IconButton(onClick = { passwordVisible = !passwordVisible }) {
          Icon(
            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            contentDescription = if (passwordVisible) "Hide password" else "Show password"
          )
        }
      },
      visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
      singleLine = true,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("signup_password_input")
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
      value = confirmPassword,
      onValueChange = { confirmPassword = it },
      label = { Text("Confirm Password") },
      placeholder = { Text("Re-enter password") },
      leadingIcon = {
        Icon(Icons.Default.Lock, contentDescription = "Confirm Password Icon")
      },
      visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
      singleLine = true,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("signup_confirm_password_input")
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(
      onClick = { onSignUp(name, email, password, confirmPassword) },
      enabled = !isLoading && name.isNotBlank() && email.isNotBlank() && password.length >= 6,
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .testTag("signup_submit_button"),
      shape = RoundedCornerShape(12.dp)
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.size(24.dp),
          color = MaterialTheme.colorScheme.onPrimary,
          strokeWidth = 2.dp
        )
      } else {
        Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Already have an account?",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      TextButton(
        onClick = onNavigateToLogin,
        modifier = Modifier.testTag("switch_to_login_button")
      ) {
        Text(
          text = "Log In",
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      }
    }
  }
}

@Composable
private fun ForgotPasswordForm(
  isLoading: Boolean,
  codeSentEmail: String?,
  onRequestCode: (String) -> Unit,
  onConfirmReset: (String, String, String, String) -> Unit,
  onNavigateToLogin: () -> Unit
) {
  var email by remember { mutableStateOf(codeSentEmail ?: "") }
  var verificationCode by remember { mutableStateOf("") }
  var newPassword by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  val isCodeRequested = codeSentEmail != null

  Column(modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
      value = email,
      onValueChange = { email = it },
      label = { Text("Account Email") },
      placeholder = { Text("Enter your registered email") },
      leadingIcon = {
        Icon(Icons.Default.Email, contentDescription = "Email Icon")
      },
      singleLine = true,
      enabled = !isCodeRequested,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("forgot_email_input")
    )

    Spacer(modifier = Modifier.height(12.dp))

    if (!isCodeRequested) {
      Button(
        onClick = { onRequestCode(email) },
        enabled = !isLoading && email.isNotBlank(),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("request_reset_code_button"),
        shape = RoundedCornerShape(12.dp)
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp
          )
        } else {
          Text("Send Verification Code", fontWeight = FontWeight.Bold)
        }
      }
    } else {
      OutlinedTextField(
        value = verificationCode,
        onValueChange = { verificationCode = it },
        label = { Text("6-Digit Verification Code") },
        placeholder = { Text("Enter 6-digit code") },
        leadingIcon = {
          Icon(Icons.Default.Key, contentDescription = "Key Icon")
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("reset_code_input")
      )

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedTextField(
        value = newPassword,
        onValueChange = { newPassword = it },
        label = { Text("New Password (Min 6 chars)") },
        placeholder = { Text("Enter new password") },
        leadingIcon = {
          Icon(Icons.Default.Lock, contentDescription = "New Password Icon")
        },
        trailingIcon = {
          IconButton(onClick = { passwordVisible = !passwordVisible }) {
            Icon(
              imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
              contentDescription = if (passwordVisible) "Hide password" else "Show password"
            )
          }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("new_password_input")
      )

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedTextField(
        value = confirmPassword,
        onValueChange = { confirmPassword = it },
        label = { Text("Confirm New Password") },
        placeholder = { Text("Re-enter new password") },
        leadingIcon = {
          Icon(Icons.Default.Lock, contentDescription = "Confirm New Password")
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("confirm_new_password_input")
      )

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = { onConfirmReset(email, verificationCode, newPassword, confirmPassword) },
        enabled = !isLoading && verificationCode.length >= 4 && newPassword.length >= 6,
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("confirm_reset_password_button"),
        shape = RoundedCornerShape(12.dp)
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp
          )
        } else {
          Text("Reset Password", fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      OutlinedButton(
        onClick = { onRequestCode(email) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Resend Verification Code", style = MaterialTheme.typography.bodySmall)
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    TextButton(
      onClick = onNavigateToLogin,
      modifier = Modifier
        .align(Alignment.CenterHorizontally)
        .testTag("back_to_login_button")
    ) {
      Text("Back to Log In", color = MaterialTheme.colorScheme.primary)
    }
  }
}
