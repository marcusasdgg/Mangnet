package com.example.poomagnet.ui.MangaSpecific

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poomagnet.App.ScreenType
import com.example.poomagnet.mangaDex.dexApiService.MangaDexRepository
import com.example.poomagnet.mangaDex.dexApiService.MangaInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    fun addToLibrary() {
        val currentManga = uiState.value.currentManga

        if (currentManga != null) {
            // Toggle the inLibrary value
            val newManga = currentManga.copy(inLibrary = !currentManga.inLibrary)

            // Launch coroutine to handle state update and repository operation sequentially
            this.viewModelScope.launch {
                // Add or remove manga from the library based on the toggled value
                if (newManga.inLibrary) {
                    mangaDexRepository.addToLibrary(newManga)
                } else {
                    mangaDexRepository.removeFromLibrary(currentManga)
                }
            }
                // Update the UI state inside the coroutine to ensure consistency
            _uiState.update { it.copy(currentManga = newManga) }
            // Log the new state
        } else {
            Log.d("TAG", "addToLibrary: manga was null???")
        }
    }

}