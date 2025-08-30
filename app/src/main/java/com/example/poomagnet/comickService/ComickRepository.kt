package com.example.poomagnet.comickService

import android.content.Context
import com.example.poomagnet.downloadService.DownloadService
import com.example.poomagnet.mangaRepositoryManager.MangaInfo
import com.example.poomagnet.mangaRepositoryManager.SimpleDate
import com.example.poomagnet.mangaRepositoryManager.SlimChapter
import com.example.poomagnet.mangaRepositoryManager.Tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackUpInstance (
    val library: MutableList<MangaInfo>,
    var newUpdatedChapters: MutableList<Pair<SimpleDate, SlimChapter>>,
    val tagMap: MutableMap<Tag, String>
)

// essence of a repository is that
// it provides a few features:
// abstracts a lot of the manga searching and offline reading from teh viewmodel
// some of teh most important functions we need are to be able to search for manga based on tags
// download chapters, add chapters to library.
class ComickRepository @Inject constructor(val context: Context, private val downloadService: DownloadService){
    private val apiService = retrofitInstance.api
    // comicks api works interestingly
    // tags work using a string instead of int like manganato

    private var tagMap: MutableMap<Tag, Int> = mutableMapOf()
    val library: MutableList<MangaInfo> = mutableListOf()
    private var idSet: MutableSet<String> = mutableSetOf()

    init {
        // call restoreBackup
        setupTags()

    }

    private fun setupTags(){
        CoroutineScope(Dispatchers.IO).launch{
            try {
                val response = apiService.getTagList()


            } catch (e: Exception){

            }
        }
    }

    // functions to implement TODO:
    // loadmangaFromBackup
    // updateWholeLibrary
    // restorBackup
    // backupManga
    // searchAllManga
    // getChapters
    // updateInLibarry
    // downloadChapter
    // getNewUpdatedChapters




}