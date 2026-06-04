package io.github.rbomblauskas.kraitis.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.rbomblauskas.kraitis.data.ClothingItem
import java.io.File

// caller decides the size through the modifier
@Composable
fun ItemPhoto(
    item: ClothingItem,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)
    if (item.photoPath != null) {
        AsyncImage(
            model = File(item.photoPath),
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(shape)
        )
    } else {
        // no photo yet, show first letter instead
        Box(
            modifier = modifier
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.name.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
