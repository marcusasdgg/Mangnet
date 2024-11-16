package com.example.poomagnet.ui.MangaSpecific

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.poomagnet.mangaRepositoryManager.Chapter
import com.example.poomagnet.mangaRepositoryManager.ChapterContents
import com.example.poomagnet.mangaRepositoryManager.MangaInfo
import com.example.poomagnet.mangaRepositoryManager.MangaRepositoryManager
import com.example.poomagnet.mangaRepositoryManager.isDownloaded
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

//make sure to not overwrite the old chapter list objects and add the new ones in.
@HiltViewModel
class MangaSpecificViewModel @Inject constructor( private val repo: MangaRepositoryManager,
                                                  @ApplicationContext private val context: Context) : ViewModel() {
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
                    chapterList = ot.currentManga.chapterList?.sortedWith(
                    compareByDescending<Chapter> { it.chapter }.thenByDescending { it.volume }
                )?: listOf()
                )
            )
        }
    }

    fun filterSame(){
        _uiState.update {
            ot ->
            ot.copy(
                currentManga = ot.currentManga?.copy(
                    chapterList = ot.currentManga.chapterList?.filter { it.pageCount > 0 }?: listOf()
                )
            )
        }
        _uiState.update { ot ->
            ot.copy(
                currentManga = ot.currentManga?.copy(
                    chapterList = ot.currentManga.chapterList?.distinctBy { it.chapter }?: listOf()
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
        var currentManga = uiState.value.currentManga
        var chapterFound = currentManga?.chapterList?.firstOrNull { elm -> elm.id == chapterId }
        var chapterlist = currentManga?.chapterList
        if (chapterFound !== null && currentManga !== null){
            //chapter exists
            Log.d("TAG", "getChapterUrls: found chapter in manga and found manga")
            if (chapterFound.contents?.isDownloaded == true){
                _uiState.update {
                    it.copy(
                        currentChapter = chapterFound
                    )
                }
            } else {
                val chap = repo.getChapterContents(chapterFound, currentManga.id)
                chapterlist = chapterlist?.map {elm ->if (elm.id == chapterId){
                    chap
                } else {
                    elm
                }}

                currentManga = currentManga.copy(chapterList = chapterlist)
                _uiState.update {
                    it.copy(
                        currentChapter = chap,
                        currentManga = currentManga
                    )
                }
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
                   repo.addToLibrary(newManga)
                } else {
                    repo.removeFromLibrary(currentManga)
                }
            }
                // Update the UI state inside the coroutine to ensure consistency
            _uiState.update { it.copy(currentManga = newManga) }
            // Log the new state
        } else {
            Log.d("TAG", "addToLibrary: manga was null???")
        }
    }

    suspend fun getChapterInfo(){
        val id = uiState.value.currentManga?.id
        Log.d("TAG", "id passed was this: \"$id\"")
        if (id !== null){
            val result = uiState.value.currentManga?.let { repo.getChapters(it) }
            _uiState.update {
                it.copy(
                    currentManga = result
                )
            }
            //filterSame()
            orderManga()
        }
    }

    fun getRefererUrl(): String{
        val s = repo.getBaseUrls(uiState.value.currentManga!!.id , uiState.value.currentChapter!!.id )
        Log.d("TAG", "getRefererUrl: $s")
        return s
    }

    suspend fun getNextChapter(context: Context){

        val currentChapter = uiState.value.currentChapter?.chapter
        val currentVolume = uiState.value.currentChapter?.volume
        if (currentVolume !== null && currentChapter !== null){

            var firstNextChapter = uiState.value.currentManga?.chapterList?.reversed()?.firstOrNull { elm  -> elm.chapter > currentChapter }
            Log.d("TAG", "getNextChapter: next chapter is $firstNextChapter")

            if (firstNextChapter?.contents?.isDownloaded == true){
                Log.d("TAG", "is downloaded: ")
                _uiState.update {
                    it.copy(
                        nextChapter = firstNextChapter
                    )
                }
                return
            }

            if (firstNextChapter !== null){
                val chapter = repo.getChapterContents(firstNextChapter, uiState.value.currentManga?.id ?: "")
                if (chapter.contents?.imagePaths?.isEmpty() == true){
                    Log.d("TAG", "getNextChapter: no images found in getchapter contents")
                    _uiState.update {
                        it.copy(
                            nextChapter = null
                        )
                    }
                }
                Log.d("TAG", "getNextChapter: urls are $chapter")
                firstNextChapter = chapter
                chapter.contents?.imagePaths?.forEach { e -> preloadImage(context,e) }
                _uiState.update {
                    it.copy(
                        nextChapter = firstNextChapter
                    )
                }
            } else {
                Log.d("TAG", "getNextChapter: no next chapter found")
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
            val t  = uiState.value.currentManga?.chapterList?.map { elm ->
                if (elm.id == currentChapter.id){
                    currentChapter
                } else {
                    elm
                }
            }
            if (t !== null){
                Log.d("TAG", "newChapList: ${t.firstOrNull{elm -> elm.finished}}")
                _uiState.update {
                    it.copy(currentManga = it.currentManga?.copy(chapterList = t))
                }
                Log.d("TAG", "In actual manga entry markThisAsDone: ${_uiState.value.currentManga?.chapterList?.firstOrNull { elm -> elm.finished }}")
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
            repo.updateInLibrary(currentManga)
        }
    }

    suspend fun updateLastChapterRead(chapterId: String, pageCount: Int){
        _uiState.update {
            it.copy(
                currentManga = it.currentManga?.copy(
                    lastReadChapter = Pair(chapterId,pageCount)
                )
            )
        }
        updateLibraryEquivalent()
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
            val t  = uiState.value.currentManga?.chapterList?.map { elm ->
                if (elm.id == s.id){
                    s
                } else {
                    elm
                }
            }
            if (t !== null){
                _uiState.update {
                    it.copy(currentManga = it.currentManga?.copy(chapterList = t))
                }
            }
        }
    }

    suspend fun getPreviousChapter(context: Context){
        val currentChapter = uiState.value.currentChapter?.chapter
        val currentVolume = uiState.value.currentChapter?.volume
        if (currentVolume !== null && currentChapter !== null){
            var firstNextChapter = uiState.value.currentManga?.chapterList?.firstOrNull { elm  -> elm.chapter < currentChapter }
            if (firstNextChapter !== null){
                val chapter = repo.getChapterContents(firstNextChapter, uiState.value.currentManga?.id ?: "")
                Log.d("TAG", "getNextChapter: $chapter")
                if (chapter.contents is ChapterContents.Online){
                    firstNextChapter = chapter
                    chapter.contents.imagePaths.forEach { e -> preloadImage(context, e) }
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

    suspend fun loadImageFromLibrary(mangaId: String, coverUrl: String): String{
        return repo.getImageUri(mangaId, "$coverUrl.jpeg")
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


    suspend fun downloadChapter(chapterId: String, mangaId: String){
        Log.d("TAG", "downloadChapter: $chapterId, $mangaId")
        val inputData = Data.Builder()
            .putString("mangaId", mangaId)
            .putString("chapterId", chapterId)
            .build()
        val workRequest = OneTimeWorkRequestBuilder<MangaWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "mangaDownloadQueue",
                ExistingWorkPolicy.APPEND,
                workRequest
            )

        Log.d("TAG", "downloadChapters: ${WorkManager.getInstance(context).getWorkInfosByTag("MangaWorker").toString()}")
    }

    fun loadPreviousChapter(){
        _uiState.update {
            it.copy(
                currentChapter = it.previousChapter,
                nextChapter = it.currentChapter
            )
        }
    }

    suspend fun loadContentimage(mangaId: String, chapterId: String, url: String): String{
        return repo.retrieveImageContent(mangaId, chapterId, url)
    }



}

//worker which taakes paramters mangaId, chapterId and sourceId, will implement in future
@HiltWorker
class MangaWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @Assisted val repo: MangaRepositoryManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Retrieve parameters
        val mangaId = inputData.getString("mangaId") ?: return  Result.failure()
        val id = inputData.getString("chapterId") ?: return  Result.failure()
        Log.d("TAG", "doWork: ")
        return try {
            repo.downloadChapter(mangaId, id)
            Result.success()
        } catch (e: Exception) {
            Log.d("TAG", "doWork: $e")
            Result.failure()
        }
    }
}


//when moving to read chapter view, we need to edit the app.kt's backhandler such that
//it now navigates us back to the mangaScreen page? Since backhandler is triggerred by
//the leafiest element we can define a new backhandler in our newscreen that will block the old
// one. The entire reading experience must be stored in mangaSpecificViewModel though.

// redo teh horizontal pager so you instead hook on trying to gesture on the last page.
//also make the horizontal pager go through a list of composable functions intead of calling dynamically.