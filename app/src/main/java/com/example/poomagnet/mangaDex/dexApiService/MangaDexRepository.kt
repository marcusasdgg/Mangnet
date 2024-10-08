package com.example.poomagnet.mangaDex.dexApiService


import Demographic
import Tag
import TagDeserializer
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.ImageBitmap
import com.example.poomagnet.downloadService.DownloadService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.time.OffsetDateTime
import javax.inject.Inject

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.reflect.Type
import java.util.Date



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
    val coverArt: ImageBitmap?,
    val coverArtUrl: String,
    val offSet: Int,
    var inLibrary: Boolean = false,
    var chapterList: Pair<Date, List<Chapter>>? = null,
    val tagList: MutableList<String> = mutableListOf(),
    val lastReadChapter: Pair<String,Int> = Pair("",0),
    val demographic: String
)
// on entering MangaPage, we will trigger a request to load chapters for chapterList that will turn,
//the null to a MutableList.


data class Chapter(
    val name: String,
    val id: String,
    val volume: Double,
    val chapter: Double,
    val group: String,
    val type: String,
    val pageCount: Double,
    val contents: ChapterContents? = null,
    val date: SimpleDate? = null,
    val lastPageRead: Int = 0,
    val finished: Boolean = false,

)

data class slimChapter(
    val id: String,
    val name: String,
    val chapter: Double,
    val volume: Double,
    val mangaId: String,
    val imageUrl: String,
    val mangaName: String,
)


data class BackUpInstance(
    val library: MutableSet<MangaInfo>,
    val idSet: MutableSet<String>,
    var newUpdatedChapters: MutableList<Pair<SimpleDate, slimChapter>>,
    val tagMap: MutableMap<Tag,String>
)








sealed class ChapterContents {
    data class Downloaded(val imagePaths: List<Pair<String, Boolean>>, val ifDone: Boolean) : ChapterContents()
    data class Online(val imagePaths: List<Pair<String, Boolean>>, val ifDone: Boolean) : ChapterContents()
}

val ChapterContents.isDownloaded: Boolean
    get() = this is ChapterContents.Downloaded

val ChapterContents.isOnline: Boolean
    get() = this is ChapterContents.Online

class ChapterContentsSerializer : JsonSerializer<ChapterContents> {
    override fun serialize(src: ChapterContents, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        Log.d("TAG", "serialize: ")
        val jsonObject = JsonObject()

        when (src) {
            is ChapterContents.Downloaded -> {
                jsonObject.addProperty("type", "Downloaded")
                jsonObject.add("imagePaths", context.serialize(src.imagePaths))
                jsonObject.addProperty("ifDone", src.ifDone)
            }
            is ChapterContents.Online -> {
                jsonObject.addProperty("type", "Online")
                jsonObject.add("imagePaths", context.serialize(src.imagePaths))
                jsonObject.addProperty("ifDone", src.ifDone)
            }
        }

        return jsonObject
    }
}

class ChapterContentsDeserializer : JsonDeserializer<ChapterContents> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ChapterContents {
        Log.d("TAG", "serialize: ")
        val jsonObject = json.asJsonObject
        val imagePathsType = object : TypeToken<List<Pair<String, Boolean>>>() {}.type
        val imagePaths: List<Pair<String, Boolean>> = context.deserialize(jsonObject.get("imagePaths"), imagePathsType)
        val ifDone = jsonObject.get("ifDone").asBoolean

        return when (jsonObject.get("type").asString) {
            "Downloaded" -> ChapterContents.Downloaded(imagePaths, ifDone)
            "Online" -> ChapterContents.Online(imagePaths, ifDone)
            else -> throw JsonParseException("Unknown ChapterContents type")
        }
    }
}


//need to store, chapter and volume name


class MangaDexRepository @Inject constructor(private val context: Context, private val downloadService: DownloadService)  {
    private val apiService = RetrofitInstance.api
    var newUpdatedChapters: MutableList<Pair<SimpleDate, slimChapter>> = mutableListOf()


    private val gsonSerializer = GsonBuilder()
        .registerTypeAdapter(ChapterContents::class.java, ChapterContentsSerializer())
        .registerTypeAdapter(ChapterContents::class.java, ChapterContentsDeserializer())
        .registerTypeAdapter(SimpleDate::class.java, SimpleDateAdapter())
        .registerTypeAdapter(SlimChapterAdapter::class.java, SlimChapterAdapter())
        .registerTypeAdapter(Tag::class.java, TagDeserializer())
        .create()

