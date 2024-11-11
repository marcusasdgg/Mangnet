package com.example.poomagnet.App

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.poomagnet.MyWorker
import com.example.poomagnet.mangaDex.dexApiService.MangaDexRepository
import com.example.poomagnet.mangaRepositoryManager.MangaRepositoryManager
import com.example.poomagnet.ui.MangaSpecific.MangaWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class pooMagnet() : Application(), Configuration.Provider{
    @Inject
    lateinit var work: CustomWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setWorkerFactory(work)
            .build()

}

class CustomWorkerFactory @Inject constructor(private val api: MangaRepositoryManager): WorkerFactory(){
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? =
        when (workerClassName) {
            MyWorker::class.java.name -> MyWorker(appContext, workerParameters, api)
            MangaWorker::class.java.name -> MangaWorker(appContext, workerParameters, api)
            else -> null // If workerClassName doesn't match any of your workers
        }
}
