package com.example.poomagnet.mangaRepositoryManager

import android.content.Context
import android.util.Log
import com.example.poomagnet.mangaDex.dexApiService.MangaDexRepository
import com.example.poomagnet.manganatoService.MangaNatoRepository
import com.google.gson.JsonParser
import javax.inject.Inject

//central manager of all managa source repositories, will improve structure later
// future improvements:
// make in-memory manga lists stable - switch off set.
// add a feature that on startup reads all downloaded files and sets downloaded or not downloaded.
// do file retrievals with a special itty bitty service like download service but called file service or something.

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
                try {
                    Log.d("TAG", "getChapters: manganato")
                    natoRepo.getChapters(manga)
                } catch(e: Exception){
                    manga
                }
            }
            else -> {
                try {
                Log.d("TAG", "getChapters: mangadex")
                mangadexRepo.getChapters(manga)
                 } catch(e: Exception){
                    manga
                }
            }
        }
    }

    fun getLibrary(): List<MangaInfo> {
        return mangadexRepo.library.toList() + natoRepo.library.toList()
    }

    suspend fun getImageUri(mangaId: String, coverUrl: String) : String {
        val source = getBelongedRepo(mangaId)
        return when (source){
            Sources.MANGANATO -> {
                val s = natoRepo.getImageUri(mangaId, coverUrl)
                Log.d("TAG", "getImageUri: $s")
                s
            }

            Sources.MANGADEX -> {
                val s = mangadexRepo.getImageUri(mangaId, coverUrl)
                Log.d("TAG", "getImageUri: $s")
                s
            }
            Sources.ALL -> {
                Log.d("TAG", "getImageUri: no source detected")
                ""}
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
            val file = mangadexRepo.backUpMangaString()
            val file2 = natoRepo.backUpMangaString()
            return "{\"mangadex\" : ${file}, \n \"manganato\": ${file2}\n}"
            //
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

    fun loadFromBackUp(backup: String){
        val backups = JsonParser().parse(backup).asJsonObject
        natoRepo.restoreBackup(backups.get("manganato").toString())
        mangadexRepo.restoreBackup(backups.get("mangadex").toString())
    }

    suspend fun updateInLibrary(manga: MangaInfo){
        val source = getBelongedRepo(manga.id)
        when (source){
            Sources.MANGANATO -> natoRepo.updateInLibrary(manga)
            Sources.MANGADEX -> mangadexRepo.updateInLibrary(manga)
            else -> throw(IllegalArgumentException())
        }

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
            else -> {Log.d("TAG", "downloadChapter: nothing? $mangaId, $chapterId")}
        }
    }

    suspend fun retrieveImageContent(mangaId: String, chapterId: String, url: String): String {
        val source = getBelongedRepo(mangaId)
        return when(source){
            Sources.MANGANATO -> natoRepo.retrieveImageContent(mangaId,chapterId,url)
            Sources.MANGADEX -> mangadexRepo.retrieveImageContent(mangaId,chapterId,url)
            else -> ""
        }
    }

    fun getContext(): Context {
        return mangadexRepo.context
    }
}

//each repo has its own backup file, the getbackUpFromFile will take the n files and merge them into 1 text file.
