package io.github.rbomblauskas.kraitis.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.rbomblauskas.kraitis.data.ClothingCategory
import io.github.rbomblauskas.kraitis.data.ClothingCondition
import io.github.rbomblauskas.kraitis.data.ClothingItem
import io.github.rbomblauskas.kraitis.data.ClothingStatus
import io.github.rbomblauskas.kraitis.ui.theme.KraitisTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeApp(viewModel: WardrobeViewModel) {
    val items by viewModel.items.collectAsState()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var selectedItemId by rememberSaveable { mutableStateOf<Long?>(null) }

    WardrobeContent(
        items = items,
        selectedItemId = selectedItemId,
        showAddDialog = showAddDialog,
        onItemClick = { selectedItemId = it },
        onBackToList = { selectedItemId = null },
        onAddClick = { showAddDialog = true },
        onDismissAdd = { showAddDialog = false },
        onSaveItem = { name, category, condition, price ->
            viewModel.addItem(name, category, condition, price)
            showAddDialog = false
        },
        onStatusChange = viewModel::updateStatus
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WardrobeContent(
    items: List<ClothingItem>,
    selectedItemId: Long?,
    showAddDialog: Boolean,
    onItemClick: (Long) -> Unit,
    onBackToList: () -> Unit,
    onAddClick: () -> Unit,
    onDismissAdd: () -> Unit,
    onSaveItem: (String, ClothingCategory, ClothingCondition, String) -> Unit,
    onStatusChange: (Long, ClothingStatus) -> Unit
) {
    val selectedItem = items.firstOrNull { it.id == selectedItemId }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (selectedItemId == null) {
                TopAppBar(title = { Text("Kraitis") })
            } else {
                TopAppBar(
                    title = { Text("Item detail") },
                    navigationIcon = {
                        TextButton(onClick = onBackToList) {
                            Text("Back")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (selectedItemId == null) {
                FloatingActionButton(onClick = onAddClick) {
                    Text("+")
                }
            }
        }
    ) { innerPadding ->
        if (selectedItemId == null) {
            WardrobeList(
                items = items,
                onItemClick = onItemClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            ItemDetail(
                item = selectedItem,
                onBack = onBackToList,
                onStatusChange = onStatusChange,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
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
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = item.status.label,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Text("${item.category.label} - ${item.condition.label}")
            item.priceCents?.let { priceCents ->
                Text("Price: ${formatPrice(priceCents)}")
            }
        }
    }
}

@Composable
private fun ItemDetail(
    item: ClothingItem?,
    onBack: () -> Unit,
    onStatusChange: (Long, ClothingStatus) -> Unit,
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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailRow("Category", item.category.label)
                    DetailRow("Condition", item.condition.label)
                    DetailRow("Price", item.priceCents?.let { formatPrice(it) } ?: "-")
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
    onSave: (String, ClothingCategory, ClothingCondition, String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(ClothingCategory.TOP) }
    var condition by rememberSaveable { mutableStateOf(ClothingCondition.GOOD) }
    var price by rememberSaveable { mutableStateOf("") }
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
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price optional") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, category, condition, price) },
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
            selectedItemId = null,
            showAddDialog = false,
            onItemClick = {},
            onBackToList = {},
            onAddClick = {},
            onDismissAdd = {},
            onSaveItem = { _, _, _, _ -> },
            onStatusChange = { _, _ -> }
        )
    }
}
