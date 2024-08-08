package com.example.poomagnet.mangaDex.dexApiService

import android.util.Log
import com.example.poomagnet.ui.HomeScreen.mangaInfo

enum class mangaState {
    IN_PROGRESS,
    FINISHED,
}

class MangaInfo(
    id: String,
    type: String,
    title: String,
    alternateTitles: List<String>,
    description: String,
    state: mangaState,
    contentRating: String,
    availableLanguages: List<String>
)

class MangaDexRepository() {
    private val apiService = RetrofitInstance.api
    //add local database jargon blah blah later. learn SQL.
    suspend fun searchAllManga(title: String): List<MangaInfo> {
        val s = apiService.mangaSearchSimple(title)

        if (s["result"] == "ok") {
            var list: MutableList<MangaInfo> = mutableListOf()
            val attr = s["data"]
            val p: Map<*,*> = s["data"]["attributes"]
            if (attr is Map<*,*>){
                attr.forEach{ elm ->
                    list.add(MangaInfo(attr["id"].toString(), attr["type"].toString(),attr["attributes"] ))
                }
            }else {
                return listOf()
            }

        } else {
            return listOf()
        }

    }

    suspend fun detailedSearchManga(title: String, tags: List<String>) {}

    suspend fun searchAllChapter(mangaId: Int) {}

    suspend fun downloadChapter(chapterId: Int) {}


}