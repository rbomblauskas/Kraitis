package io.github.rbomblauskas.kraitis.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    var confirmItemId by rememberSaveable { mutableStateOf<Long?>(null) }

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

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 110.dp),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(wearable, key = { it.id }) { item ->
            Column(
                modifier = Modifier.clickable { confirmItemId = item.id },
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ItemPhoto(
                    item = item,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    val confirmItem = wearable.firstOrNull { it.id == confirmItemId }
    if (confirmItem != null) {
        val lastWornAt = wearEvents
            .filter { it.itemId == confirmItem.id }
            .maxOfOrNull { it.wornAt }

        AlertDialog(
            onDismissRequest = { confirmItemId = null },
            title = { Text("Log wear") },
            text = {
                Text(
                    "Mark \"${confirmItem.name}\" as worn today?\n" +
                        "Last worn: " + (lastWornAt?.let { formatDate(it) } ?: "never")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onLogWear(confirmItem.id)
                        confirmItemId = null
                    }
                ) {
                    Text("Log wear")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmItemId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
