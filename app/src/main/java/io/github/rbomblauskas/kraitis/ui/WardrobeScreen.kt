package io.github.rbomblauskas.kraitis.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.rbomblauskas.kraitis.data.ClothingCategory
import io.github.rbomblauskas.kraitis.data.ClothingCondition
import io.github.rbomblauskas.kraitis.data.ClothingItem
import io.github.rbomblauskas.kraitis.data.ClothingStatus
import io.github.rbomblauskas.kraitis.data.PhotoStore
import io.github.rbomblauskas.kraitis.data.Season
import io.github.rbomblauskas.kraitis.data.WearEvent
import io.github.rbomblauskas.kraitis.domain.costPerWearCents
import io.github.rbomblauskas.kraitis.domain.decideNextAction
import io.github.rbomblauskas.kraitis.ui.theme.KraitisTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class KraitisTab(val label: String) {
    WARDROBE("Wardrobe"),
    LOG_WEAR("Log wear"),
    TIPS("Tips")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeApp(viewModel: WardrobeViewModel) {
    val items by viewModel.items.collectAsState()
    val wearEvents by viewModel.wearEvents.collectAsState()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var selectedItemId by rememberSaveable { mutableStateOf<Long?>(null) }
    var tab by rememberSaveable { mutableStateOf(KraitisTab.WARDROBE) }

    WardrobeContent(
        items = items,
        wearEvents = wearEvents,
        selectedItemId = selectedItemId,
        showAddDialog = showAddDialog,
        tab = tab,
        onTabChange = { tab = it },
        // also jumps back to the wardrobe tab when an item is opened from tips
        onItemClick = {
            selectedItemId = it
            tab = KraitisTab.WARDROBE
        },
        onBackToList = { selectedItemId = null },
        onAddClick = { showAddDialog = true },
        onDismissAdd = { showAddDialog = false },
        onSaveItem = { form ->
            viewModel.addItem(form)
            showAddDialog = false
        },
        onStatusChange = viewModel::updateStatus,
        onGalleryPhoto = viewModel::setPhotoFromGallery,
        onCameraPhoto = viewModel::setPhotoFromCamera,
        onLogWear = viewModel::logWear
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WardrobeContent(
    items: List<ClothingItem>,
    wearEvents: List<WearEvent>,
    selectedItemId: Long?,
    showAddDialog: Boolean,
    tab: KraitisTab,
    onTabChange: (KraitisTab) -> Unit,
    onItemClick: (Long) -> Unit,
    onBackToList: () -> Unit,
    onAddClick: () -> Unit,
    onDismissAdd: () -> Unit,
    onSaveItem: (AddItemForm) -> Unit,
    onStatusChange: (Long, ClothingStatus) -> Unit,
    onGalleryPhoto: (Long, Uri) -> Unit,
    onCameraPhoto: (Long, String) -> Unit,
    onLogWear: (Long) -> Unit
) {
    val selectedItem = items.firstOrNull { it.id == selectedItemId }
    val detailOpen = tab == KraitisTab.WARDROBE && selectedItemId != null

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (detailOpen) {
                TopAppBar(
                    title = { Text("Item detail") },
                    navigationIcon = {
                        TextButton(onClick = onBackToList) {
                            Text("Back")
                        }
                    }
                )
            } else {
                val title = when (tab) {
                    KraitisTab.WARDROBE -> "Kraitis"
                    KraitisTab.LOG_WEAR -> "Log wear"
                    KraitisTab.TIPS -> "Recommendations"
                }
                TopAppBar(title = { Text(title) })
            }
        },
        bottomBar = {
            NavigationBar {
                KraitisTab.entries.forEach { entry ->
                    val icon = when (entry) {
                        KraitisTab.WARDROBE -> Icons.Filled.Home
                        KraitisTab.LOG_WEAR -> Icons.Filled.Check
                        KraitisTab.TIPS -> Icons.Filled.Star
                    }
                    NavigationBarItem(
                        selected = tab == entry,
                        onClick = { onTabChange(entry) },
                        icon = { Icon(icon, contentDescription = entry.label) },
                        label = { Text(entry.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (tab == KraitisTab.WARDROBE && selectedItemId == null) {
                FloatingActionButton(onClick = onAddClick) {
                    Text("+")
                }
            }
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (tab) {
            KraitisTab.WARDROBE -> if (selectedItemId == null) {
                WardrobeList(
                    items = items,
                    onItemClick = onItemClick,
                    modifier = contentModifier
                )
            } else {
                ItemDetail(
                    item = selectedItem,
                    wearEvents = wearEvents.filter { it.itemId == selectedItemId },
                    onBack = onBackToList,
                    onStatusChange = onStatusChange,
                    onGalleryPhoto = onGalleryPhoto,
                    onCameraPhoto = onCameraPhoto,
                    onLogWear = onLogWear,
                    modifier = contentModifier
                )
            }

            KraitisTab.LOG_WEAR -> Box(modifier = contentModifier) {
                Text(
                    text = "Coming soon",
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            KraitisTab.TIPS -> Box(modifier = contentModifier) {
                Text(
                    text = "Coming soon",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(
            onDismiss = onDismissAdd,
            onSave = onSaveItem
        )
    }
}

@Composable
private fun WardrobeList(
    items: List<ClothingItem>,
    onItemClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        EmptyWardrobe(modifier = modifier)
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items, key = { it.id }) { item ->
                ClothingRow(
                    item = item,
                    onClick = { onItemClick(item.id) }
                )
            }
        }
    }
}

@Composable
private fun EmptyWardrobe(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("No clothes saved yet")
            Text(
                text = "Add the first item to start auditing your wardrobe.",
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ClothingRow(
    item: ClothingItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemPhoto(item = item, size = 72.dp)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${item.category.label} - ${item.condition.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = item.status.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ItemDetail(
    item: ClothingItem?,
    wearEvents: List<WearEvent>,
    onBack: () -> Unit,
    onStatusChange: (Long, ClothingStatus) -> Unit,
    onGalleryPhoto: (Long, Uri) -> Unit,
    onCameraPhoto: (Long, String) -> Unit,
    onLogWear: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (item == null) {
        Box(modifier = modifier) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Item was not found")
                Button(onClick = onBack) {
                    Text("Back to wardrobe")
                }
            }
        }
        return
    }

    val pickPhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onGalleryPhoto(item.id, uri)
        }
    }

    val wearCount = wearEvents.size
    val lastWornAt = wearEvents.maxOfOrNull { it.wornAt }
    val decision = decideNextAction(item, wearCount, lastWornAt)
    val costPerWear = costPerWearCents(item.priceCents, wearCount)

    val context = LocalContext.current
    val photoStore = remember { PhotoStore(context.applicationContext) }
    // path is remembered here because the camera result only says true/false
    var cameraPhotoPath by rememberSaveable { mutableStateOf<String?>(null) }
    val takePhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { saved ->
        val path = cameraPhotoPath
        if (path != null) {
            if (saved) {
                onCameraPhoto(item.id, path)
            } else {
                File(path).delete()
            }
        }
        cameraPhotoPath = null
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = item.status.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item.photoPath?.let { path ->
                    AsyncImage(
                        model = File(path),
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            pickPhoto.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Pick photo")
                    }
                    OutlinedButton(
                        onClick = {
                            val file = photoStore.newPhotoFile()
                            cameraPhotoPath = file.absolutePath
                            takePhoto.launch(photoStore.contentUri(file))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Take photo")
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailRow("Category", item.category.label)
                    DetailRow("Condition", item.condition.label)
                    DetailRow("Season", item.season.label)
                    DetailRow("Sentimental", if (item.sentimental) "yes" else "no")
                    DetailRow("Price", item.priceCents?.let { formatPrice(it) } ?: "-")
                    DetailRow("Worn", "${wearCount}x")
                    DetailRow("Last worn", lastWornAt?.let { formatDate(it) } ?: "never")
                    DetailRow("Cost per wear", costPerWear?.let { formatPrice(it) } ?: "-")
                }
            }
        }

        item {
            Button(
                onClick = { onLogWear(item.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Log wear today", style = MaterialTheme.typography.titleMedium)
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Next step: ${decision.action.label}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    decision.reasons.forEach { reason ->
                        Text("- $reason", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Change status",
                    style = MaterialTheme.typography.titleMedium
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClothingStatus.entries.forEach { status ->
                        FilterChip(
                            selected = item.status == status,
                            onClick = { onStatusChange(item.id, status) },
                            label = { Text(status.label) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(value)
    }
}

@Composable
private fun AddItemDialog(
    onDismiss: () -> Unit,
    onSave: (AddItemForm) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(ClothingCategory.TOP) }
    var condition by rememberSaveable { mutableStateOf(ClothingCondition.GOOD) }
    var price by rememberSaveable { mutableStateOf("") }
    var season by rememberSaveable { mutableStateOf(Season.ALL_YEAR) }
    var sentimental by rememberSaveable { mutableStateOf(false) }
    val canSave = name.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add clothing item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true
                )
                DropdownField(
                    label = "Category",
                    selected = category,
                    options = ClothingCategory.entries,
                    optionLabel = { it.label },
                    onSelect = { category = it }
                )
                DropdownField(
                    label = "Condition",
                    selected = condition,
                    options = ClothingCondition.entries,
                    optionLabel = { it.label },
                    onSelect = { condition = it }
                )
                DropdownField(
                    label = "Season",
                    selected = season,
                    options = Season.entries,
                    optionLabel = { it.label },
                    onSelect = { season = it }
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price optional") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Row(
                    modifier = Modifier.clickable { sentimental = !sentimental },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = sentimental,
                        onCheckedChange = { sentimental = it }
                    )
                    Text("Sentimental value")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(AddItemForm(name, category, condition, price, season, sentimental))
                },
                enabled = canSave
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownField(
    label: String,
    selected: T,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = optionLabel(selected),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun formatPrice(priceCents: Long): String {
    val whole = priceCents / 100
    val cents = (priceCents % 100).toString().padStart(2, '0')
    return "$whole.$cents"
}

private fun formatDate(timeMillis: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timeMillis))

@Preview(showBackground = true)
@Composable
private fun WardrobeContentPreview() {
    KraitisTheme {
        WardrobeContent(
            items = listOf(
                ClothingItem(
                    id = 1,
                    name = "Black hoodie",
                    category = ClothingCategory.TOP,
                    condition = ClothingCondition.GOOD,
                    priceCents = 2499,
                    status = ClothingStatus.ACTIVE
                )
            ),
            wearEvents = emptyList(),
            selectedItemId = null,
            showAddDialog = false,
            tab = KraitisTab.WARDROBE,
            onTabChange = {},
            onItemClick = {},
            onBackToList = {},
            onAddClick = {},
            onDismissAdd = {},
            onSaveItem = {},
            onStatusChange = { _, _ -> },
            onGalleryPhoto = { _, _ -> },
            onCameraPhoto = { _, _ -> },
            onLogWear = {}
        )
    }
}
