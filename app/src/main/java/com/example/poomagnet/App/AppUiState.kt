package com.example.poomagnet.App

data class AppUiState(
    val isShowingHomepage: Boolean = true,
    val currentScreen: ScreenType = ScreenType.Home,
    val topHidden: Boolean = false,
    val botHidden: Boolean = false,

    val followedManga: String = "",
)