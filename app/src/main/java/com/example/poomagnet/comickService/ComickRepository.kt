package com.example.poomagnet.comickService

import android.content.Context
import com.example.poomagnet.downloadService.DownloadService
import javax.inject.Inject
import com.example.poomagnet.comickService.retroFitInstance
import com.example.poomagnet.mangaRepositoryManager.MangaInfo
import com.example.poomagnet.mangaRepositoryManager.Tag

data class BackUpInstance {



}

// essence of a repository is that
// it provides a few features:
// abstracts a lot of the manga searching and offline reading from teh viewmodel
// some of teh most important functions we need are to be able to search for manga based on tags
// download chapters, add chapters to library.
class ComickRepository @Inject constructor(val context: Context, private val downloadService: DownloadService){
    private val apiService = retroFitInstance.api
    // comicks api works interestingly
    // tags work using a string instead of int like manganato

    private var tagMap: MutableMap<Tag, Int> = mutableMapOf()
    val library: MutableList<MangaInfo> = mutableListOf()



}