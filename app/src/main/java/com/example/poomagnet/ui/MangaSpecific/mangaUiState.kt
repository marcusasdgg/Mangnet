package com.example.poomagnet.ui.MangaSpecific

import com.example.poomagnet.mangaDex.dexApiService.MangaInfo

data class mangaUiState(
    val currentManga: MangaInfo? = null,
    val visible: Boolean = false,
)


