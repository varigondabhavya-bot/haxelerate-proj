package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.BrandShieldDatabase
import com.example.data.repository.BrandShieldRepository
import com.example.presentation.BrandShieldAppShell
import com.example.presentation.BrandShieldViewModel
import com.example.ui.theme.BrandShieldTheme
import com.example.utils.ThreatNotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThreatNotificationHelper.createNotificationChannel(applicationContext)

        val database = BrandShieldDatabase.getDatabase(applicationContext)
        val repository = BrandShieldRepository(database.dao())
        val openThreatId = intent?.getStringExtra("OPEN_THREAT_ID")

        setContent {
            val viewModel: BrandShieldViewModel = viewModel(
                factory = BrandShieldViewModel.provideFactory(repository)
            )
            if (openThreatId != null) {
                viewModel.dismissSplashNow()
                viewModel.openThreatDetail(openThreatId)
            }
            val darkTheme by viewModel.darkTheme.collectAsStateWithLifecycle()

            BrandShieldTheme(darkTheme = darkTheme) {
                BrandShieldAppShell(viewModel = viewModel)
            }
        }
    }
}
