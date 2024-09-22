package com.example.poomagnet.ui.UpdateScreen

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.poomagnet.mangaDex.dexApiService.MangaDexRepository
import com.example.poomagnet.mangaDex.dexApiService.MangaInfo
import com.example.poomagnet.mangaDex.dexApiService.slimChapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class updateViewModel @Inject constructor(
    private val mangaDexRepository: MangaDexRepository
): ViewModel() {
    val _uiState: MutableStateFlow<updateUiState> = MutableStateFlow(updateUiState())
    val uiState: StateFlow<updateUiState> = _uiState

    fun syncLibrary(){
        val list = mangaDexRepository.newUpdatedChapters
        val newMap: MutableMap<String, MutableList<slimChapter>> = mutableMapOf()
        for (i in list){
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
        val library = mangaDexRepository.library
        val manga = library.firstOrNull { el ->
            el.id == id
        }
        return manga
    }


    suspend fun performUpdate(){
        mangaDexRepository.updateWholeLibrary()
        syncLibrary()
    }

}