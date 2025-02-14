package com.example.poomagnet.mangaDex.dexApiService


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.poomagnet.downloadService.DownloadService
import com.example.poomagnet.mangaRepositoryManager.Chapter
import com.example.poomagnet.mangaRepositoryManager.ChapterContents
import com.example.poomagnet.mangaRepositoryManager.ChapterContentsDeserializer
import com.example.poomagnet.mangaRepositoryManager.ChapterContentsSerializer
import com.example.poomagnet.mangaRepositoryManager.MangaInfo
import com.example.poomagnet.mangaRepositoryManager.SimpleDate
import com.example.poomagnet.mangaRepositoryManager.SimpleDateAdapter
import com.example.poomagnet.mangaRepositoryManager.SlimChapter
import com.example.poomagnet.mangaRepositoryManager.SlimChapterAdapter
import com.example.poomagnet.mangaRepositoryManager.Tag
import com.example.poomagnet.mangaRepositoryManager.TagDeserializer
import com.example.poomagnet.mangaRepositoryManager.isOnline
import com.example.poomagnet.mangaRepositoryManager.mangaState
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import javax.inject.Inject


data class BackUpInstance(
    val library: MutableList<MangaInfo>,
    var idSet: MutableSet<String> = mutableSetOf(),
    var newUpdatedChapters: MutableList<Pair<SimpleDate, SlimChapter>>,
    val tagMap: MutableMap<Tag,String>
)












//need to store, chapter and volume name


class MangaDexRepository @Inject constructor(val context: Context, private val downloadService: DownloadService)  {
    private val apiService = RetrofitInstance.api
    private var newUpdatedChapters: MutableList<Pair<SimpleDate, SlimChapter>> = mutableListOf()

    fun getNewUpdatedChapters(): List<Pair<SimpleDate, SlimChapter>>{
        return newUpdatedChapters
    }

    fun restoreBackup(jsonString: String){
        // Deserialize the JSON string into a list of MangaInfo objects using Gson
        val listType = object : TypeToken<BackUpInstance>() {}.type
        val r: BackUpInstance = gsonSerializer.fromJson(jsonString, listType)
        library = r.library
        idSet = r.idSet
        newUpdatedChapters = r.newUpdatedChapters
    }

    private val gsonSerializer = GsonBuilder()
        .registerTypeAdapter(ChapterContents::class.java, ChapterContentsSerializer())
        .registerTypeAdapter(ChapterContents::class.java, ChapterContentsDeserializer())
        .registerTypeAdapter(SimpleDate::class.java, SimpleDateAdapter())
        .registerTypeAdapter(SlimChapterAdapter::class.java, SlimChapterAdapter())
        .registerTypeAdapter(Tag::class.java, TagDeserializer())
        .create()

    //local persistence is so much easier now, i just backup

    var library: MutableList<MangaInfo> = mutableListOf()

    private var tagMap: MutableMap<Tag,String> = mutableMapOf()
    private var idSet: MutableSet<String> = mutableSetOf()


    init {
        loadMangaFromBackup(context)
        // after loading all mangas from backup we next devise a function which takes from download service.
        library = library.map { manga ->
            searchDownloaded(manga)
        }.toMutableList()
        setupTags()
    }

    suspend fun getImageUri(mangaId: String, coverUrl: String): String{
        Log.d("TAG", "getImageUri: with mangaId $mangaId, and coverurl $coverUrl")
        return downloadService.retrieveImage(mangaId,coverUrl).toString()
    }

