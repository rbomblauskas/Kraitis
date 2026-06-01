package io.github.rbomblauskas.kraitis.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
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
import io.github.rbomblauskas.kraitis.data.ClothingItem
import io.github.rbomblauskas.kraitis.data.ClothingStatus
import io.github.rbomblauskas.kraitis.ui.theme.KraitisTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeApp(viewModel: WardrobeViewModel) {
    val items by viewModel.items.collectAsState()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    WardrobeContent(
        items = items,
        showAddDialog = showAddDialog,
        onAddClick = { showAddDialog = true },
        onDismissAdd = { showAddDialog = false },
        onSaveItem = { name, category, condition, price ->
            viewModel.addItem(name, category, condition, price)
            showAddDialog = false
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WardrobeContent(
    items: List<ClothingItem>,
    showAddDialog: Boolean,
    onAddClick: () -> Unit,
    onDismissAdd: () -> Unit,
    onSaveItem: (String, String, String, String) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Kraitis") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Text("+")
            }
        }
    ) { innerPadding ->
        WardrobeList(
            items = items,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
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
                ClothingRow(item = item)
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
private fun ClothingRow(item: ClothingItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
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
            Text("${item.category} - ${item.condition}")
            item.priceCents?.let { priceCents ->
                Text("Price: ${formatPrice(priceCents)}")
            }
        }
    }
}

@Composable
private fun AddItemDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var condition by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    val canSave = name.isNotBlank() && category.isNotBlank() && condition.isNotBlank()

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
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = condition,
                    onValueChange = { condition = it },
                    label = { Text("Condition") },
                    singleLine = true
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
                    category = "hoodie",
                    condition = "good",
                    priceCents = 2499,
                    status = ClothingStatus.ACTIVE
                )
            ),
            showAddDialog = false,
            onAddClick = {},
            onDismissAdd = {},
            onSaveItem = { _, _, _, _ -> }
        )
    }
}
