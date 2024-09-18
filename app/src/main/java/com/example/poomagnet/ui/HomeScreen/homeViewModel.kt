    package com.example.poomagnet.ui.HomeScreen


import androidx.lifecycle.ViewModel
import com.example.poomagnet.mangaDex.dexApiService.MangaDexRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.logging.Filter

class HomeViewModel: ViewModel() {
    private val _uistate = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uistate


    public fun toggleDropDown(boolean: Boolean){
        _uistate.update {
            it.copy(
                expandedMenu = boolean
            )
        }
    }

    public fun changeDropDown(new: FilterOptions) {
        _uistate.update {
            it.copy(
                currentMenuOption = new
            )
        }
    }

}
