package com.example.poomagnet.App

import com.example.poomagnet.mangaDex.dexApiService.MangaInfo

data class AppUiState(
    val isShowingHomepage: Boolean = true,
    val currentScreen: ScreenType = ScreenType.Home,
    val topHidden: Boolean = false,
    val botHidden: Boolean = false,

    val currentManga: MangaInfo? = null,
    val previousScreen: ScreenType = ScreenType.Home,

)