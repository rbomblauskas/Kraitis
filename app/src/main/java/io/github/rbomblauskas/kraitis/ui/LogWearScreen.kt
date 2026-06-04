package io.github.rbomblauskas.kraitis.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.rbomblauskas.kraitis.data.ClothingItem
import io.github.rbomblauskas.kraitis.data.ClothingStatus
import io.github.rbomblauskas.kraitis.data.WearEvent

@Composable
fun LogWearScreen(
    items: List<ClothingItem>,
    wearEvents: List<WearEvent>,
    onLogWear: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // only items that still make sense to wear
    val wearable = items.filter {
        it.status == ClothingStatus.ACTIVE || it.status == ClothingStatus.REWEAR
    }

    if (wearable.isEmpty()) {
        Box(modifier = modifier) {
            Text(
                text = "No active clothes to wear.\nAdd items in the wardrobe tab first.",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(wearable, key = { it.id }) { item ->
            val events = wearEvents.filter { it.itemId == item.id }
            val lastWornAt = events.maxOfOrNull { it.wornAt }

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ItemPhoto(item = item, size = 56.dp)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "worn ${events.size}x, last: " +
                                (lastWornAt?.let { formatDate(it) } ?: "never"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { onLogWear(item.id) },
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Wear")
                    }
                }
            }
        }
    }
}
