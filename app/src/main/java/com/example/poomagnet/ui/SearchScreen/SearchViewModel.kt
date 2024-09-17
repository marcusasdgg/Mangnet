package com.example.poomagnet.ui.SearchScreen

import Demographic
import Ordering
import Tag
import android.util.Log
import androidx.compose.ui.state.ToggleableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poomagnet.mangaDex.dexApiService.MangaDexRepository
import com.example.poomagnet.mangaDex.dexApiService.MangaInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val mangaDexRepository: MangaDexRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    fun updateSearchText(newText: String){
        _uiState.update {
            it.copy(
                searchText = newText
            )
        }
    }

    fun expandSearchBar(ifExpanded: Boolean) {
        _uiState.update {
            it.copy(
                searchExpanded = ifExpanded,
            )
        }
    }

    fun dropdownSource(ifDropped: Boolean) {
        _uiState.update {
            it.copy(
                sourceExpanded = ifDropped,
            )
        }
    }

    fun changeSource(newSource: Sources) {
        _uiState.update {
            it.copy(
                sourceSelected = newSource,
            )
        }
    }

    suspend fun executeSearch() {
        val res = uiState.value.sortTags.filter { it.value.first }.map { it.key.msg to it.value.second.msg }.first()


        val result = mangaDexRepository.searchAllManga(uiState.value.searchText, ordering = mapOf(res))
        _uiState.update{
            it.copy(
                itemCount = result.second,
                searchListing = result.first,
            )
        }
    }

    fun switchTag(tag: Tag, state: ToggleableState) {
        _uiState.update {
            val updatedTagsIncluded = it.tagsIncluded.toMutableMap().apply {
                this[tag] = state
            }
            it.copy(
                tagsIncluded = updatedTagsIncluded
            )
        }
    }

    suspend fun continueSearch() {
        if (uiState.value.searchListing.size == uiState.value.itemCount-1){
            Log.d("TAG", "continueSearch: failed as search finished")
            return
        } else {
            val result = mangaDexRepository.searchAllManga(uiState.value.searchText, offSet = uiState.value.searchListing.size+1)
            _uiState.update{
                it.copy(
                    searchListing = it.searchListing + result.first,
                )
            }
        }
    }

    fun revealBottomSheet(boolean: Boolean) {
        _uiState.update{
            it.copy(
                showDrawer = boolean
            )
        }
    }

    fun startRefresh(boolean: Boolean){
        _uiState.update {
            it.copy(
                isRefreshing = boolean
            )
        }
    }

    fun loadit() {
        viewModelScope.launch {
            startRefresh(true)
            Log.d("TAG", "loadit: started")
            delay(1000)
            Log.d("TAG", "loadit: ended")
            startRefresh(false)
        }
    }

    fun selectOrder(selected: Ordering, currentState: Pair<Boolean, Direction>) {
        val newMap = _uiState.value.sortTags.toMutableMap().apply {
            for (i in this){
                i.setValue(Pair(false, Direction.Descending))
            }
            if (currentState.second == Direction.Descending && !currentState.first){
                this[selected] = Pair(true, Direction.Descending)
            } else if (currentState.second == Direction.Descending && currentState.first) {
                this[selected] = Pair(true, Direction.Ascending)
            } else if (currentState.second == Direction.Ascending){
                this[selected] = Pair(true, Direction.Descending)
            }
        }

        _uiState.update {
            it.copy(
                sortTags = newMap,
                somethingChanged = true,
            )
        }
    }

    fun setFlag(boolean: Boolean){
        _uiState.update {
            it.copy(
                somethingChanged = boolean
            )
        }
    }

    fun setDemo(demo: Demographic, toggle: ToggleableState){
        val newToggleState = if (toggle == ToggleableState.Off) {ToggleableState.On} else if (toggle == ToggleableState.On){ToggleableState.Indeterminate} else {ToggleableState.Off}
        _uiState.update{
            it.copy(
                demographics = it.demographics.toMutableMap().apply { this[demo] = newToggleState },
                somethingChanged = true
            )
        }
    }

    fun setTag(tag: Tag, toggle: ToggleableState){
        val newToggleState = if (toggle == ToggleableState.Off) {ToggleableState.On} else if (toggle == ToggleableState.On){ToggleableState.Indeterminate} else {ToggleableState.Off}
        _uiState.update{
            it.copy(
                tagsIncluded = it.tagsIncluded.toMutableMap().apply { this[tag] = newToggleState },
                somethingChanged = true
            )
        }
    }

    //search listings, switch page.



    //anything else?

}