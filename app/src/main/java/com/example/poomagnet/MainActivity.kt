package com.example.poomagnet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.poomagnet.App.App
import com.example.poomagnet.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                App(activityResultLauncher)
            }
        }


        scheduleDailyWorker()
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()){ isGranted ->
            // Handle Permission granted/rejected
            if (isGranted) {
                // Permission is granted
            } else {
                // Permission is denied
            }
        }


    private fun scheduleDailyWorker() {
        val workRequest = PeriodicWorkRequestBuilder<MyWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(10, TimeUnit.SECONDS) // Delay until next run if needed
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "DailyUpdateWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }



}