    // given a manga search to see chapters downloaded and return back a list of all downloaded chapters.
    // for now forgo the error correction where backup says not downloaded but is.
    private fun searchDownloaded(manga: MangaInfo): MangaInfo { //: MangaInfo
        val list = manga.chapterList?.map { ch ->
            val list  = downloadService.checkDownloaded(manga.id, ch.id)
            if (list.size != ch.pageCount.toInt()) {
                Log.d("TAG", "searchDownloaded: ${ch.chapter} does not match with the file contents.")
            }
            val contents = ChapterContents.Downloaded(list,false)
            return@map ch.copy(contents = contents)
        }
        return manga.copy(chapterList = list)
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
            val file = File(context.filesDir, "backup_mangadex.txt")
            if (file.exists()) {
                val jsonString = file.readText()
                Log.d("TAG", "loadMangaFromBackup: backup is $jsonString")
                // Deserialize the JSON string into a list of MangaInfo objects using Gson
                val listType = object : TypeToken<BackUpInstance>() {}.type
                val r: BackUpInstance = gsonSerializer.fromJson(jsonString, listType)
                library = r.library
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

        val libraryShouldBe = library.map {element ->
            element.copy(chapterList = element.chapterList?.map { chapter ->
                chapter.copy(contents = if (chapter.contents?.isOnline == true) null else chapter.contents )
            } ?: listOf())
        }.toMutableList()
        val file = File(context.filesDir, "backup_mangadex.txt")
        withContext(Dispatchers.IO) {
            FileOutputStream(file).use { fos ->
                // Create an OutputStreamWriter to write text data
                OutputStreamWriter(fos).use { writer ->
                    // Write the data to the file
                    writer.write(
                        gsonSerializer.toJson(BackUpInstance(libraryShouldBe, idSet, newUpdatedChapters, tagMap))
                    )
                }
            }
        }
        withContext(Dispatchers.IO) {
            printBackUp()
        }
    }

    fun backUpMangaString(): String{
        val libraryShouldBe = library.map {element ->
            element.copy(chapterList = element.chapterList?.map { chapter ->
                chapter.copy(contents = null)
            } ?: listOf())
        }.toMutableList()
        return gsonSerializer.toJson(BackUpInstance(libraryShouldBe, idSet, newUpdatedChapters, tagMap))
    }


    fun CoroutineScope.downloadChapterConcurrently(
        chapterContents: List<String>,
        mangaId: String,
        chapterId: String
    ): List<Pair<Deferred<String>, Boolean>> {  //
        val deferredList = mutableListOf<Pair<Deferred<String>, Boolean>>()
        val semaphore = Semaphore(5)


        for ((index,content) in chapterContents.withIndex()) {
            val deferred = async(Dispatchers.IO) {
                semaphore.withPermit {
                    val result = downloadService.downloadContent(mangaId, chapterId, content) // This returns a String

                    if ((index + 1) % 10 != 0) {
                        delay(1000L / 10)
                    }
                    result
                }
            }
            deferredList.add(Pair(deferred, false))
        }

        return deferredList
    }



    suspend fun downloadChapter(mangaId: String, chapterId: String):Boolean{
        val nameList: MutableList<Pair<String,Boolean>> = mutableListOf()
        val chapterS = library.first { e -> e.id == mangaId }.chapterList?.first { e -> e.id == chapterId }
        val chapterContents = getChapterContents(chapterS!!).contents?.imagePaths
        var list: List<String> = listOf()

        coroutineScope {
            val lists = downloadChapterConcurrently(chapterContents!!, mangaId, chapterId)
            list = lists.map { (deferred) ->
                deferred.await()  // Await the result and pair it with the flag
            }
        }


        var manga = library.find { elm -> elm.id == mangaId }

        val chapList = manga?.chapterList?.toMutableList()


        val chapterIndex: Int = manga?.chapterList?.indexOfFirst { elm ->
            elm.id == chapterId
        } ?: -1

        var chapter = manga?.chapterList?.find { elm ->
            elm.id == chapterId
        }

        chapter = chapter?.copy(
            contents = ChapterContents.Downloaded(list, ifDone = false)
        )

        if (chapter != null) {
            chapList!![chapterIndex] = chapter
        }

        manga = manga?.copy(
            chapterList = chapList!!
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
        if(manga.chapterList?.size  == 0){
            val chapterList = getChapters(mang)
            Log.d("TAG", "no chapters found trying again")
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
                                        mutableListOf(),
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
    //make this return mangainfo with teh new chapters!!!
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getChapters(manga: MangaInfo): MangaInfo {
        val TAG = "TAG"
        val id = manga.id
        Log.d("TAG", "getChapters: ${manga.chapterList}")
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
            val curChapList = manga.chapterList?.toMutableList() ?: mutableListOf()
            val list = mutableListOf<Chapter>()
            for (i in curChapList){
                list.add(i)
            }
            for (i in chapterObjects){
                if (!list.any{e -> e.id == i.id}){
                    list.add(i)
                    if (manga.chapterList?.isNotEmpty() == true && manga.inLibrary){
                        newUpdatedChapters.add(Pair(SimpleDate(), SlimChapter.fromChapter(i, manga)))
                    }
                }
            }


            if (idSet.contains(manga.id)){
                library.removeIf { elm -> elm.id == manga.id }
                library.add(manga.copy(chapterList = list))
            }
            Log.d(TAG, "getChapters: found ${list.size} elements")
            backUpManga()
            return manga.copy(chapterList = list)
        } catch(e: Exception){
            Log.d("TAG", "chapList: failed to get chapters ${e.message}}")
            throw(e)
        }
    }

    //add support for datasaver later.
   suspend fun getChapterContents(ch: Chapter): Chapter {
       val id = ch.id
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
           return ch.copy(contents = ChapterContents.Online(list, false))
       } catch (e: Exception) {
           return ch
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


    // function which given a new manga entry replaces the one with the same id.
    suspend fun updateInLibrary(manga: MangaInfo){
        Log.d("TAG", "updateInLibrary: receieved $manga")
        library = library.map { elm ->
            if (manga.id == elm.id){
                val oldChapters = elm.chapterList?.toMutableList()
                manga.chapterList?.forEach { t ->
                    if (!oldChapters?.any{ e -> e.id == t.id }!!){
                        oldChapters.add(t)
                    } else {
                        val old = oldChapters.indexOfFirst { m -> m.id == t.id }
                        if (old != -1){
                            if (t.finished){
                                oldChapters[old] = t
                            }
                        }
                    }
                }
                manga.copy(chapterList = oldChapters ?: listOf())
            }else {
                elm
            }
        }.toMutableList()

        // get pointer to manga in library.



        backUpManga()
    }

    suspend fun updateWholeLibrary(){
        Log.d("TAG", "updateWholeLibrary: called")
        //use chapterlist which does everything for u idiot.
        try {
            library.map { its ->
                getChapters(its)
            }
            backUpManga()
        } catch (e: Exception){
            Log.d("TAG", "updateWholeLibrary: $e")
        }

    }

    
    suspend fun retrieveImageContent(mangaId: String, chapterId: String, url: String): String{
        return downloadService.retrieveMangaImage(mangaId, chapterId, url).toString()
    }



}

// nut





//right now for library swapped mangaInfo objects, it doesnt properly work? as in it tries to load the chapters but gets a http 400?
//fixed it by increasing limit, must be a rate limti issue i reckon.
//now need to link library/home page to mangadex repo.

//final fixes, if offline dont break anything,
// store tagmap locally.
