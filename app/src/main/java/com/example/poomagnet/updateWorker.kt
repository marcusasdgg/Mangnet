package com.example.poomagnet

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.poomagnet.mangaDex.dexApiService.MangaDexRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltWorker
class MyWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @Assisted private val mangaDexRepository: MangaDexRepository
) : CoroutineWorker(appContext, workerParams) {

    init {
        Log.d("MyWorker", "MyWorker instantiated with params: $workerParams")
    }
    override suspend fun doWork(): Result {
        return try {
            updateFunction()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun updateFunction() {
        mangaDexRepository.updateWholeLibrary()
    }
}