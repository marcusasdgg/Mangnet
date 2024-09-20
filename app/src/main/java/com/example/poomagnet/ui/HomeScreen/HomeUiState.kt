package com.example.poomagnet.ui.HomeScreen

import com.example.poomagnet.mangaDex.dexApiService.MangaInfo

data class HomeUiState(
    val typeView: displayType = displayType.VERTICALCARD,
    val expandedMenu: Boolean = false,
    val currentMenuOption: FilterOptions = FilterOptions.All,
    val library: List<MangaInfo> = listOf()
    //val followed manga = ???
)

enum class FilterOptions{
    All,
    Romance,
    Comedy,
    Action,
    Cultivation,
}