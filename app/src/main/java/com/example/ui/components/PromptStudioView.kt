package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainTab
import com.example.ui.RikAiViewModel

@Composable
fun PromptStudioView(
  viewModel: RikAiViewModel,
  onNavigateToChat: (prompt: String) -> Unit
) {
  var subjectText by remember { mutableStateOf("") }
  var selectedStyle by remember { mutableStateOf("Photorealistic 8K") }
  var selectedLighting by remember { mutableStateOf("Volumetric Golden Hour") }
  var selectedLens by remember { mutableStateOf("85mm Portrait (f/1.4)") }

  val styles = listOf("Photorealistic 8K", "Cinematic Film 35mm", "Cyberpunk Neon", "Fantasy Unreal Engine", "Vintage Polaroid")
  val lightings = listOf("Volumetric Golden Hour", "Moody Cyberpunk Neon", "Soft Studio Light", "Bioluminescent Glow")
  val lenses = listOf("85mm Portrait (f/1.4)", "Wide 24mm Cinematic", "Macro Close-Up", "Aerial Drone View")

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
      .testTag("prompt_studio_view")
  ) {
    Text(
      text = "RIK AI Image Prompt Studio",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground
    )
    Text(
      text = "Generate professional, photorealistic cinematic prompts formatted for AI image models",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Subject Input
    OutlinedTextField(
      value = subjectText,
      onValueChange = { subjectText = it },
      label = { Text("What visual scene do you want to create?") },
      placeholder = { Text("e.g., An astronaut sipping chai in a futuristic Kolkata tea stall") },
      leadingIcon = {
        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
      },
      minLines = 3,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("prompt_subject_input")
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Visual Style Selector
    Text(
      text = "Artistic Style",
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(6.dp))
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      styles.forEach { style ->
        FilterChip(
          selected = selectedStyle == style,
          onClick = { selectedStyle = style },
          label = { Text(style, fontSize = 12.sp) }
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Lighting Selector
    Text(
      text = "Atmospheric Lighting",
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(6.dp))
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      lightings.forEach { light ->
        FilterChip(
          selected = selectedLighting == light,
          onClick = { selectedLighting = light },
          label = { Text(light, fontSize = 12.sp) }
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Camera Lens Selector
    Text(
      text = "Camera & Perspective",
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(6.dp))
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      lenses.forEach { lens ->
        FilterChip(
          selected = selectedLens == lens,
          onClick = { selectedLens = lens },
          label = { Text(lens, fontSize = 12.sp) }
        )
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Generate Button
    Button(
      onClick = {
        val subject = subjectText.ifBlank { "a futuristic AI companion assisting a human in an illuminated holographic laboratory" }
        val finalRequest = "Generate a photorealistic image prompt for: $subject. Style: $selectedStyle, Lighting: $selectedLighting, Perspective: $selectedLens."
        viewModel.setTab(MainTab.CHAT)
        viewModel.sendMessage(finalRequest)
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .testTag("generate_image_prompt_button"),
      shape = RoundedCornerShape(14.dp)
    ) {
      Icon(Icons.Default.AutoAwesome, contentDescription = null)
      Spacer(modifier = Modifier.width(8.dp))
      Text("Craft Cinematic Prompt [IMAGE_PROMPT]", fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Quick Inspiration Ideas
    Text(
      text = "Trending Prompt Inspirations",
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(10.dp))

    val inspirations = listOf(
      "A serene tea garden in Darjeeling with morning mist, golden sunrise rays penetrating pine trees, hyper-realistic 8K.",
      "Cyberpunk street vendor in futuristic Old Delhi, neon signs reflecting off rainy cobblestones, octane render.",
      "Futuristic robotic artisan crafting intricate Bengali terracotta pottery with glowing laser tools."
    )

    inspirations.forEach { idea ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(text = idea, style = MaterialTheme.typography.bodySmall)
          Spacer(modifier = Modifier.height(6.dp))
          Button(
            onClick = {
              viewModel.setTab(MainTab.CHAT)
              viewModel.sendMessage("Generate a photorealistic image prompt for: $idea")
            },
            modifier = Modifier.align(Alignment.End),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Use Idea", fontSize = 11.sp)
          }
        }
      }
    }
  }
}
