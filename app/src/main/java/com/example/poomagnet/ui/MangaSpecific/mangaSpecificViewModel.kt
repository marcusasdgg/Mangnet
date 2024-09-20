package com.example.poomagnet.ui.MangaSpecific

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poomagnet.App.ScreenType
import com.example.poomagnet.mangaDex.dexApiService.Chapter
import com.example.poomagnet.mangaDex.dexApiService.MangaDexRepository
import com.example.poomagnet.mangaDex.dexApiService.MangaInfo
import com.example.poomagnet.mangaDex.dexApiService.isOnline
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.util.Date
import javax.inject.Inject

//make sure to not overwrite the old chapter list objects and add the new ones in.
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

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getChapterUrls(chapterId: String){
        val new_chapter = uiState.value.currentManga?.chapterList?.second?.map { elm ->
            if (elm.id == chapterId && (elm.contents == null || elm.contents.isOnline)){
                val s =  mangaDexRepository.getChapterContents(chapterId)
                Log.d("TAG", "getChapterUrls: $s")
                elm.copy(contents =s)
            }else {
                elm
            }
        }

        _uiState.update {
            it.copy(
                currentManga = it.currentManga?.copy(chapterList = it.currentManga.chapterList?.copy(second = new_chapter?: listOf()))
            )
        }
    }

    fun makeVisible(boolean: Boolean){
        _uiState.update {
            it.copy(
                visible = boolean
            )
        }
    }

    fun enterReadMode(boolean: Boolean, curr: Chapter?){
        if (boolean){
            _uiState.update {
                it.copy(
                    inReadMode = boolean,
                    currentChapter = curr
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    inReadMode = boolean,
                    currentChapter = null,
                )
            }
        }

    }

    //need to add extra function to download image and store it.
    fun addToLibrary() {
        val currentManga = uiState.value.currentManga

        if (currentManga != null) {
            // Toggle the inLibrary value
            val newManga = currentManga.copy(inLibrary = !currentManga.inLibrary)
            val TAG = "TAG"
            Log.d(TAG, "addToLibrary: current state: ${currentManga.inLibrary}")
            Log.d(TAG, "addToLibrary: new state: ${newManga.inLibrary}")
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

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getChapterInfo(){
        val id = uiState.value.currentManga?.id
        Log.d("TAG", "id passed was this: \"$id\"")
        if (id !== null){
            val chapterlist = mangaDexRepository.chapList(id)
            _uiState.update {
                it.copy(
                    currentManga = it.currentManga?.copy(chapterList = Pair(Date(),chapterlist.first))
                )
            }
        }
    }


}

//when moving to read chapter view, we need to edit the app.kt's backhandler such that
//it now navigates us back to the mangaScreen page? Since backhandler is triggerred by
//the leafiest element we can define a new backhandler in our newscreen that will block the old
// one. The entire reading experience must be stored in mangaSpecificViewModel though.