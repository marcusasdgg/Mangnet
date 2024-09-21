package com.example.poomagnet.ui.MangaSpecific

import com.example.poomagnet.mangaDex.dexApiService.Chapter
import com.example.poomagnet.mangaDex.dexApiService.MangaInfo

data class mangaUiState(
    val currentManga: MangaInfo? = null,
    val visible: Boolean = false,
    val inReadMode: Boolean = false,
    val currentChapter: Chapter? = null,
    val readBarVisible: Boolean = false,
    val currentPage: Int = 0,
    val homeBarVisible: Boolean = false,
    val previousChapter: Chapter? = null,
    val nextChapter: Chapter? = null,
    val nextFlag: Boolean = true,
    val latestChapterReadId: String = "",
)


