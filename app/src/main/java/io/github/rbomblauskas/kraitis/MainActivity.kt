package io.github.rbomblauskas.kraitis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.rbomblauskas.kraitis.data.KraitisDatabase
import io.github.rbomblauskas.kraitis.data.PhotoStore
import io.github.rbomblauskas.kraitis.ui.WardrobeApp
import io.github.rbomblauskas.kraitis.ui.WardrobeViewModel
import io.github.rbomblauskas.kraitis.ui.WardrobeViewModelFactory
import io.github.rbomblauskas.kraitis.ui.theme.KraitisTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = KraitisDatabase.getDatabase(applicationContext)
        val photoStore = PhotoStore(applicationContext)
        val viewModelFactory = WardrobeViewModelFactory(
            database.clothingDao(),
            database.wearEventDao(),
            photoStore
        )

        setContent {
            KraitisTheme {
                val wardrobeViewModel: WardrobeViewModel = viewModel(factory = viewModelFactory)
                WardrobeApp(viewModel = wardrobeViewModel)
            }
        }
    }
}
