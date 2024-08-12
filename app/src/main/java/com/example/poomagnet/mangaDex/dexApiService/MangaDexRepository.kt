package com.example.poomagnet.mangaDex.dexApiService

import android.util.Log
import com.example.poomagnet.ui.HomeScreen.mangaInfo

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
    val availableLanguages: List<String>
)

class MangaDexRepository() {
    private val apiService = RetrofitInstance.api
    //add local database jargon blah blah later. learn SQL.
    suspend fun searchAllManga(title: String): List<MangaInfo> {//search including stuff like coverpage url.
        val s = apiService.mangaSearchSimple(title)
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

                            list.add(MangaInfo(id,type,mangaTitle,altlist,description,state,contentRating,languageList))
                        }
                    }
                }
            }
        } else {
            return listOf()
        }
        return list;
    }

    suspend fun detailedSearchManga(title: String, tags: List<String>) {}

    suspend fun searchAllChapter(mangaId: Int) {}

    suspend fun downloadChapter(chapterId: Int) {}


}