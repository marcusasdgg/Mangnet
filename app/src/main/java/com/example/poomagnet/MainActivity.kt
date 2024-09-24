package com.example.poomagnet

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
        ActivityCompat.requestPermissions(this,
            listOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE).toTypedArray(),
            PackageManager.PERMISSION_GRANTED);
        enableEdgeToEdge()
        setContent {
            AppTheme {
                App()
            }
        }


        scheduleDailyWorker()
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
