package com.example.poomagnet.ui.SearchScreen

import ContentRating
import Demographic
import Ordering
import Tag
import androidx.compose.ui.state.ToggleableState
import com.example.poomagnet.mangaDex.dexApiService.MangaInfo


data class SearchUiState (
    val sourceSelected: Sources = Sources.ALL,
    val sourceExpanded: Boolean = false,
    val searchExpanded: Boolean = false,
    val searchText: String = "",
    val searchListing: List<MangaInfo> = listOf(),
    val itemCount: Int = 0,
    val sortTags: Map<Ordering, Pair<Boolean, Direction>> = Ordering.entries.associateWith { Pair(false, Direction.Descending) }.toMutableMap().apply { this[Ordering.Relevance] = Pair(true,Direction.Descending) },
    val showDrawer: Boolean = false,
    val tagsIncluded: Map<Tag,ToggleableState> =  Tag.entries.associateWith {ToggleableState.Off},
    val demographics: Map<Demographic,ToggleableState> = Demographic.entries.associateWith { ToggleableState.Off },
    val contentRating: Map<ContentRating, ToggleableState> = ContentRating.entries.associateWith { ToggleableState.Off },
    val isRefreshing: Boolean = false,
    val somethingChanged: Boolean = false,
)

enum class Direction(val msg: String) {
    Ascending("asc"),
    Descending("desc"),
}
