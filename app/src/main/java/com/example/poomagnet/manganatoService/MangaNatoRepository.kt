package com.example.poomagnet.manganatoService

import android.content.Context
import android.util.Log
import com.example.poomagnet.downloadService.DownloadService
import com.example.poomagnet.mangaRepositoryManager.MangaInfo
import com.example.poomagnet.mangaRepositoryManager.Tag
import com.example.poomagnet.mangaRepositoryManager.mangaState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import javax.inject.Inject

class MangaNatoRepository @Inject constructor(private val context: Context, private val downloadService: DownloadService) {
    private val natoApi = RetrofitInstance.api
    private var tagMap: MutableMap<Tag,Int> = mutableMapOf()
    private var suggestiveRating: Int = 0;
    private var eroticaRating: Int = 0;
    private var pornographicRating: Int = 0;
    private var shounen = 0;
    private var shoujo = 0;
    private var seinen = 0;
    private var josei = 0;


    init {
        initTags()
    }

    fun loadBackup(){

    }

    fun backUp(){

    }

    //speed this function up by, if demographic is "" in mangaspecific view, we call updateInfo on this given the url in title
    suspend fun searchAllManga(title: String, page: Int = 1, ordering: String, demo: List<String>, tagsIncluded: List<Tag>, tagsExcluded: List<Tag>, contentRating: List<String>, status: String) : Pair<List<MangaInfo>,Int>{
        var ret = mutableListOf<MangaInfo>()


        var tE: String? = null
        var tI: String? = null

        if (tagsIncluded.isNotEmpty()){
            tI = "_"
            for (tag in tagsIncluded){
                val temp = tagMap[tag] ?: ""
                if (temp != ""){
                    tI += temp.toString() + "_"
                }
            }
            for (t in contentRating){
                when(t){
                    "suggestive" -> {
                        tI += suggestiveRating.toString()
                    }
                    "erotica" -> {
                        tI += eroticaRating.toString()
                    }
                    "pornographic" -> {
                        tI += pornographicRating.toString() + "_"
                    }

                }
            }

            for (t in demo){
                when(t){
                    "shounen" -> {
                        tI += shounen.toString()+ "_"
                    }
                    "shoujo" -> {
                        tI += shoujo.toString() + "_"
                    }
                    "seinen" -> {
                        tI += seinen.toString() + "_"
                    }
                    "josei" -> {
                        tI += josei.toString() + "_"
                    }

                }
            }
        }

        if (tagsExcluded.isNotEmpty()){
            tE = "_"
            for (tag in tagsExcluded){
                val temp = tagMap[tag] ?: ""
                if (temp != ""){
                    tE += temp.toString() + "_"
                }
            }
        }

        val titleE : String? = if (title == "") null else title
        val statusE: String? = if(status == "") null else status

        Log.d("TAG", "searchAllManga: $tE, $tI, $title")

        val res = natoApi.mangaSearchSimple(titleE,page,ordering,tI,tE,statusE)
        //Log.d("TAG", "searchAllManga $res")
        val soupy = Jsoup.parse(res).body().getElementsByTag("div").first{
            it.hasClass("body-site")
        }.getElementsByTag("div").first {
            it.hasClass("container") && it.hasClass("container-main")
        }. getElementsByTag("div").first {
            it.hasClass("panel-content-genres")
        }.getElementsByTag("div").filter {
                e -> e.hasClass("content-genres-item")
        }.toList()

        Log.d("TAG", "searchAllManga: found mangas : ${soupy.size}")
        for (elm in soupy){
            val img = elm.getElementsByTag("a").first().getElementsByTag("img")
            val imgUrl = img.attr("src")
            val itemInfo = elm.getElementsByClass("genres-item-info").first()
            val tite = itemInfo.getElementsByTag("h3").first().getElementsByTag("a").first().attr("title").toString()
            val innerUrl = itemInfo.getElementsByTag("h3").first().getElementsByTag("a").first().attr("href").toString()
            val id = innerUrl.split("/").last()
            val type = "manga"

            val res = natoApi.getInfo(innerUrl)
            val innerSoup = Jsoup.parse(res).body().getElementsByTag("div").first {
                it.hasClass("body-site")
            }.getElementsByTag("div").first{
                it.hasClass("container") && it.hasClass("container-main")
            }.getElementsByTag("div").first {
                it.hasClass("container-main-left")
            }.getElementsByTag("div").first{
                it.hasClass("panel-story-info")
            }


            val right = innerSoup.getElementsByClass("story-info-right").first().getElementsByTag("table").first().getElementsByTag("tbody").first().getElementsByTag("tr").toList()
            var alternateTitles: MutableList<String> = mutableListOf()
            var status = ""
            var tagList: MutableList<String> = mutableListOf()

            var demographic = ""

            for (e in right){
                val label = e.getElementsByClass("table-label").first().getElementsByTag("i").first().className().removeSuffix(" :")

                when (label) {
                    "Alternative" -> {
                        alternateTitles = e.getElementsByClass("table-value").first().getElementsByTag("h2").text().split(";").toMutableList()
                    }
                    "Status" -> {
                        status = e.getElementsByClass("table-value").text()
                    }
                    "Genres" -> {
                        val lit = e.getElementsByClass("table-value").first().getElementsByTag("a").toList()
                        for (i in lit){
                            when (i.text()){
                                "Seinen" -> {demographic = i.text()}
                                "Shounen" -> {demographic = i.text()}
                                "Shoujo" -> {demographic = i.text()}
                                "Josei" -> {demographic = i.text()}
                                else -> tagList.add(i.text())
                            }
                            tagList.add(i.text())
                        }
                    }
                }

            }

            val description = innerSoup.getElementsByClass("panel-story-info-description").first().text()

            val state = when(status){
                "Ongoing" -> {
                    mangaState.IN_PROGRESS
                }
                else -> mangaState.FINISHED
            }

            ret.add(MangaInfo(id, type, tite, alternateTitles, description, state, "noClue", listOf("en"), null, imgUrl, 0, inLibrary =  false, tagList = tagList, demographic = demographic))
        }
        return Pair(ret,ret.size)
    }

