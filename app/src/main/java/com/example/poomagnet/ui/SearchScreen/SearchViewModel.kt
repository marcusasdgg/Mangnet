package com.example.poomagnet.ui.SearchScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.poomagnet.mangaDex.dexApiService.MangaDexRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
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
        ///
        val result = mangaDexRepository.searchAllManga(uiState.value.searchText)
        _uiState.update{
            it.copy(
                itemCount = result.second,
                searchListing = result.first,
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

    //search listings, switch page.



    //anything else?

}