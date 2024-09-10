package com.example.poomagnet.mangaDex.dexApiService

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

enum class mangaState {
    IN_PROGRESS,
    FINISHED,
}

data class MangaInfo(
    val id: String,
    val type: String,
    val title: String,
    val alternateTitles: List<String>,
    val description: String,
    val state: mangaState,
    val contentRating: String,
    val availableLanguages: List<String>,
    val coverArt: ImageBitmap?
)

class MangaDexRepository() {
    private val apiService = RetrofitInstance.api
    //add local database jargon blah blah later. learn SQL.
    suspend fun searchAllManga(title: String): List<MangaInfo> {//search including stuff like coverpage url.
        Log.d("TAG", "searchALlManga: startin first get request")
        val s = apiService.mangaSearchSimple(title, listOf("cover_art"))
        Log.d("TAG", "searchALlManga: called first get request")
        val list: MutableList<MangaInfo> = mutableListOf()
        if (s["result"] == "ok") {
            var altlist: MutableList<String> = mutableListOf()
            val searchArray = s["data"]
            if (searchArray is List<Any?>){
                searchArray.forEach { elm ->
                    if (elm is Map<*,*>) {
                        val id = elm["id"].toString()
                        val type = elm["type"].toString()
                        val attributes = elm["attributes"]
                        if (attributes is Map<*,*>) {

                            val mangaTitle = attributes["title"].toString()

                            val altTitles = attributes["altTitles"]
                            if (altTitles is List<*>) {
                                altTitles.forEach { titl ->
                                    if (titl is Map<*,*>){
                                        titl.forEach { (key,value) -> altlist.add(value.toString()) }
                                    }
                                }
                            }

                            val descriptions = attributes["description"]
                            var description = ""
                            if (descriptions is Map<*,*>){
                                description = descriptions["en"].toString()
                            }

                            val state = mangaState.IN_PROGRESS;
                            val contentRating = attributes["contentRating"].toString()
                            val languageList: MutableList<String> = mutableListOf()
                            val jl = attributes["availableTranslatedLanguages"]
                            if (jl is List<*>){
                                jl.forEach { lan -> languageList.add(lan.toString()) }
                            }

                            val relationships = elm["relationships"]
                            var coverUrl = ""
                            if (relationships is List<*>) {
                                relationships.forEach { relation ->
                                    if (relation is Map<*,*>)  {
                                        if (relation["type"] == "cover_art"){
                                            val rel_attr = relation["attributes"]
                                            if (rel_attr is Map<*,*>) {
                                                coverUrl = rel_attr["fileName"].toString()
                                            }
                                        }
                                    }
                                }
                            }

                            val image = downloadImage(coverUrl, id)

                            list.add(MangaInfo(id,type,mangaTitle,altlist,description,state,contentRating,languageList, image?.asImageBitmap()))
                        }
                    }
                }
            }
        } else {
            return listOf()
        }
        return list;
    }

    private suspend fun downloadImage(url: String, id: String): Bitmap? {
        return try {
            val response = apiService.downloadFile("https://uploads.mangadex.org/covers/$id/$url")
            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    val inputStream = responseBody.byteStream()
                    BitmapFactory.decodeStream(inputStream)
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun detailedSearchManga(title: String, tags: List<String>) {}

    suspend fun searchAllChapter(mangaId: Int) {}

    suspend fun downloadChapter(chapterId: Int) {}


}