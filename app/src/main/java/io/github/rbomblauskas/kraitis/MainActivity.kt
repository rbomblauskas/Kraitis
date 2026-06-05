package io.github.rbomblauskas.kraitis

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.rbomblauskas.kraitis.data.KraitisDatabase
import io.github.rbomblauskas.kraitis.data.PhotoStore
import io.github.rbomblauskas.kraitis.reminders.scheduleDailyReminder
import io.github.rbomblauskas.kraitis.ui.WardrobeApp
import io.github.rbomblauskas.kraitis.ui.WardrobeViewModel
import io.github.rbomblauskas.kraitis.ui.WardrobeViewModelFactory
import io.github.rbomblauskas.kraitis.ui.theme.KraitisTheme

class MainActivity : ComponentActivity() {
    // result does not matter, the worker checks the permission anyway
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        scheduleDailyReminder(applicationContext)

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