    //allow this function to read off backup file if exists
    fun initTags(){
        CoroutineScope(Dispatchers.IO).launch{
            try {
                val result = natoApi.tagInit()
                val htparse = Jsoup.parse(result).getElementsByTag("body").first().getElementsByTag("div")
                    .first { it.hasClass("container") && it.hasClass("container-main") }.getElementsByTag("div").first(){
                        it.hasClass("panel-advanced-search-tool")
                    }.getElementsByTag("div").first{
                        it.hasClass("advanced-search-tool-content")
                    }.getElementsByTag("div").first{
                        it.hasClass("advanced-search-tool-genres-list")
                    }.getElementsByTag("span").toList()
                Log.d("TAG", "initTags: found ${htparse.size} spans")
                for (i in htparse){
                    val num = i.attr("data-i").toInt()
                    val title = i.attr("title").removeSuffix(" Manga")

                    when(title){
                        "Pornographic" -> {
                            pornographicRating = num
                            continue
                        }
                        "Ecchi" -> {
                           suggestiveRating = num
                            continue
                        }
                        "Erotica" -> {
                            eroticaRating = num
                            continue
                        }
                        "Shounen" -> {
                            shounen = num
                        }
                        "Shoujo" -> {
                            shoujo = num
                        }
                        "Seinen" -> {
                            seinen = num
                        }
                        "Josei" -> {
                            josei = num
                        }

                    }

                    Tag.fromValue(title)?.let { tagMap.put(it, num ) }
                }
                Log.d("TAG", "initTags: $tagMap with ${tagMap.size} entries")
                val s = searchAllManga(title = "s", page = 1, ordering = "topview", tagsIncluded = listOf(Tag.Action), tagsExcluded = listOf(), contentRating = listOf("suggestive"), status = "", demo = listOf("seinen"))
                Log.d("TAG", "initTags: testing searchAll : given ${s.first.size} results ")
                Log.d("TAG", "initTags: testing searchAll :  ${s.first.map { e -> e.title }.toList()}")


            } catch (e : Exception){
                Log.d("TAG", "initTags: $e")
            }
        }


    }


}