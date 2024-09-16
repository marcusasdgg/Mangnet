package com.example.poomagnet.ui.SearchScreen

import Tag
import androidx.compose.ui.state.ToggleableState
import com.example.poomagnet.mangaDex.dexApiService.MangaInfo
import included


data class SearchUiState (
    val sourceSelected: Sources = Sources.ALL,
    val sourceExpanded: Boolean = false,
    val searchExpanded: Boolean = false,
    val searchText: String = "",
    val searchListing: List<MangaInfo> = listOf(),
    val itemCount: Int = 0,
    val sortTags: List<String> = listOf(),
    val showDrawer: Boolean = false,
    val tagsIncluded: Map<Tag,ToggleableState> =  Tag.entries.associateWith {ToggleableState.Off}
)