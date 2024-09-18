package com.example.poomagnet.mangaDex.dexApiService


import Ordering
import Tag
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import javax.inject.Inject

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
    var chapterList: MutableList<Chapter>? = null,
    val tagList: MutableList<String> = mutableListOf()
){
    override fun equals(other: Any?): Boolean{
        if (other !is MangaInfo) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode() // Only hash based on the unique id
    }
}
// on entering MangaPage, we will trigger a request to load chapters for chapterList that will turn,
//the null to a MutableList.





sealed class Chapter {
    data class Downloaded(val imagePaths: List<String>) : Chapter()
    data class Online(val imagePaths: List<String>) : Chapter()
}

val Chapter.isDownloaded: Boolean
    get() = this is Chapter.Downloaded

val Chapter.isOnline: Boolean
    get() = this is Chapter.Online




class MangaDexRepository @Inject constructor(private val context: Context)  {
    private val apiService = RetrofitInstance.api
    //add local database jargon blah blah later. learn SQL.

    private val gsonSerializer = Gson()

    //local persistence is so much easier now, i just backup

    private var library: MutableSet<MangaInfo> = mutableSetOf()

    private var tagMap: MutableMap<Tag,String> = mutableMapOf()


    init {
        loadMangaFromBackup(context)
        setupTags()
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

            } catch (e: Exception) {
                Log.d("TAG", "setupTags: failed due to $e")
            }
            Log.d("TAG", "setupTags: ${tagMap.size}")
        }
    }

    private fun loadMangaFromBackup(context: Context) {
        try {
            // Read the file content from backup.txt
            val file = File(context.filesDir, "backup.txt")
            if (file.exists()) {
                val jsonString = file.readText()

                // Deserialize the JSON string into a list of MangaInfo objects using Gson
                val gson = Gson()
                val listType = object : TypeToken<Set<MangaInfo>>() {}.type
                library = gson.fromJson(jsonString, listType)
            } else {
                Log.d("TAG", "backup.txt not found, mangaObj is empty. ")
            }
        } catch (e: Exception) {
            Log.e("TAG", "Error loading manga from backup.txt: ${e.message}")
        }
    }

    private suspend fun backUpManga(context: Context){
        val file = File(context.filesDir, "backup.txt")
        withContext(Dispatchers.IO) {
            FileOutputStream(file).use { fos ->
                // Create an OutputStreamWriter to write text data
                OutputStreamWriter(fos).use { writer ->
                    // Write the data to the file
                    writer.write(
                        gsonSerializer.toJson(library)
                    )
                }
            }
        }
    }

    suspend fun addToLibrary(manga: MangaInfo) {
        library.add(manga)
        backUpManga(context)
        Log.d("TAG", "addToLibrary: $library")
    }

    suspend fun removeFromLibrary(manga: MangaInfo?){
        library.remove(manga)
        backUpManga(context)
    }

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
                            val type = elm["type"].toString()
                            val attributes = elm["attributes"]
                            if (attributes is Map<*, *>) {
                                var tags: MutableList<String> = mutableListOf()
                                var mangaTitle = "n/a"
                                val titleSearch = attributes["title"]
                                if (titleSearch is Map<*, *>) {
                                    mangaTitle = titleSearch["en"].toString()
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

                                //val image = downloadImage(coverUrl, id)
                                val contructedUrl =
                                    "https://uploads.mangadex.org/covers/$id/$coverUrl"
                                val mangad = MangaInfo(
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
                                    tags
                                )
                                list.add(
                                    mangad.copy(inLibrary = library.contains(mangad))
                                )
                                altlist = mutableListOf()
                            }
                        }
                    }
                }
            } else {
                Log.d("TAG", "searchALlManga: finsihed first get request")
                return Pair(listOf<MangaInfo>(), 0)
            }

            val limit: Int = s["total"].toString().toIntOrNull() ?: 0
            Log.d("TAG", "searchALlManga: finsihed first get request")
            return Pair(list, limit);
        } catch(e : Exception) {
            Log.d("TAG", "search failed $e")
            return Pair(listOf(),0)
        }
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


}