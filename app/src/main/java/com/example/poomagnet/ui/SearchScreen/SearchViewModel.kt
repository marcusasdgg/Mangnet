package com.example.poomagnet.ui.SearchScreen

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.ui.state.ToggleableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poomagnet.mangaRepositoryManager.ContentRating
import com.example.poomagnet.mangaRepositoryManager.Demographic
import com.example.poomagnet.mangaRepositoryManager.MangaRepositoryManager
import com.example.poomagnet.mangaRepositoryManager.Ordering
import com.example.poomagnet.mangaRepositoryManager.Sources
import com.example.poomagnet.mangaRepositoryManager.Tag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: MangaRepositoryManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    fun updateSearchText(newText: String){
        _uiState.update {
            it.copy(
                oldText = it.searchText,
                searchText = newText
            )
        }
    }

    fun updateOldSearchText(){
        _uiState.update {
            it.copy(
                oldText = it.searchText
            )
        }
    }

    suspend fun loadImageFromLibrary(mangaId: String, coverUrl: String): String{
        return repo.getImageUri(mangaId, coverUrl)
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
                somethingChanged = true,
            )
        }
    }

    fun addManga(){
        _uiState.update {
            it.copy(
                somethingAdded = true
            )
        }
    }

    fun resetAddManga(){
        _uiState.update {
            it.copy(
                somethingAdded = false
            )
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun executeSearch() {
        val res = uiState.value.sortTags.filter { it.value.first }.map { it.key.msg to it.value.second.msg }.first()

        val s = getIncludeExclude()
        Log.d("TAG", "executeSearch: tags include exclude  are $s")
        val result = uiState.value.sourceSelected.let { t ->
            repo.searchAllManga(uiState.value.searchText,
                ordering = mapOf(res),
                demo = getDemo().map { it.msg }.toList(),
                rating = getContentRating().map { it.msg }.toList(),
                tagsIncluded = s.first,
                tagsExcluded = s.second,
                source = t
            )
        }
        Log.d("TAG", "executeSearch: $result")
        _uiState.update{
            it.copy(
                itemCount = result.second,
                searchListing = result.first,
                pageNumber = 1
            )
        }





    }

    fun getIncludeExclude(): Pair<List<Tag>, List<Tag>> {
        val include = uiState.value.tagsIncluded.entries.filter { it.value == ToggleableState.On }.map { it.key }.toList()
        val exclude = uiState.value.tagsIncluded.entries.filter { it.value == ToggleableState.Indeterminate }.map { it.key }.toList()
        return Pair(include, exclude)
    }

    fun getDemo(): List<Demographic> {
        return uiState.value.demographics.entries.filter { it.value == ToggleableState.On }.map { it.key }.toList()
    }

    fun getContentRating(): List<ContentRating> {
        return uiState.value.contentRating.entries.filter { it.value == ToggleableState.On }.map { it.key }.toList()
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

    fun resetPageNo(){
        Log.d("TAG", "resetPageNo: page number was reset")
        _uiState.update { it.copy(
            pageNumber = 1
        ) }
    }

    suspend fun continueSearch() {
        if (uiState.value.searchListing.size == uiState.value.itemCount-1){
            Log.d("TAG", "continueSearch: failed as search finished")
            return
        } else {
            val res = uiState.value.sortTags.filter { it.value.first }.map { it.key.msg to it.value.second.msg }.first()
            val s = getIncludeExclude()
            val offset = when(uiState.value.sourceSelected){
                Sources.MANGANATO -> {uiState.value.pageNumber + 1}
                Sources.MANGADEX -> {uiState.value.searchListing.size+1}
                Sources.ALL -> {0}
            }
            val result =  repo.searchAllManga(
                uiState.value.searchText,
                offSet = offset,
                ordering = mapOf(res),
                demo = getDemo().map { it.msg }.toList(),
                rating = getContentRating().map { it.msg }.toList(),
                tagsIncluded = s.first,
                tagsExcluded = s.second,
                source = uiState.value.sourceSelected
            )

            Log.d("TAG", "continueSearch:  old pageNo is ${uiState.value.pageNumber}")
            _uiState.update{
                it.copy(
                    searchListing = it.searchListing + result.first,
                    somethingChanged = false,
                    pageNumber = it.pageNumber + 1
                )
            }
            Log.d("TAG", "continueSearch: new pageNo is $offset")
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

    fun selectFirstOrder(selected: Ordering, currentState: Pair<Boolean, Direction>) {
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
        val newToggleState = if (toggle == ToggleableState.Off) {ToggleableState.On} else {ToggleableState.Off}
        _uiState.update{
            it.copy(
                demographics = it.demographics.toMutableMap().apply { this[demo] = newToggleState },
                somethingChanged = true
            )
        }
    }

    fun setTag(tag: Tag, toggle: ToggleableState){
        Log.d("TAG", "setTag: called set tag on $tag")
        val newToggleState = if (toggle == ToggleableState.Off) {ToggleableState.On} else if (toggle == ToggleableState.On){ToggleableState.Indeterminate} else {ToggleableState.Off}
        _uiState.update{
            it.copy(
                tagsIncluded = it.tagsIncluded.toMutableMap().apply { this[tag] = newToggleState },
                somethingChanged = true
            )
        }
    }

    fun setContentRating(rating: ContentRating, toggle: ToggleableState){
        val newToggleState = if (toggle == ToggleableState.Off) {ToggleableState.On}  else {ToggleableState.Off}
        _uiState.update{
            it.copy(
                contentRating = it.contentRating.toMutableMap().apply { this[rating] = newToggleState },
                somethingChanged = true
            )
        }
    }
    fun changeFirstLoad(){
        _uiState.update {
            it.copy(firstLoad = false)
        }
    }

    fun changeSecondLoad(){
        _uiState.update {
            it.copy(secondLoad = false)
        }
    }




    //search listings, switch page.



    //anything else?
    // we need a cache stored here of like the last 15 opened mangas, with the chapter list cached so that
    //if we find the manga again we can immediately load.
}