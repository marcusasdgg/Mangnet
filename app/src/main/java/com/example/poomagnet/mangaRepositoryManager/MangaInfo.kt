package com.example.poomagnet.mangaRepositoryManager

import androidx.compose.ui.graphics.ImageBitmap
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

sealed class ChapterContents {
    data class Downloaded(val imagePaths: List<Pair<String, Boolean>>, val ifDone: Boolean) : ChapterContents()
    data class Online(val imagePaths: List<Pair<String, Boolean>>, val ifDone: Boolean) : ChapterContents()
}

val ChapterContents.isDownloaded: Boolean
    get() = this is ChapterContents.Downloaded

val ChapterContents.isOnline: Boolean
    get() = this is ChapterContents.Online
