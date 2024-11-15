package com.example.poomagnet.mangaRepositoryManager

import androidx.compose.ui.graphics.ImageBitmap

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
    var chapterList: List<Chapter>? = null,
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
){
    companion object {
        // Example "from" constructor
        fun fromChapter(other: Chapter, manga: MangaInfo): slimChapter {
            return slimChapter(
                id = other.id,
                name = other.name,
                chapter = other.chapter,
                volume = other.volume,
                mangaId = manga.id,
                imageUrl = manga.coverArtUrl,
                mangaName = other.name
            )
        }
    }
}




sealed class ChapterContents(@Transient open val  imagePaths: List<String>,
                             @Transient open val ifDone: Boolean) {
    data class Downloaded(override val imagePaths: List<String>,override val ifDone: Boolean) : ChapterContents(imagePaths, ifDone)
    data class Online(override val imagePaths: List<String>, override val ifDone: Boolean) : ChapterContents(imagePaths, ifDone)
}


val ChapterContents.isDownloaded: Boolean
    get() = this is ChapterContents.Downloaded

val ChapterContents.isOnline: Boolean
    get() = this is ChapterContents.Online
