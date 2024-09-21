package com.example.poomagnet.ui.MangaSpecific

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poomagnet.App.ScreenType
import com.example.poomagnet.mangaDex.dexApiService.Chapter
import com.example.poomagnet.mangaDex.dexApiService.ChapterContents
import com.example.poomagnet.mangaDex.dexApiService.MangaDexRepository
import com.example.poomagnet.mangaDex.dexApiService.MangaInfo
import com.example.poomagnet.mangaDex.dexApiService.isOnline
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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

    fun orderManga(){
        _uiState.update { ot ->
            ot.copy(
                currentManga = ot.currentManga?.copy(
                    chapterList = ot.currentManga.chapterList?.copy(
                        second = ot.currentManga.chapterList?.second?.sortedWith(
                            compareByDescending<Chapter> { it.chapter }.thenByDescending { it.volume }
                        )?: listOf()
                    )
                )
            )
        }
    }

    fun filterSame(){
        _uiState.update {
            ot ->
            ot.copy(
                currentManga = ot.currentManga?.copy(
                    chapterList = ot.currentManga.chapterList?.copy(
                        second = ot.currentManga.chapterList?.second?.filter { it.pageCount > 0 }?: listOf()
                    )

                )
            )
        }
        _uiState.update { ot ->
            ot.copy(
                currentManga = ot.currentManga?.copy(
                    chapterList = ot.currentManga.chapterList?.copy(
                        second = ot.currentManga.chapterList?.second?.distinctBy { it.chapter }?: listOf()
                        )

                )
            )
        }
    }

    fun toggleReadBar(boolean: Boolean = !_uiState.value.readBarVisible){
        _uiState.update {
            it.copy(
                readBarVisible = boolean
            )
        }
    }

    fun toggleHomeBar(boolean: Boolean = !_uiState.value.homeBarVisible){
        _uiState.update {
            it.copy(
                homeBarVisible = boolean
            )
        }
    }


    fun updateCurrentpage(page: Int){
        _uiState.update{
            it.copy(
                currentPage = page
            )
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getChapterUrls(chapterId: String){
        var t: Chapter? = null;
        val new_chapter = uiState.value.currentManga?.chapterList?.second?.map { elm ->
            if (elm.id == chapterId && (elm.contents == null || elm.contents.isOnline)){
                val s =  mangaDexRepository.getChapterContents(chapterId)
                Log.d("TAG", "getChapterUrls: $s")
                t = elm.copy(contents =s)
                elm.copy(contents =s)
            }else {
                elm
            }
        }

        Log.d("TAG", "getChapterUrls: $new_chapter")

        _uiState.update {
            it.copy(
                currentManga = it.currentManga?.copy(chapterList = Pair(Date(),new_chapter?: listOf())),
                currentChapter = t?:it.currentChapter
            )
        }

        Log.d("TAG", "current one: ${_uiState.value.currentChapter}")
    }

    fun makeVisible(boolean: Boolean){
        _uiState.update {
            it.copy(
                visible = boolean
            )
        }
    }


    fun enterReadMode(boolean: Boolean){
        if (!boolean){
            _uiState.update {
                it.copy(
                    inReadMode = false,
                    currentChapter = null,
                    nextChapter = null,
                    previousChapter = null
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    inReadMode = true,
                )
            }
        }
    }
    fun setFlag(boolean: Boolean){
        _uiState.update {
            it.copy(
                nextFlag = boolean
            )
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
            val chapterList = mangaDexRepository.chapList(id)
            val list = uiState.value.currentManga?.chapterList?.second?.toMutableList()
            if (list !== null && uiState.value.currentManga?.inLibrary == true){
                for (i in chapterList.first){
                    if (!list.any{elm -> elm.id == i.id}){
                        list.add(i)
                    }
                }
                _uiState.update {
                    it.copy(
                        currentManga = it.currentManga?.copy(chapterList = Pair(Date(), list))
                    )
                }
                filterSame()
                orderManga()
                if (uiState.value.currentManga !== null){
                    mangaDexRepository.updateInLibrary(uiState.value.currentManga!!)
                }
                return
            } else {
                Log.d("TAG", "getChapterInfo: Chapter is not in library")
            }

            _uiState.update {
                it.copy(
                    currentManga = it.currentManga?.copy(chapterList = Pair(Date(),chapterList.first))
                )
            }
            filterSame()
            orderManga()
        }
    }

    suspend fun getNextChapter(context: Context){
        val currentChapter = uiState.value.currentChapter?.chapter
        val currentVolume = uiState.value.currentChapter?.volume
        if (currentVolume !== null && currentChapter !== null){

            var firstNextChapter = uiState.value.currentManga?.chapterList?.second?.reversed()?.firstOrNull { elm  -> elm.chapter > currentChapter }
            Log.d("TAG", "getNextChapter: next chapter is $firstNextChapter")

            if (firstNextChapter !== null){
                val chapterUrls = mangaDexRepository.getChapterContents(firstNextChapter.id)
                Log.d("TAG", "getNextChapter: $chapterUrls")
                if (chapterUrls is ChapterContents.Online){
                    firstNextChapter = firstNextChapter.copy(contents = chapterUrls)
                    for (i in chapterUrls.imagePaths){
                        preloadImage(context, i.first)
                    }
                }
                _uiState.update {
                    it.copy(
                        nextChapter = firstNextChapter
                    )
                }
            }
            Log.d("TAG", "getNextChapter: finished $firstNextChapter")
        }
    }

    fun readPage(pageNum: Int){
        val currentChapter = uiState.value.currentChapter?.copy(lastPageRead = pageNum)
        _uiState.update {
            it.copy(
                currentChapter = currentChapter
            )
        }
    }

    fun markThisAsDone(){

        val currentChapter = uiState.value.currentChapter?.copy(finished = true)
        if (currentChapter !== null){
            Log.d("TAG", "markThisAsDone: ${currentChapter.chapter}")
            val t  = uiState.value.currentManga?.chapterList?.second?.map { elm ->
                if (elm.id == currentChapter.id){
                    currentChapter
                } else {
                    elm
                }
            }
            if (t !== null){
                Log.d("TAG", "newChapList: ${t.firstOrNull{elm -> elm.finished}}")
                _uiState.update {
                    it.copy(currentManga = it.currentManga?.copy(chapterList = it.currentManga.chapterList?.copy(second = t)))
                }
                Log.d("TAG", "In actual manga entry markThisAsDone: ${_uiState.value.currentManga?.chapterList?.second?.firstOrNull { elm -> elm.finished }}")
                viewModelScope.launch {
                    updateLibraryEquivalent()
                }
            }

        }
    }

    suspend fun updateLibraryEquivalent(){
        val currentManga = uiState.value.currentManga
        if (currentManga !== null && currentManga.inLibrary ){
            Log.d("TAG", "updateLibraryEquivalent: updating library\n${currentManga}")
            mangaDexRepository.updateInLibrary(currentManga)
        }
    }

    fun latestUnReadChapter(){
        var latestRead =
            uiState.value.currentManga?.chapterList?.second?.indexOfFirst { elm ->

                if (elm.finished){
                    Log.d("TAG", "latestUnReadChapter: $elm")
                }
                return@indexOfFirst elm.finished
            }
                ?.plus(-1)

        if (latestRead == null || latestRead == 0){
            latestRead = 0
        }

        Log.d("TAG", "attempting to retrieve chapter at index $latestRead")

        val chapId = uiState.value.currentManga?.chapterList?.second?.getOrNull(latestRead)?.id

        if (chapId !== null){
            _uiState.update {
                it.copy(
                    latestChapterReadId = chapId
                )
            }
        } else {
            Log.d("TAG", "latestUnReadChapter: wtf id not found")
            _uiState.update {
                it.copy(
                    latestChapterReadId = ""
                )
            }
        }



    }

    fun resetState(){
        _uiState.update {
            it.copy(
                currentPage = 0,
                nextChapter = null,
                previousChapter = null,
                nextFlag = false,
            )
        }
    }

    fun markAsDone(){
        val curr = uiState.value.currentChapter
        var s: Chapter? = null
        if (curr !== null && curr.contents !== null){
            when (curr.contents){
                is ChapterContents.Downloaded ->{
                    if (curr.lastPageRead == curr.pageCount.toInt()){
                        s = curr.copy(finished = true)
                    }
                }
                is ChapterContents.Online -> {
                    if (curr.lastPageRead == curr.pageCount.toInt()){
                        s = curr.copy(finished = true)
                    }
                }
            }
        }

        if (s !== null){
            val t  = uiState.value.currentManga?.chapterList?.second?.map { elm ->
                if (elm.id == s.id){
                    s
                } else {
                    elm
                }
            }
            if (t !== null){
                _uiState.update {
                    it.copy(currentManga = it.currentManga?.copy(chapterList = it.currentManga.chapterList?.copy(second = t)))
                }
            }
        }
    }

    suspend fun getPreviousChapter(context: Context){
        val currentChapter = uiState.value.currentChapter?.chapter
        val currentVolume = uiState.value.currentChapter?.volume
        if (currentVolume !== null && currentChapter !== null){
            var firstNextChapter = uiState.value.currentManga?.chapterList?.second?.firstOrNull { elm  -> elm.chapter < currentChapter }
            if (firstNextChapter !== null){
                val chapterUrls = mangaDexRepository.getChapterContents(firstNextChapter.id)
                Log.d("TAG", "getNextChapter: $chapterUrls")
                if (chapterUrls is ChapterContents.Online){
                    firstNextChapter = firstNextChapter.copy(contents = chapterUrls)
                    for (i in chapterUrls.imagePaths){
                        preloadImage(context, i.first)
                    }
                }
                _uiState.update {
                    it.copy(
                        previousChapter = firstNextChapter
                    )
                }
            }
        }
    }
    fun setPage(int: Int){
        _uiState.update {
            it.copy(
                currentPage = int
            )
        }
    }

    fun loadNextChapter(){
        Log.d("TAG", "loadNextChapter:started")
        _uiState.update {
            it.copy(
                previousChapter = it.currentChapter,
                currentChapter = it.nextChapter,
                nextChapter = null,
                currentPage = 1,
            )
        }
        Log.d("TAG", "loadNextChapter: ${uiState.value.currentChapter}")

    }




    fun loadPreviousChapter(){
        _uiState.update {
            it.copy(
                currentChapter = it.previousChapter,
                nextChapter = it.currentChapter
            )
        }
    }




}

//when moving to read chapter view, we need to edit the app.kt's backhandler such that
//it now navigates us back to the mangaScreen page? Since backhandler is triggerred by
//the leafiest element we can define a new backhandler in our newscreen that will block the old
// one. The entire reading experience must be stored in mangaSpecificViewModel though.

// redo teh horizontal pager so you instead hook on trying to gesture on the last page.
//also make the horizontal pager go through a list of composable functions intead of calling dynamically.