    //local persistence is so much easier now, i just backup

    var library: MutableSet<MangaInfo> = mutableSetOf()
    private var idSet: MutableSet<String> = mutableSetOf()

    private var tagMap: MutableMap<Tag,String> = mutableMapOf()


    init {
        loadMangaFromBackup(context)
        setupTags()
    }

    suspend fun getImageUri(mangaId: String, coverUrl: String): String{
        return downloadService.retrieveImage(mangaId,coverUrl).toString()
    }

    private fun setupTags(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getTagList()["data"]

                if (response is List<*>) {
                    response.forEach { elm ->
                        if (elm is Map<*,*>){
                            val id: String = elm["id"].toString()
                            val attribute = elm["attributes"]
                            if (attribute is Map<*,*>){
                                val name = attribute["name"]
                                if (name is Map<*,*>){
                                    val tag = Tag.fromValue(name["en"].toString())
                                    if (tag != null) {
                                        tagMap[tag] = id
                                    } else {
                                        Log.d("TAG", "setupTags: tag configuration went wrong")
                                    }
                                }
                            }
                        }
                    }
                }
                Log.d("TAG", "setupTags: $tagMap")

            } catch (e: Exception) {
                Log.d("TAG", "setupTags: failed due to $e")
            }
            Log.d("TAG", "setupTags: ${tagMap.size}")
        }
    }

    private fun printBackUp(){
        val file = File(context.filesDir, "backup.txt")
        if (file.exists()){
            Log.d("TAG", "printBackUp: ${file.readText()}")
        }
    }
    
    private fun printLibrary(){
        Log.d("TAG", "printLibrary: ${library.map { elm -> elm.coverArtUrl }}")
    }




    private fun loadMangaFromBackup(context: Context) {
        try {
            // Read the file content from backup.txt
            val file = File(context.filesDir, "backup.txt")
            if (file.exists()) {
                val jsonString = file.readText()
                Log.d("TAG", "loadMangaFromBackup: backup is $jsonString")
                // Deserialize the JSON string into a list of MangaInfo objects using Gson
                val listType = object : TypeToken<BackUpInstance>() {}.type
                val r: BackUpInstance = gsonSerializer.fromJson(jsonString, listType)
                library = r.library
                idSet = r.idSet
                newUpdatedChapters = r.newUpdatedChapters
                //tagMap = r.tagMap
                Log.d("TAG", "loadMangaFromBackup initalize: $newUpdatedChapters")
                printLibrary()
            } else {
                Log.d("TAG", "backup.txt not found, mangaObj is empty. ")
            }
        } catch (e: Exception) {
            Log.e("TAG", "Error loading manga from backup.txt", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun backUpManga(){
        Log.d("TAG", "commencing backup: $library")

        val libraryShouldBe = library.map {element ->
            element.copy(chapterList = Pair(Date(), element.chapterList?.second?.map { chapter ->
                chapter.copy(contents = if (chapter.contents?.isOnline == true) null else chapter.contents )
            } ?: listOf()))
        }
        val file = File(context.filesDir, "backup.txt")
        withContext(Dispatchers.IO) {
            FileOutputStream(file).use { fos ->
                // Create an OutputStreamWriter to write text data
                OutputStreamWriter(fos).use { writer ->
                    // Write the data to the file
                    writer.write(
                        gsonSerializer.toJson(BackUpInstance(library, idSet, newUpdatedChapters, tagMap))
                    )
                }
            }
        }
        withContext(Dispatchers.IO) {
            printBackUp()
        }
    }


    fun CoroutineScope.downloadChapterConcurrently(
        chapterContents: List<Pair<String, Any>>,
        mangaId: String,
        chapterId: String
    ): List<Pair<Deferred<String>, Boolean>> {  // Change to Deferred<String>
        val deferredList = mutableListOf<Pair<Deferred<String>, Boolean>>()

        // Launch all download tasks asynchronously
        for (i in chapterContents) {
            val deferred = async(Dispatchers.IO) {
                downloadService.downloadContent(mangaId, chapterId, i.first) // This returns a String
            }
            deferredList.add(Pair(deferred, false))
            Log.d("TAG", "downloadChapter: Task launched for $i")
        }

        return deferredList
    }



    suspend fun downloadChapter(mangaId: String, chapterId: String):Boolean{
        val nameList: MutableList<Pair<String,Boolean>> = mutableListOf()
        val chapterContents = getChapterContents(chapterId).imagePaths
        var list: List<Pair<String,Boolean>> = listOf()
        coroutineScope {
            val lists = downloadChapterConcurrently(chapterContents, mangaId, chapterId)
            list = lists.map { (deferred, flag) ->
                Pair(deferred.await(), flag)  // Await the result and pair it with the flag
            }
        }

        var manga = library.find { elm -> elm.id == mangaId }

        val chapList = manga?.chapterList?.second?.toMutableList()


        val chapterIndex: Int = manga?.chapterList?.second?.indexOfFirst { elm ->
            elm.id == chapterId
        } ?: -1

        var chapter = manga?.chapterList?.second?.find { elm ->
            elm.id == chapterId
        }

        chapter = chapter?.copy(
            contents = ChapterContents.Downloaded(list, ifDone = false)
        )

        if (chapter != null) {
            chapList!![chapterIndex] = chapter
        }

        manga = manga?.copy(
            chapterList = Pair(Date(),chapList!!)
        )


        library.removeIf { elm -> elm.id == mangaId }
        library.add(manga!!)

        backUpManga()

        return true
    }

    suspend fun addToLibrary(manga: MangaInfo) {
        if (idSet.contains(manga.id)){
            Log.d("TAG", "already in library ")
            return
        }
        var mang = manga
        if(manga.chapterList?.second?.size  == 0){
            val chapterList = chapList(mang.id)
            Log.d("TAG", "no chapters found trying again")
            mang = manga.copy(chapterList = Pair(chapterList.second, chapterList.first), coverArtUrl = downloadService.downloadCoverUrl(manga.id, manga.coverArtUrl))
            library.add(mang)
            idSet.add(manga.id)
            printLibrary()
            backUpManga()
            Log.d("TAG", "addToLibrary: ${library.map { elm -> elm.title }.toList()} with inlib states ${library.map { elm -> elm.inLibrary }.toList()}")
        } else {
            val d = downloadService.downloadCoverUrl(manga.id, manga.coverArtUrl)
            Log.d("TAG", "addToLibrary: found url $d")
            library.add(mang.copy(coverArtUrl = d))
            idSet.add(manga.id)
            printLibrary()
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

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun searchAllManga(title: String, offSet: Int = 0, ordering: Map<String,String> = mapOf(), demo: List<String>, tagsIncluded: List<Tag>, tagsExcluded: List<Tag>, rating: List<String>): Pair<List<MangaInfo>, Int> {//search including stuff like coverpage url.
        Log.d("TAG", "searchALlManga: starting first get request")

        val tI = tagsIncluded.map { tagMap[it] }.toList().filterNotNull()
        val tE = tagsExcluded.map{tagMap[it]}.toList().filterNotNull()
        try {
            val s = apiService.mangaSearchSimple(title, offSet, listOf("cover_art"),tI ,tE, demo, rating,ordering)

            val list: MutableList<MangaInfo> = mutableListOf()
            if (s["result"] == "ok") {
                var altlist: MutableList<String> = mutableListOf()
                val searchArray = s["data"]
                if (searchArray is List<Any?>) {
                    Log.d("TAG", "there are ${searchArray.size} search results")
                    searchArray.forEach { elm ->
                        Log.d("TAG", "result found")
                        if (elm is Map<*, *>) {
                            val id = elm["id"].toString()

                            if (idSet.contains(id)){
                                val em = library.firstOrNull { e -> e.id == id }
                                if (em !== null){
                                    list.add(em)
                                    Log.d("TAG", "found manga in lib")
                                    return@forEach
                                } else {
                                    Log.d("TAG", "searchAllManga: Id set not synchronized")
                                }
                            }
                            val type = elm["type"].toString()
                            val attributes = elm["attributes"]
                            if (attributes is Map<*, *>) {
                                var tags: MutableList<String> = mutableListOf()
                                var mangaTitle = "n/a"
                                val titleSearch = attributes["title"]
                                if (titleSearch is Map<*, *>) {
                                   if (titleSearch["en"] == null){
                                       Log.d("TAG", "searchAllManga found titles: $titleSearch")
                                        mangaTitle = titleSearch["ja-ro"].toString()
                                   } else {
                                       mangaTitle = titleSearch["en"].toString()
                                   }
                                }

                                val tagSon = attributes["tags"]

                                if (tagSon is List<*>){
                                    tagSon.forEach { elm ->
                                        if (elm is Map<*,*>){
                                            val tagAttr = elm["attributes"]
                                            if (tagAttr is Map<*,*>){
                                                val tagName = tagAttr["name"]
                                                if (tagName is Map<*,*>){
                                                    tags.add(tagName["en"].toString())
                                                }
                                            }
                                        }
                                    }
                                }


                                val altTitles = attributes["altTitles"]
                                if (altTitles is List<*>) {
                                    altTitles.forEach { titl ->
                                        if (titl is Map<*, *>) {
                                            titl.forEach { (key, value) -> altlist.add(value.toString()) }
                                        }
                                    }
                                }

                                val descriptions = attributes["description"]
                                var description = ""
                                if (descriptions is Map<*, *>) {
                                    description = descriptions["en"].toString()
                                }

                                val state = mangaState.IN_PROGRESS;
                                val contentRating = attributes["contentRating"].toString()
                                val languageList: MutableList<String> = mutableListOf()
                                val jl = attributes["availableTranslatedLanguages"]
                                if (jl is List<*>) {
                                    jl.forEach { lan -> languageList.add(lan.toString()) }
                                }

                                val relationships = elm["relationships"]
                                var coverUrl = ""
                                if (relationships is List<*>) {
                                    relationships.forEach { relation ->
                                        if (relation is Map<*, *>) {
                                            if (relation["type"] == "cover_art") {
                                                val rel_attr = relation["attributes"]
                                                if (rel_attr is Map<*, *>) {
                                                    coverUrl = rel_attr["fileName"].toString()
                                                }
                                            }
                                        }
                                    }
                                }
                                val demographic = attributes["publicationDemographic"].toString()

                                //val image = downloadImage(coverUrl, id)
                                val contructedUrl =
                                    "https://uploads.mangadex.org/covers/$id/$coverUrl"

                                list.add(
                                    MangaInfo(
                                        id,
                                        type,
                                        mangaTitle,
                                        altlist,
                                        description,
                                        state,
                                        contentRating,
                                        languageList,
                                        null,
                                        contructedUrl,
                                        offSet,
                                        false,
                                        Pair(Date(0),mutableListOf()),
                                        tags,
                                        demographic = demographic
                                    )
                                )
                                altlist = mutableListOf()
                            }
                        }
                    }
                }
            } else {
                return Pair(listOf<MangaInfo>(), 0)
            }

            val limit: Int = s["total"].toString().toIntOrNull() ?: 0
            return Pair(list, limit);
        } catch(e : Exception) {
            Log.d("TAG", "search failed $e")
            return Pair(listOf(),0)
        }
    }

    // make it so that it can pull off library autmatically instead of calling a get request.
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun chapList(id: String): Pair<List<Chapter>, Date> {
        val TAG = "TAG"
        try {
            val responses = mutableListOf(apiService.getChapterList(id,0))
            val totalChapters = responses[0]["total"] as Double
            var offSet = 0
            while (offSet < totalChapters){
                responses.add(apiService.getChapterList(id,offSet))
                offSet += 1000
            }
            val chapterObjects: MutableList<Chapter> = mutableListOf()

            responses.forEach { res ->
                val reponse = res["data"]
                Log.d(TAG, "chapList: $reponse")
                if (reponse is List<*>){
                    reponse.forEach { response ->
                        if (response is Map<*,*>){
                            val chapterId = response["id"].toString()
                            val attributes = response["attributes"]
                            if (attributes is Map<*,*>){
                                if (attributes["translatedLanguage"] != "en"){
                                    Log.d(TAG, "chapList: found non english chapter")
                                    return@forEach
                                }
                                val volume = attributes["volume"].toString().toDoubleOrNull() ?: -1.0
                                val chapter = attributes["chapter"].toString().toDoubleOrNull() ?: -1.0
                                val title = attributes["title"] as? String ?: ""
                                val pageCount = attributes["pages"] as? Double ?: -1.0
                                val time = SimpleDate(attributes["readableAt"].toString())
                                val type = response["type"].toString()

                                val relationships = response["relationships"]
                                var group = ""

                                if (relationships is List<*>){
                                    relationships.forEach { relation ->
                                        if (relation is Map<*,*>){
                                            if( relation["type"] == "scanlation_group"){
                                                val relAttr = relation["attributes"]
                                                if (relAttr is Map<*,*>){
                                                    group = relAttr["name"].toString()
                                                }
                                            }
                                        }
                                    }
                                }

                                chapterObjects.add(Chapter(title,chapterId,volume,chapter,group,type,pageCount,null,time))
                            }
                        }
                    }
                }
            }

            library.map { elm ->
                if (elm.id == id){
                    elm.copy(chapterList = Pair(Date(),chapterObjects))
                } else {
                    elm
                }
            }
            backUpManga()
            return Pair(chapterObjects, Date())

        } catch(e: Exception){
            Log.d("TAG", "chapList: failed to get chapters ${e.message}}")
            return Pair(listOf(), Date())
        }
    }

    //add support for datasaver later.
   suspend fun getChapterContents(id: String): ChapterContents.Online {
       try {
           val response = apiService.getChapterPagesInfo(id)
           val baseUrl = response["baseUrl"]
           val chapterInfo = response["chapter"]
           Log.d("TAG", "getChapterContents: response is $response")
           var hash = ""
           val list: MutableList<String> = mutableListOf()
           if (chapterInfo is Map<*,*>){
               hash = chapterInfo["hash"].toString()
               val data = chapterInfo["data"]
               if (data is List<*>){
                   data.forEach { elm->
                       list.add("$baseUrl/data/$hash/$elm")
                   }
               }
           }
           return ChapterContents.Online(list.map {elm -> Pair(elm, false) }, false)
       } catch (e: Exception) {
           return ChapterContents.Online(listOf(), false)
       }

    }

//    suspend fun downloadChapterContents(mangaId: String, id: String): ChapterContents.Downloaded{
//        val contents = getChapterContents(id)
//        if (contents is ChapterContents.Online){
//            val imagePaths = contents.imagePaths.map { its -> its.first }.toList()
//            val downloadedContents = downloadService.downloadChapter(id, mangaId, imagePaths)
//            val curinLib = library.find { its -> its.id == id }
//            if (curinLib !== null){
//                val List = curinLib.chapterList
//                if (List !== null){
//                    val index: Int = List.second.indexOfFirst { chap -> chap.id == id }
//                    val newList = List.second.toMutableList()
//                    if (index != -1) {
//                        newList[index] = newList[index].copy(contents = downloadedContents)
//                    }
//                    val newLib = curinLib.copy(
//                        chapterList = Pair(Date(),newList)
//                    )
//
//                    library.removeIf { ot -> ot.id == mangaId }
//                    library.add(newLib)
//                    backUpManga()
//                    return downloadedContents
//                }
//
//            } else {
//                return ChapterContents.Downloaded(listOf(), false)
//            }
//
//        } else {
//            return ChapterContents.Downloaded(listOf(), false)
//        }
//        return ChapterContents.Downloaded(listOf(), false)
//    }

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

    suspend fun updateInLibrary(manga: MangaInfo){
        Log.d("TAG", "updateInLibrary: receieved $manga")
        val currentDate = SimpleDate(OffsetDateTime.now().toString())
        library = library.map { elm ->
            if (manga.id == elm.id){
                Log.d("TAG", "updateInLibrary: found manga")
                val oldChapters = elm.chapterList?.second?.toMutableList()
                manga.chapterList?.second?.forEach { t ->
                    if (!oldChapters?.any{ e -> e.id == t.id }!!){
                        oldChapters.add(t)
                        newUpdatedChapters.add(Pair(currentDate, slimChapter(t.id,t.name,t.chapter,t.volume, elm.id, elm.coverArtUrl, elm.title )))
                    } else {
                        val old = oldChapters.indexOfFirst { m -> m.id == t.id }
                        if (old != -1){
                            if (t.finished){
                                oldChapters[old] = t
                            }
                        }
                    }
                }
                manga.copy(chapterList = Pair(Date(),oldChapters ?: listOf()))
            }else {
                elm
            }
        }.toMutableSet()

        backUpManga()
    }

    suspend fun updateWholeLibrary(){
        Log.d("TAG", "updateWholeLibrary: called")
        try {
            library.map { its ->
                val result = chapList(its.id).first
                val current = its.chapterList?.second?.toMutableList()
                if (current !== null){
                    result.forEach { el ->
                        if (!current.any { t -> t.id == el.id }){
                            if (!newUpdatedChapters.any { s -> s.second.id == el.id  }){
                                val currentDate = SimpleDate(OffsetDateTime.now().toString())
                                newUpdatedChapters.add(Pair(currentDate, slimChapter(el.id,el.name,el.chapter,el.volume, its.id, its.coverArtUrl, its.title )))

                            }
                        }
                    }
                    its.copy(chapterList = Pair(Date(), current.toList()) )
                }else {
                    its
                }
            }
            backUpManga()
        } catch (e: Exception){
            Log.d("TAG", "updateWholeLibrary: $e")
        }

    }

    fun addToList(chapter: Chapter, mangaId: String, url: String, name: String){
        try {
            if (!newUpdatedChapters.any { elm -> elm.second.id == chapter.id }){
                newUpdatedChapters.add(Pair(SimpleDate(OffsetDateTime.now().toString()), slimChapter(chapter.id, chapter.name,chapter.chapter,chapter.volume,mangaId,url,name)))
            }

        } catch (e: Exception){
            Log.d("TAG", "addToList: $e")
        }

    }
    
    suspend fun retrieveImageContent(mangaId: String, chapterId: String, url: String): String{
        return downloadService.retrieveMangaImage(mangaId, chapterId, url).toString()
    }



}

// nut

data class SimpleDate(
    val day: Int,
    val month: Int,
    val year: Int,
){
    override fun toString(): String {
        return "$day/$month/$year"
    }

    constructor(date: String): this(
        date.split("-")[2].substring(0..1).toInt(),
        date.split("-")[1].toInt(),
        date.split("-")[0].toInt()
    )
}

class SimpleDateAdapter : JsonDeserializer<SimpleDate>, JsonSerializer<SimpleDate> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): SimpleDate {
        val jsonObject = json?.asJsonObject
        return SimpleDate(
            jsonObject?.get("year")?.asInt ?: 0,
            jsonObject?.get("month")?.asInt ?: 0,
            jsonObject?.get("day")?.asInt ?: 0
        )
    }

    override fun serialize(src: SimpleDate, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("year", src.year)
        jsonObject.addProperty("month", src.month)
        jsonObject.addProperty("day", src.day)
        return jsonObject
    }
}

class SlimChapterAdapter : JsonDeserializer<slimChapter>, JsonSerializer<slimChapter> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): slimChapter {
        val jsonObject = json?.asJsonObject
        return slimChapter(
            id = jsonObject?.get("id")?.asString ?: "",
            name = jsonObject?.get("name")?.asString ?: "",
            chapter = jsonObject?.get("chapter")?.asDouble ?: 0.0,
            volume = jsonObject?.get("volume")?.asDouble ?: 0.0,
            mangaId = jsonObject?.get("mangaId")?.asString ?: "",
            imageUrl = jsonObject?.get("imageUrl")?.asString ?: "",
            mangaName = jsonObject?.get("mangaName")?.asString ?: ""
        )
    }

    override fun serialize(src: slimChapter, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("id", src.id)
        jsonObject.addProperty("name", src.name)
        jsonObject.addProperty("chapter", src.chapter)
        jsonObject.addProperty("volume", src.volume)
        jsonObject.addProperty("mangaId", src.mangaId)
        jsonObject.addProperty("imageUrl", src.imageUrl)
        jsonObject.addProperty("mangaName", src.mangaName)
        return jsonObject
    }
}

//right now for library swapped mangaInfo objects, it doesnt properly work? as in it tries to load the chapters but gets a http 400?
//fixed it by increasing limit, must be a rate limti issue i reckon.
//now need to link library/home page to mangadex repo.

//final fixes, if offline dont break anything,
// store tagmap locally.
