package io.github.rbomblauskas.kraitis.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.rbomblauskas.kraitis.data.ClothingCategory
import io.github.rbomblauskas.kraitis.data.ClothingCondition
import io.github.rbomblauskas.kraitis.data.ClothingDao
import io.github.rbomblauskas.kraitis.data.ClothingItem
import io.github.rbomblauskas.kraitis.data.ClothingStatus
import io.github.rbomblauskas.kraitis.data.PhotoStore
import io.github.rbomblauskas.kraitis.data.Season
import io.github.rbomblauskas.kraitis.data.WearEvent
import io.github.rbomblauskas.kraitis.data.WearEventDao
import java.math.RoundingMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AddItemForm(
    val name: String,
    val category: ClothingCategory,
    val condition: ClothingCondition,
    val priceText: String,
    val season: Season,
    val sentimental: Boolean
)

class WardrobeViewModel(
    private val clothingDao: ClothingDao,
    private val wearEventDao: WearEventDao,
    private val photoStore: PhotoStore
) : ViewModel() {
    val items: StateFlow<List<ClothingItem>> = clothingDao.getAllItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val wearEvents: StateFlow<List<WearEvent>> = wearEventDao.getAllEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun logWear(itemId: Long) {
        viewModelScope.launch {
            wearEventDao.insert(WearEvent(itemId = itemId, wornAt = System.currentTimeMillis()))
        }
    }

    fun addItem(form: AddItemForm) {
        val trimmedName = form.name.trim()
        if (trimmedName.isBlank()) {
            return
        }

        viewModelScope.launch {
            clothingDao.insert(
                ClothingItem(
                    name = trimmedName,
                    category = form.category,
                    condition = form.condition,
                    priceCents = parsePriceCents(form.priceText),
                    season = form.season,
                    sentimental = form.sentimental
                )
            )
        }
    }

    fun updateStatus(itemId: Long, status: ClothingStatus) {
        viewModelScope.launch {
            clothingDao.updateStatus(itemId, status)
        }
    }

    fun setPhotoFromGallery(itemId: Long, uri: Uri) {
        viewModelScope.launch {
            val oldPath = items.value.firstOrNull { it.id == itemId }?.photoPath
            val newPath = photoStore.copyFromUri(uri) ?: return@launch
            clothingDao.updatePhoto(itemId, newPath)
            photoStore.delete(oldPath)
        }
    }

    // camera already wrote the file, just remember the path
    fun setPhotoFromCamera(itemId: Long, path: String) {
        viewModelScope.launch {
            val oldPath = items.value.firstOrNull { it.id == itemId }?.photoPath
            clothingDao.updatePhoto(itemId, path)
            photoStore.delete(oldPath)
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
    private val clothingDao: ClothingDao,
    private val wearEventDao: WearEventDao,
    private val photoStore: PhotoStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WardrobeViewModel::class.java)) {
            return WardrobeViewModel(clothingDao, wearEventDao, photoStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
