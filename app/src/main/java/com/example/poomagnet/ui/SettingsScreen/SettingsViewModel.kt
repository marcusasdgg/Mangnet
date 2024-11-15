package com.example.poomagnet.ui.SettingsScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.poomagnet.mangaRepositoryManager.MangaRepositoryManager
import dagger.hilt.android.lifecycle.HiltViewModel
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
}
