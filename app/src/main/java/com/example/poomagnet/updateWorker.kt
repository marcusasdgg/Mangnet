package com.example.poomagnet

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.poomagnet.mangaDex.dexApiService.MangaDexRepository

class MyWorker(private val appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            updateFunction()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun updateFunction() {
        // Your update logic here
        MangaDexRepository(appContext).updateWholeLibrary()
    }
}