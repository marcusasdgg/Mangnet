package com.example.poomagnet.ui.SearchScreen

import com.example.poomagnet.mangaDex.dexApiService.MangaInfo


data class SearchUiState (
    val sourceSelected: Sources = Sources.ALL,
    val sourceExpanded: Boolean = false,
    val searchExpanded: Boolean = false,
    val searchText: String = "",
    val searchListing: List<MangaInfo> = listOf(),
    val itemCount: Int = 0,
    val sortTags: List<String> = listOf(),
    val showDrawer: Boolean = false,
)