package com.example.poomagnet.ui.MangaSpecific

import com.example.poomagnet.mangaDex.dexApiService.Chapter
import com.example.poomagnet.mangaDex.dexApiService.MangaInfo

data class mangaUiState(
    val currentManga: MangaInfo? = null,
    val visible: Boolean = false,
    val inReadMode: Boolean = false,
    val currentChapter: Chapter? = null,
)


