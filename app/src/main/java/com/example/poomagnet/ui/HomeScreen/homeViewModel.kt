    package com.example.poomagnet.ui.HomeScreen


import com.example.poomagnet.mangaRepositoryManager.ContentRating
import com.example.poomagnet.mangaRepositoryManager.Demographic
import com.example.poomagnet.mangaRepositoryManager.Ordering
import com.example.poomagnet.mangaRepositoryManager.Tag
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.ui.state.ToggleableState
import androidx.lifecycle.ViewModel
import com.example.poomagnet.mangaRepositoryManager.MangaInfo
import com.example.poomagnet.mangaRepositoryManager.MangaRepositoryManager
import com.example.poomagnet.ui.SearchScreen.Direction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

    //make this a hilt view model and inject mangadex api object in.
@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class HomeViewModel @Inject constructor(
    private val repo: MangaRepositoryManager
    ): ViewModel() {
        private val mangaDexRepository = repo.getMangaDexRepo()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState


    public fun syncLibrary(){
        _uiState.update {
            it.copy(
                library = mangaDexRepository.library.toList()
            )
        }
    }

    suspend fun loadImageFromLibrary(mangaId: String, coverUrl: String): String{
        return mangaDexRepository.getImageUri(mangaId, coverUrl)
    }

    fun loadIt(boolean: Boolean){
        _uiState.update {
            it.copy(
                ifLoading = boolean
            )
        }
    }

    suspend fun updateAll(){
        mangaDexRepository.updateWholeLibrary()
        syncLibrary()
    }


    public fun toggleDropDown(boolean: Boolean){
        _uiState.update {
            it.copy(
                expandedMenu = boolean
            )
        }
    }

    public fun changeDropDown(new: FilterOptions) {
        _uiState.update {
            it.copy(
                currentMenuOption = new
            )
        }
    }

    fun revealBottomSheet(boolean: Boolean){
        _uiState.update {
            it.copy(
                showDrawer = boolean
            )
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

    fun setDemo(demo: Demographic, toggle: ToggleableState){
        val newToggleState = if (toggle == ToggleableState.Off) {
            ToggleableState.On} else {
            ToggleableState.Off}
        _uiState.update{
            it.copy(
                demographics = it.demographics.toMutableMap().apply { this[demo] = newToggleState },
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

        fun recompose() {
            val oldLibrary = mangaDexRepository.library.toList()
            var library: MutableList<MangaInfo> = oldLibrary.toMutableList()

            val (includ, exclud) = getIncludeExclude()
            val included = includ.map { item -> item.full_name }
            val excluded = exclud.map { item -> item.full_name }
            val demo = getDemo().map { item -> item.msg }
            val rating = getContentRating().map { item -> item.msg }

            // Filter library by demographic (if demo is not empty)
            if (demo.isNotEmpty()) {
                library = library.filter { it.demographic in demo }.toMutableList()
            }

            // Filter library by content rating (if rating is not empty)
            if (rating.isNotEmpty()) {
                library = library.filter { it.contentRating in rating }.toMutableList()
            }

            // Filter by included tags (if included is not empty)
            if (included.isNotEmpty()) {
                library = library.filter { manga ->
                    // Check if all included tags are in the manga's taglist
                    included.all { it in manga.tagList}
                }.toMutableList()
            }

            // Filter out excluded tags (if excluded is not empty)
            if (excluded.isNotEmpty()) {
                library = library.filter { manga ->
                    // Ensure none of the excluded tags are in the manga's taglist
                    excluded.none { it in manga.tagList }
                }.toMutableList()
            }

            // Now library contains the filtered list based on demo, rating, included, and excluded tags.
            // You can update the repository or UI with this new filtered library.

            _uiState.update {
                it.copy(
                    library = library
                )
            }
        }


    fun resetFlag(){
        _uiState.update {
            it.copy(
                somethingChanged = false
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



    }
