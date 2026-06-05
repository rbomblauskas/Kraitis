package io.github.rbomblauskas.kraitis.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
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
import io.github.rbomblauskas.kraitis.domain.Decision
import io.github.rbomblauskas.kraitis.domain.decideNextAction

// statuses the user can pick straight from the audit card
private val auditActions = listOf(
    ClothingStatus.REWEAR,
    ClothingStatus.REPAIR,
    ClothingStatus.SELL,
    ClothingStatus.DONATE,
    ClothingStatus.ARCHIVED
)

@Composable
fun RecommendationsScreen(
    items: List<ClothingItem>,
    wearEvents: List<WearEvent>,
    onItemClick: (Long) -> Unit,
    onStatusChange: (Long, ClothingStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    // items already marked sell/donate/archived count as handled
    val recommendations: List<Pair<ClothingItem, Decision>> = items
        .filter {
            it.status == ClothingStatus.ACTIVE ||
                it.status == ClothingStatus.REWEAR ||
                it.status == ClothingStatus.REPAIR
        }
        .mapNotNull { item ->
            val events = wearEvents.filter { it.itemId == item.id }
            val decision = decideNextAction(item, events.size, events.maxOfOrNull { it.wornAt })
            if (decision.action == item.status) null else item to decision
        }

    if (recommendations.isEmpty()) {
        Box(modifier = modifier) {
            Text(
                text = "Nothing to change right now.\nYour wardrobe looks fine.",
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
        items(recommendations, key = { it.first.id }) { (item, decision) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(item.id) }
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ItemPhoto(item = item, modifier = Modifier.size(56.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "now: ${item.status.label}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Next step: ${decision.action.label}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        decision.reasons.forEach { reason ->
                            Text(
                                text = "- $reason",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        auditActions.forEach { status ->
                            FilterChip(
                                selected = status == decision.action,
                                onClick = { onStatusChange(item.id, status) },
                                label = { Text(status.label) }
                            )
                        }
                    }
                }
            }
        }
    }
}
