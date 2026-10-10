package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.SavedItemEntity
import com.example.ui.RikAiViewModel

@Composable
fun SavedItemsView(
  viewModel: RikAiViewModel
) {
  val savedItems by viewModel.savedItems.collectAsState()
  var selectedCategory by remember { mutableStateOf("ALL") }
  val context = LocalContext.current

  val categories = listOf("ALL", "IMAGE_PROMPT", "RECIPE", "JOKE", "PRODUCTIVITY")

  val filteredItems = remember(savedItems, selectedCategory) {
    if (selectedCategory == "ALL") savedItems
    else savedItems.filter { it.category == selectedCategory }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("saved_items_view")
  ) {
    Text(
      text = "Saved Collection",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground
    )
    Text(
      text = "Your bookmarked recipes, AI image prompts, jokes, and routines",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Filter Chips
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      categories.forEach { cat ->
        FilterChip(
          selected = selectedCategory == cat,
          onClick = { selectedCategory = cat },
          label = {
            Text(
              text = when (cat) {
                "ALL" -> "All (${savedItems.size})"
                "IMAGE_PROMPT" -> "Prompts"
                "RECIPE" -> "Recipes"
                "JOKE" -> "Jokes"
                "PRODUCTIVITY" -> "Routines"
                else -> cat
              },
              fontSize = 12.sp
            )
          }
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (filteredItems.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = 60.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.BookmarkBorder,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "No saved items in this category yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Tap the bookmark icon on any RIK AI message to save it here!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredItems, key = { it.id }) { item ->
          SavedItemCard(
            item = item,
            onCopy = {
              val textToCopy = item.imagePrompt ?: item.content
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              clipboard.setPrimaryClip(ClipData.newPlainText("RIK AI Saved", textToCopy))
              Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
            },
            onDelete = {
              viewModel.deleteSavedItem(item.id)
              Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show()
            }
          )
        }
      }
    }
  }
}

@Composable
private fun SavedItemCard(
  item: SavedItemEntity,
  onCopy: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("saved_item_${item.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = when (item.category) {
              "IMAGE_PROMPT" -> Icons.Default.Image
              "RECIPE" -> Icons.Default.Restaurant
              "JOKE" -> Icons.Default.Mood
              "PRODUCTIVITY" -> Icons.Default.Lightbulb
              else -> Icons.Default.BookmarkBorder
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = item.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Row {
          IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
          }
          IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
              Icons.Default.Delete,
              contentDescription = "Delete",
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = item.content,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 6
      )

      if (item.imagePrompt != null) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Prompt: ${item.imagePrompt}",
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          maxLines = 3
        )
      }
    }
  }
}
