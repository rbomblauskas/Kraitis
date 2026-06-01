package io.github.rbomblauskas.kraitis.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.rbomblauskas.kraitis.data.ClothingDao
import io.github.rbomblauskas.kraitis.data.ClothingItem
import io.github.rbomblauskas.kraitis.data.ClothingStatus
import java.math.RoundingMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WardrobeViewModel(
    private val clothingDao: ClothingDao
) : ViewModel() {
    val items: StateFlow<List<ClothingItem>> = clothingDao.getAllItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addItem(
        name: String,
        category: String,
        condition: String,
        priceText: String
    ) {
        val trimmedName = name.trim()
        val trimmedCategory = category.trim()
        val trimmedCondition = condition.trim()

        if (trimmedName.isBlank() || trimmedCategory.isBlank() || trimmedCondition.isBlank()) {
            return
        }

        viewModelScope.launch {
            clothingDao.insert(
                ClothingItem(
                    name = trimmedName,
                    category = trimmedCategory,
                    condition = trimmedCondition,
                    priceCents = parsePriceCents(priceText)
                )
            )
        }
    }

    fun updateStatus(itemId: Long, status: ClothingStatus) {
        viewModelScope.launch {
            clothingDao.updateStatus(itemId, status)
        }
    }

    private fun parsePriceCents(priceText: String): Long? {
        val normalizedPrice = priceText.trim().replace(',', '.')
        if (normalizedPrice.isBlank()) return null

        return normalizedPrice
            .toBigDecimalOrNull()
            ?.movePointRight(2)
            ?.setScale(0, RoundingMode.HALF_UP)
            ?.toLong()
    }
}

class WardrobeViewModelFactory(
    private val clothingDao: ClothingDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WardrobeViewModel::class.java)) {
            return WardrobeViewModel(clothingDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
