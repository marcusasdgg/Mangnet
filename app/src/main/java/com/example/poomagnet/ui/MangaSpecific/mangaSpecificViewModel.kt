package com.example.poomagnet.ui.MangaSpecific

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poomagnet.App.ScreenType
import com.example.poomagnet.mangaDex.dexApiService.MangaDexRepository
import com.example.poomagnet.mangaDex.dexApiService.MangaInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MangaSpecificViewModel @Inject constructor( private val mangaDexRepository: MangaDexRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(mangaUiState())
    val uiState: StateFlow<mangaUiState> = _uiState

    fun selectCurrentManga(manga: MangaInfo?){
        if (manga == null){ //shouldnt happen?
            _uiState.update{
                it.copy(
                    currentManga = null,
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    currentManga = manga,
                )
            }
        }

    }

    fun makeVisible(boolean: Boolean){
        _uiState.update {
            it.copy(
                visible = boolean
            )
        }
    }

    //need to add extra function to download image and store it.
    fun addToLibrary(){
        if (uiState.value.currentManga !== null && !uiState.value.currentManga!!.inLibrary){
            val newManga = uiState.value.currentManga?.copy(inLibrary = true)
            this.viewModelScope.launch {
                if (newManga !== null){
                    mangaDexRepository.addToLibrary(newManga)
                }
            }
            _uiState.update {
                it.copy(
                    currentManga = newManga
                )
            }
        } else {
            this.viewModelScope.launch {
                mangaDexRepository.removeFromLibrary(uiState.value.currentManga)
            }
            _uiState.update {
                it.copy(
                    currentManga = uiState.value.currentManga?.copy(inLibrary = false)
                )
            }
        }
    }

}