package com.example.poomagnet.manganatoService

import android.content.Context
import android.util.Log
import com.example.poomagnet.downloadService.DownloadService
import com.example.poomagnet.mangaRepositoryManager.Chapter
import com.example.poomagnet.mangaRepositoryManager.ChapterContents
import com.example.poomagnet.mangaRepositoryManager.ChapterContentsDeserializer
import com.example.poomagnet.mangaRepositoryManager.ChapterContentsSerializer
import com.example.poomagnet.mangaRepositoryManager.MangaInfo
import com.example.poomagnet.mangaRepositoryManager.SimpleDate
import com.example.poomagnet.mangaRepositoryManager.SimpleDateAdapter
import com.example.poomagnet.mangaRepositoryManager.SlimChapterAdapter
import com.example.poomagnet.mangaRepositoryManager.Tag
import com.example.poomagnet.mangaRepositoryManager.TagDeserializer
import com.example.poomagnet.mangaRepositoryManager.isOnline
import com.example.poomagnet.mangaRepositoryManager.mangaState
import com.example.poomagnet.mangaRepositoryManager.slimChapter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.Scanner
import javax.inject.Inject

data class BackUpInstance(
    val library: MutableSet<MangaInfo>,
    val idSet: MutableSet<String>,
    var newUpdatedChapters: MutableList<Pair<SimpleDate, slimChapter>>,
    val tagMap: MutableMap<Tag,Int>,
    var suggestiveRating: Int,
    val eroticaRating: Int,
    val pornographicRating: Int,
    val shounen: Int,
    val shoujo: Int,
    val seinen: Int,
    val josei: Int,
)


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
    var library: MutableSet<MangaInfo> = mutableSetOf()
    var idSet: MutableSet<String> = mutableSetOf()
    var newUpdatedChapters: MutableList<Pair<SimpleDate, slimChapter>> = mutableListOf()

    private val gsonSerializer = GsonBuilder()
        .registerTypeAdapter(ChapterContents::class.java, ChapterContentsSerializer())
        .registerTypeAdapter(ChapterContents::class.java, ChapterContentsDeserializer())
        .registerTypeAdapter(SimpleDate::class.java, SimpleDateAdapter())
        .registerTypeAdapter(SlimChapterAdapter::class.java, SlimChapterAdapter())
        .registerTypeAdapter(Tag::class.java, TagDeserializer())
        .create()

    init {
        loadMangaFromBackup(context)
        initTags()
    }

    private fun loadMangaFromBackup(context: Context) {
        try {
            // Read the file content from backup.txt
            val file = File(context.filesDir, "backup_manganato.txt")
            if (file.exists()) {
                val jsonString = file.readText()
                Log.d("TAG", "loadMangaFromBackup nato!: backup is $jsonString")
                // Deserialize the JSON string into a list of MangaInfo objects using Gson
                val listType = object : TypeToken<BackUpInstance>() {}.type
                val r: BackUpInstance = gsonSerializer.fromJson(jsonString, listType)
                library = r.library
                idSet = r.idSet
                newUpdatedChapters = r.newUpdatedChapters
                tagMap = r.tagMap
                suggestiveRating = r.suggestiveRating
                eroticaRating = r.eroticaRating
                pornographicRating = r.pornographicRating
                seinen = r.seinen
                shoujo = r.shoujo
                shounen = r.shounen
                josei = r.josei
                //tagMap = r.tagMap
                Log.d("TAG", "loadMangaFromBackup initalize: $newUpdatedChapters")
            } else {
                Log.d("TAG", "backup.txt not found, mangaObj is empty. ")
            }
        } catch (e: Exception) {
            Log.e("TAG", "Error loading manga from backup.txt", e)
        }
    }

    suspend fun backUpManga(){
        Log.d("TAG", "commencing backup: ${library.map { e -> e.chapterList }}")

        val libraryShouldBe = library.map {element ->
            element.copy(chapterList = element.chapterList?.map { chapter ->
                chapter.copy(contents = if (chapter.contents?.isOnline == true) null else chapter.contents )
            } ?: listOf())
        }
        val file = File(context.filesDir, "backup_manganato.txt")
        withContext(Dispatchers.IO) {
            FileOutputStream(file).use { fos ->
                // Create an OutputStreamWriter to write text data
                OutputStreamWriter(fos).use { writer ->
                    // Write the data to the file
                    writer.write(
                        gsonSerializer.toJson(BackUpInstance(library, idSet, newUpdatedChapters, tagMap, suggestiveRating, eroticaRating, pornographicRating, shounen, shoujo, seinen, josei))
                    )
                }
            }
            Log.d("TAG", "backUpManga: success")
        }
    }

    suspend fun searchAllManga(title: String, page: Int = 1, ordering: String, demo: List<String>, tagsIncluded: List<Tag>, tagsExcluded: List<Tag>, contentRating: List<String>, status: String) : Pair<List<MangaInfo>,Int>{
        var ret = mutableListOf<MangaInfo>()

        try {
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
            val order = when (ordering){
                "order[followedCount]" -> "topview"
                "order[title]" -> "az"
                else -> ""
            }

//        Log.d("TAG", "searchAllManga: $tE, $tI, $title")
            val res = natoApi.mangaSearchSimple(titleE,if (page != 0) page else 1,if (order !== "") order else null,tI,tE,statusE)
            Log.d("TAG", "searchAllManga prasing html qs: $title")
            val soupy = Jsoup.parse(res).body().getElementsByTag("div").first{
                it.hasClass("body-site")
            }.getElementsByTag("div").first {
                it.hasClass("container") && it.hasClass("container-main")
            }. getElementsByTag("div").first {
                it.hasClass("panel-content-genres")
            }.getElementsByTag("div").filter {
                    e -> e.hasClass("content-genres-item")
            }.toList()

            for (elm in soupy){
                val img = elm.getElementsByTag("a").first().getElementsByTag("img")
                val imgUrl = img.attr("src")
                val itemInfo = elm.getElementsByClass("genres-item-info").first()
                val tite = itemInfo.getElementsByTag("h3").first().getElementsByTag("a").first().attr("title").toString()
                val innerUrl = itemInfo.getElementsByTag("h3").first().getElementsByTag("a").first().attr("href").toString()
                val id = innerUrl.split("/").last()
                val type = "manga"


                val alternateTitles = listOf("")
                val description = ""
                val state = mangaState.IN_PROGRESS
                val tagList = mutableListOf<String>()
                val demographic = ""

                ret.add(MangaInfo(id, type, tite, alternateTitles, description, state, "noClue", listOf("en"), null, imgUrl, 0, inLibrary =  false, tagList = tagList, demographic = demographic))
            }
            val maxElm = Jsoup.parse(res).body().getElementsByTag("div").firstOrNull{
                it.hasClass("body-site")
            }?.getElementsByTag("div")?.firstOrNull {
                it.hasClass("container") && it.hasClass("container-main")
            }?.getElementsByTag("div")?.firstOrNull{
                it.hasClass("panel-page-number")
            }?.getElementsByClass("group-qty")?.firstOrNull()
                ?.getElementsByTag("a")?.firstOrNull()
                ?.text()?.split(":")?.lastOrNull()?.trim()?.replace(",", "")?.toInt() ?: 0
            return Pair(ret,maxElm)
        } catch (e: Exception){
            Log.d("TAG", "searchAllManga: $e")
            return Pair(ret,0)
        }
    }

    //allow this function to read off backup file if exists
    fun initTags(){
        if (tagMap.isEmpty()){
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


                } catch (e : Exception){
                    Log.d("TAG", "initTags: $e")
                }
            }

        }
    }

    suspend fun getChapters(manga: MangaInfo): MangaInfo{

            val innerUrl = "https://chapmanganato.to/${manga.id}"
            Log.d("TAG", "getChapters: $innerUrl")
            val res = natoApi.getInfo(innerUrl)
            val innerSoup = Jsoup.parse(res).body().getElementsByTag("div").first {
                it.hasClass("body-site")
            }.getElementsByTag("div").first {
                it.hasClass("container") && it.hasClass("container-main")
            }.getElementsByTag("div").first {
                it.hasClass("container-main-left")
            }.getElementsByTag("div").first {
                it.hasClass("panel-story-info")
            }


            val right =
                innerSoup.getElementsByClass("story-info-right").first().getElementsByTag("table")
                    .first().getElementsByTag("tbody").first().getElementsByTag("tr").toList()
            var alternateTitles: MutableList<String> = mutableListOf()
            var status = ""
            var tagList: MutableList<String> = mutableListOf()

            var demographic = ""

            for (e in right) {
                val label =
                    e.getElementsByClass("table-label").first().getElementsByTag("i").first()
                        .className().removeSuffix(" :")

                when (label) {
                    "Alternative" -> {
                        alternateTitles =
                            e.getElementsByClass("table-value").first().getElementsByTag("h2")
                                .text().split(";").toMutableList()
                    }

                    "Status" -> {
                        status = e.getElementsByClass("table-value").text()
                    }

                    "Genres" -> {
                        val lit = e.getElementsByClass("table-value").first().getElementsByTag("a")
                            .toList()
                        for (i in lit) {
                            when (i.text()) {
                                "Seinen" -> {
                                    demographic = i.text()
                                }

                                "Shounen" -> {
                                    demographic = i.text()
                                }

                                "Shoujo" -> {
                                    demographic = i.text()
                                }

                                "Josei" -> {
                                    demographic = i.text()
                                }

                                else -> tagList.add(i.text())
                            }
                            tagList.add(i.text())
                        }
                    }
                }

            }

            val description =
                innerSoup.getElementsByClass("panel-story-info-description").first().text()

            val state = when (status) {
                "Ongoing" -> {
                    mangaState.IN_PROGRESS
                }

                else -> mangaState.FINISHED
            }

            val chapArr: MutableList<Chapter> = mutableListOf()
            val chapterSoup = Jsoup.parse(res).body().getElementsByTag("div").first {
                it.hasClass("body-site")
            }.getElementsByTag("div").first {
                it.hasClass("container") && it.hasClass("container-main")
            }.getElementsByTag("div").first {
                it.hasClass("container-main-left")
            }.getElementsByClass("panel-story-chapter-list").first().getElementsByTag("ul").first()
                .getElementsByTag("li").toList()

            fun hasNumber(string: String): Boolean {
                return Regex(".*\\d.*").containsMatchIn(string)
            }

            for (ch in chapterSoup) {
                val chapterUrl = ch.getElementsByTag("a").first().attr("href").toString()
                var chapString =
                    ch.getElementsByTag("a").first().text().replace("-", " ").replace(":", " ")
                        .replace("Vol.", "Vol ")
                val chapterId = chapterUrl.split("/").last()
                Log.d("TAG", "getChapters: $chapterId ")

                val stream = ByteArrayInputStream(chapString.toByteArray())
                val scanner = Scanner(stream)

                val vol = if (chapString.contains("Vol ")) {
                    scanner.next()
                    scanner.nextInt().toDouble()
                } else {
                    -1.0
                }

                val chapter = if (chapString.contains("Chapter")) {
                    try {
                        scanner.next()
                        scanner.nextDouble()
                    } catch (e: Exception) {
                        if (scanner.hasNextInt()) scanner.nextInt().toDouble() else -1.0
                    }
                } else {
                    -1.0
                }

                val type = "whothefuckcares"
                val title = if (scanner.hasNext()) scanner.next() else ""
                val group = "n/a"
                val pageCount = -1
                val soupyDoup = ch.getElementsByTag("span").first {
                    it.hasClass("chapter-time")
                }.text()
                if (!soupyDoup.contains("hour")){
                    val monthStr = soupyDoup.substring(0, 3)

                    val month = when (monthStr) {
                        "Jan" -> 1
                        "Feb" -> 2
                        "Mar" -> 3
                        "Apr" -> 4
                        "May" -> 5
                        "Jun" -> 6
                        "Jul" -> 7
                        "Aug" -> 8
                        "Sep" -> 9
                        "Oct" -> 10
                        "Nov" -> 11
                        "Dec" -> 12
                        else -> 13
                    }
                    val day = soupyDoup.substring(4, 6)
                    val year = "20" + soupyDoup.substring(7, 9)
                    val date = SimpleDate(day.toInt(), month, year.toInt())

                    chapArr.add(
                        Chapter(
                            title,
                            chapterId,
                            vol,
                            chapter,
                            group,
                            type,
                            pageCount.toDouble(),
                            null,
                            date
                        )
                    )
                }else {
                    val date = SimpleDate()
                    chapArr.add(
                        Chapter(
                            title,
                            chapterId,
                            vol,
                            chapter,
                            group,
                            type,
                            pageCount.toDouble(),
                            null,
                            date
                        )
                    )
                }

            }

            return manga.copy(description = description, demographic = demographic, state = state, alternateTitles = alternateTitles, chapterList = chapArr)
//        } catch (e: Exception){
//            Log.d("TAG", "getChapters: failed $e")
//            return manga
//        }


    }


    suspend fun addToLibrary(manga: MangaInfo) {
        if (idSet.contains(manga.id)){
            Log.d("TAG", "already in library ")
            return
        }
        var mang = manga
        if(manga.chapterList?.size  == 0){
            val chapterList = getChapters(mang)
            Log.d("TAG", "no chapters found trying again")
            library.add(mang)
            idSet.add(manga.id)
            backUpManga()
            Log.d("TAG", "addToLibrary: ${library.map { elm -> elm.title }.toList()} with inlib states ${library.map { elm -> elm.inLibrary }.toList()}")
        } else {
            val d = downloadService.downloadCoverUrl(manga.id, manga.coverArtUrl)
            Log.d("TAG", "addToLibrary: found url $d")
            library.add(mang.copy(coverArtUrl = d))
            idSet.add(manga.id)
            backUpManga()
            Log.d("TAG", "addToLibrary: ${library.map { elm -> elm.title }.toList()} with inlib states ${library.map { elm -> elm.inLibrary }.toList()}")
        }
    }

    suspend fun removeFromLibrary(manga: MangaInfo?){
        library.removeIf { elm -> elm.id == manga?.id }
        Log.d("TAG", "removeFromLibrary: removed ${manga?.id}")
        idSet.remove(manga?.id)
        newUpdatedChapters.removeIf { elm->
            elm.second.mangaId == manga?.id
        }
        backUpManga()
    }

    suspend fun getImageUri(mangaId: String, coverUrl: String): String {
        return downloadService.retrieveImage(mangaId,coverUrl).toString()
    }

    suspend fun getChapterContents(ch: Chapter, mangaId: String): Chapter {
        val id = ch.id
        try {
            val response = natoApi.getChapterPages(mangaId, id)
            val list = mutableListOf<String>()

            val Soupy = Jsoup.parse(response).body().getElementsByClass("body-site").first().getElementsByClass("container-chapter-reader").first().getElementsByTag("img").toList()
            Log.d("TAG", "getChapterContents: found ${Soupy.size} images for ${ch.id}")

            Soupy.forEach { img ->
                list.add(img.attr("src").toString())
            }

            return ch.copy(contents = ChapterContents.Online(list, false), pageCount = list.size.toDouble())
        } catch (e: Exception) {
            return ch
        }

    }
}
//val res = natoApi.getInfo(innerUrl)
