package com.example.poomagnet.ui.HomeScreen

import com.example.poomagnet.mangaRepositoryManager.ContentRating
import com.example.poomagnet.mangaRepositoryManager.Demographic
import com.example.poomagnet.mangaRepositoryManager.Ordering
import com.example.poomagnet.mangaRepositoryManager.Tag
import androidx.compose.ui.state.ToggleableState
import com.example.poomagnet.mangaRepositoryManager.MangaInfo
import com.example.poomagnet.ui.SearchScreen.Direction

data class HomeUiState(
    val typeView: displayType = displayType.VERTICALCARD,
    val expandedMenu: Boolean = false,
    val currentMenuOption: FilterOptions = FilterOptions.All,
    val library: List<MangaInfo> = listOf(),
    val ifLoading: Boolean = false,
    val showDrawer: Boolean = false,
    //val followed manga = ???
    val sortTags: Map<Ordering, Pair<Boolean, Direction>> = Ordering.entries.associateWith { Pair(false, Direction.Descending) }.toMutableMap().apply { this[Ordering.Relevance] = Pair(true,Direction.Descending) },
    val somethingChanged: Boolean = false,
    val demographics: Map<Demographic, ToggleableState> = Demographic.entries.associateWith { ToggleableState.Off },
    val contentRating: Map<ContentRating, ToggleableState> = ContentRating.entries.associateWith { ToggleableState.Off },
    val tagsIncluded: Map<Tag,ToggleableState> =  Tag.entries.associateWith {ToggleableState.Off},
)

enum class FilterOptions{
    All,
    Romance,
    Comedy,
    Action,
    Cultivation,
}