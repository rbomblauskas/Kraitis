package io.github.rbomblauskas.kraitis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.rbomblauskas.kraitis.data.KraitisDatabase
import io.github.rbomblauskas.kraitis.ui.WardrobeApp
import io.github.rbomblauskas.kraitis.ui.WardrobeViewModel
import io.github.rbomblauskas.kraitis.ui.WardrobeViewModelFactory
import io.github.rbomblauskas.kraitis.ui.theme.KraitisTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val clothingDao = KraitisDatabase.getDatabase(applicationContext).clothingDao()
        val viewModelFactory = WardrobeViewModelFactory(clothingDao)

        setContent {
            KraitisTheme {
                val wardrobeViewModel: WardrobeViewModel = viewModel(factory = viewModelFactory)
                WardrobeApp(viewModel = wardrobeViewModel)
            }
        }
    }
}
