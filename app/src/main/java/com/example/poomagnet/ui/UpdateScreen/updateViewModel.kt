package com.example.poomagnet.ui.UpdateScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.poomagnet.mangaRepositoryManager.MangaInfo
import com.example.poomagnet.mangaRepositoryManager.MangaRepositoryManager
import com.example.poomagnet.mangaRepositoryManager.SlimChapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class updateViewModel @Inject constructor(
    private val repo: MangaRepositoryManager
): ViewModel() {
    val _uiState: MutableStateFlow<updateUiState> = MutableStateFlow(updateUiState())
    val uiState: StateFlow<updateUiState> = _uiState

    fun syncLibrary(){
        val list = repo.newUpdatedChapters
        val newMap: MutableMap<String, MutableList<SlimChapter>> = mutableMapOf()
        for (i in list){
            Log.d("TAG", "syncLibrary: $i")
            if (newMap.contains(i.first.toString())){
                newMap[i.first.toString()]!!.add(i.second)
            } else {
                newMap[i.first.toString()] = mutableListOf(i.second)
            }
        }
        _uiState.update {
            it.copy(
                showList = newMap
            )
        }
    }

    fun findMangaInLibrary(id: String): MangaInfo? {
        return repo.getMangaById(id)
    }

    suspend fun loadImageFromLibrary(mangaId: String, coverUrl: String): String{
        return repo.getImageUri(mangaId, coverUrl)
    }


    suspend fun performUpdate(){
        repo.updateLibrary()
        syncLibrary()
    }

}