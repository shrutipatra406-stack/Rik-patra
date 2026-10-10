package com.example.ui.components

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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
fun ChefView(
  viewModel: RikAiViewModel,
  onNavigateToChat: (prompt: String) -> Unit
) {
  var recipeQuery by remember { mutableStateOf("") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
      .testTag("chef_view"),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "RIK AI Culinary Kitchen",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground
    )
    Text(
      text = "Gourmet recipes with exact ingredients, cook times, and numbered steps",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Recipe Search Input
    OutlinedTextField(
      value = recipeQuery,
      onValueChange = { recipeQuery = it },
      label = { Text("Search any dish or ingredient...") },
      placeholder = { Text("e.g., Shorshe Ilish, Butter Chicken, Vegan Pasta") },
      leadingIcon = {
        Icon(Icons.Default.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
      },
      trailingIcon = {
        if (recipeQuery.isNotBlank()) {
          IconButton(
            onClick = {
              val prompt = "Provide a step-by-step recipe for $recipeQuery with precise ingredient measurements, cooking time, and clear numbered instructions."
              viewModel.setTab(MainTab.CHAT)
              viewModel.sendMessage(prompt)
            }
          ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Ask RIK AI Chef", tint = MaterialTheme.colorScheme.primary)
          }
        }
      },
      singleLine = true,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("recipe_search_input")
    )

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = "Popular Chef Curations",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.align(Alignment.Start)
    )

    Spacer(modifier = Modifier.height(10.dp))

    val recipeCards = listOf(
      Triple(
        "খাঁটি বাঙালি সর্ষে ইলিশ (Shorshe Ilish)",
        "Traditional mustard hilsa fish curry with green chilies & pure mustard oil.",
        "বাংলায় খাঁটি সর্ষে ইলিশ রান্নার সম্পূর্ণ রেসিপি উপকরণ এবং রান্নার সময়সহ ধাপে ধাপে লিখে দাও।"
      ),
      Triple(
        "स्वादिष्ट पनीर बटर मसाला (Paneer Butter Masala)",
        "Rich, velvety tomato-cashew gravy with soft paneer cubes & aromatic kasuri methi.",
        "स्वादिष्ट पनीर बटर मसाला बनाने की पूरी सामग्री, समय और विधि हिंदी में विस्तार से बताइए।"
      ),
      Triple(
        "Kolkata Chicken Dum Biryani",
        "Fragrant long-grain basmati rice with spiced tender chicken and golden potatoes.",
        "Give me an authentic recipe for Kolkata Style Chicken Dum Biryani with exact spices and cooking steps."
      ),
      Triple(
        "Quick 15-Minute Garlic Butter Shrimp",
        "High protein, zesty lemon and herb pan-seared juicy shrimp with tossed greens.",
        "How do I cook a quick 15-minute garlic butter shrimp bowl with ingredients and cooking time?"
      )
    )

    recipeCards.forEach { (title, subtitle, prompt) ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp)
          .testTag("recipe_card_${title.take(10)}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = title,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(12.dp))
          Button(
            onClick = {
              viewModel.setTab(MainTab.CHAT)
              viewModel.sendMessage(prompt)
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.align(Alignment.End)
          ) {
            Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Get Recipe", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
