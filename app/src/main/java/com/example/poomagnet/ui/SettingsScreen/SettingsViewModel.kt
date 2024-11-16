package com.example.poomagnet.ui.SettingsScreen

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import bt.Bt
import bt.data.file.FileSystemStorage
import bt.runtime.Config
import com.example.poomagnet.mangaRepositoryManager.MangaRepositoryManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: MangaRepositoryManager
) : ViewModel() {
    fun getBackUp(): String {
        val s =  repo.getBackUpFromFile()
        Log.d("TAG", "getBackUp: $s")
        return s
    }

    fun restoreBackup(backup: String){
        repo.loadFromBackUp(backup)
    }

    suspend fun tryDownload(uri: Uri, url: String){
        val context = repo.getContext()
        val file = withContext(Dispatchers.IO) {
            val s = File(context.cacheDir, "tempDownload")
            return@withContext s
        }

        val client = Bt.client()
            .magnet(url)
            .storage(FileSystemStorage(file))
            .config(Config())
            .autoLoadModules()
            .module(dhtModule)
            .stopWhenDownloaded()
            .build()

        client.startAsync().join()

        withContext(Dispatchers.IO){
            val contentResolver = context.contentResolver
            val outputStream = contentResolver.openOutputStream(uri)
            val inputstream = file.inputStream()

            outputStream?.let {
                inputstream.copyTo(it)
                it.flush()
                it.close()
            }

            inputstream.close()
        }
        file.delete()
    }
}
