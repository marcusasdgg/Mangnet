package com.example.poomagnet.mangaRepositoryManager

import android.content.Context
import android.util.Log
import com.example.poomagnet.mangaDex.dexApiService.MangaDexRepository
import com.example.poomagnet.manganatoService.MangaNatoRepository
import java.io.File
import javax.inject.Inject

//central manager of all managa source repositories, will improve structure later

class MangaRepositoryManager @Inject constructor( private val mangadexRepo: MangaDexRepository, private val context: Context, private val natoRepo: MangaNatoRepository) {
    val newUpdatedChapters:  List<Pair<SimpleDate,SlimChapter>>
        get() = mangadexRepo.getNewUpdatedChapters() + natoRepo.getNewUpdatedChapters()
    fun getMangaDexRepo(): MangaDexRepository {
        return mangadexRepo
    }

    fun getMangaNatoRepo(): MangaNatoRepository {
        return natoRepo
    }

    suspend fun updateAll() {
        mangadexRepo.updateWholeLibrary()
    }


    fun getBelongedRepo(mangaId: String): Sources{
        if (mangaId.startsWith("manga")){
            return Sources.MANGANATO
        } else {
            return Sources.MANGADEX
        }
    }

    suspend fun searchAllManga(title: String, offSet: Int = 0, ordering: Map<String,String> = mapOf(), demo: List<String>, tagsIncluded: List<Tag>, tagsExcluded: List<Tag>, rating: List<String>, source: Sources, status: mangaState = mangaState.IN_PROGRESS) : Pair<List<MangaInfo>,Int>{
        //title: String, page: Int = 1, ordering: String, demo: List<String> tagsIncluded: List<Tag>, tagsExcluded: List<Tag>, contentRating: List<String>, status: String
        return when(source){
            Sources.MANGADEX -> mangadexRepo.searchAllManga(title, offSet, ordering, demo, tagsIncluded, tagsExcluded, rating)
            Sources.MANGANATO -> natoRepo.searchAllManga(title, offSet, ordering.keys.first(), demo, tagsIncluded, tagsExcluded, rating, "")
            Sources.ALL->Pair(listOf(),999)
        }
    }

    suspend fun getChapters(manga: MangaInfo) : MangaInfo{
        return when(getBelongedRepo(manga.id)){
            Sources.MANGANATO -> {
                Log.d("TAG", "getChapters: manganato")
                natoRepo.getChapters(manga)
            }
            else -> {
                Log.d("TAG", "getChapters: mangadex")
                mangadexRepo.getChapters(manga)
            }
        }
    }

    fun getLibrary(): List<MangaInfo> {
        return mangadexRepo.library.toList() + natoRepo.library.toList()
    }

    suspend fun getImageUri(mangaId: String, coverUrl: String) : String {
        val source = getBelongedRepo(mangaId)
        return when (source){
            Sources.MANGANATO -> {natoRepo.getImageUri(mangaId, coverUrl)}
            Sources.MANGADEX -> {mangadexRepo.getImageUri(mangaId, coverUrl)}
            Sources.ALL -> {""}
        }
    }

    fun getBaseUrls( mangaId: String, chapterId: String): String{
        val source = getBelongedRepo(mangaId)
        return when(source){
            Sources.MANGANATO -> "https://chapmanganato.to/$mangaId/$chapterId"
            Sources.MANGADEX ->  "https://mangadex.org/chapter/$chapterId"
            else -> ""
        }
    }

    fun getMangaById(id: String): MangaInfo?{
        val source = getBelongedRepo(id)
        return when(source){
            Sources.MANGANATO -> natoRepo.library.firstOrNull { e -> e.id == id }
            Sources.MANGADEX -> mangadexRepo.library.firstOrNull { e -> e.id == id }
            else -> null
        }
    }

    suspend fun addToLibrary(manga: MangaInfo){
        val source = getBelongedRepo(manga.id)
        when (source){
            Sources.MANGANATO -> {natoRepo.addToLibrary(manga)}
            Sources.MANGADEX -> {mangadexRepo.addToLibrary(manga)}
            Sources.ALL -> return
        }
    }

    suspend fun removeFromLibrary(manga: MangaInfo?){
        val source = manga?.id?.let { getBelongedRepo(it) } ?: return
        when (source){
            Sources.MANGANATO -> {natoRepo.removeFromLibrary(manga)}
            Sources.MANGADEX -> {mangadexRepo.removeFromLibrary(manga)}
            Sources.ALL -> return
        }
    }

    fun getBackUpFromFile(): String {
        try {
            val file = File(context.filesDir, "backup_mangadex.txt")
            val file2 = File(context.filesDir, "backup_manganato.txt")
            return "\"mangadex\" : {${file.readText()} \n \"manganato\": {${file2.readText()}}"
        } catch (e : Exception){
            Log.e("TAG", "error getting backup Instance $e")
            return ""
        }
    }

    suspend fun getChapterContents(ch: Chapter, mangaId: String): Chapter{
        val source = getBelongedRepo(mangaId)
        return when (source){
            Sources.MANGANATO -> natoRepo.getChapterContents(ch, mangaId)
            Sources.MANGADEX -> mangadexRepo.getChapterContents(ch)
            else -> throw(IllegalArgumentException())
        }
    }

    suspend fun updateLibrary(){
        mangadexRepo.updateWholeLibrary()
        natoRepo.updateWholeLibrary()
    }

    suspend fun downloadChapter(mangaId: String, chapterId: String){

        val source = getBelongedRepo(mangaId)
        when(source){
            Sources.MANGANATO -> {
                Log.d("TAG", "downloadChapter: nato")
                natoRepo.downloadChapter(mangaId,chapterId)
            }
            Sources.MANGADEX ->{
                Log.d("TAG", "downloadChapter: dex")
                mangadexRepo.downloadChapter(mangaId,chapterId)
            }
            else -> {}
        }
    }
}

//each repo has its own backup file, the getbackUpFromFile will take the n files and merge them into 1 text file.