package com.example.poomagnet.mangaRepositoryManager

import android.content.Context
import android.util.Log
import com.example.poomagnet.mangaDex.dexApiService.MangaDexRepository
import com.example.poomagnet.manganatoService.MangaNatoRepository
import java.io.File
import javax.inject.Inject

//central manager of all managa source repositories, will improve structure later

class MangaRepositoryManager @Inject constructor( private val mangadexRepo: MangaDexRepository, private val context: Context, private val natoRepo: MangaNatoRepository) {

    fun getMangaDexRepo(): MangaDexRepository {
        return mangadexRepo
    }

    fun getMangaNatoRepo(): MangaNatoRepository {
        return natoRepo
    }

    suspend fun updateAll() {
        mangadexRepo.updateWholeLibrary()
    }

    fun addToList(chapter: Chapter, mangaId: String, url: String, name: String){
        if (mangaId.startsWith("manga")){

        } else {
            mangadexRepo.addToList(chapter, mangaId, url, name)
        }
    }

    fun getBelongedRepo(mangaId: String): String{
        if (mangaId.startsWith("manga")){
            return "MangaNato"
        } else {
            return "MangaDex"
        }
    }

    suspend fun searchAllManga(title: String, offSet: Int = 0, ordering: Map<String,String> = mapOf(), demo: List<String>, tagsIncluded: List<Tag>, tagsExcluded: List<Tag>, rating: List<String>, source: Sources, status: mangaState = mangaState.IN_PROGRESS) : Pair<List<MangaInfo>,Int>{
        //title: String, page: Int = 1, ordering: String, demo: List<String> tagsIncluded: List<Tag>, tagsExcluded: List<Tag>, contentRating: List<String>, status: String
        return when(source){
            Sources.MANGADEX -> mangadexRepo.searchAllManga(title, offSet, ordering, demo, tagsIncluded, tagsExcluded, rating)
            Sources.MANGANATO -> natoRepo.searchAllManga(title, offSet, ordering.values.first(), demo, tagsIncluded, tagsExcluded, rating, "")
            Sources.ALL->Pair(listOf(),0)
        }
    }



    fun getBackUpFromFile(): String {
        try {
            val file = File(context.filesDir, "backup.txt")
            return file.readText()
        } catch (e : Exception){
            Log.e("TAG", "error getting backup Instance $e")
            return ""
        }
    }
}