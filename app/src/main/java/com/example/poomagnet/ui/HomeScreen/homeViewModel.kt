    package com.example.poomagnet.ui.HomeScreen


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.poomagnet.mangaDex.dexApiService.MangaDexRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.logging.Filter
import javax.inject.Inject

    //make this a hilt view model and inject mangadex api object in.
@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class HomeViewModel @Inject constructor(
    private val mangaDexRepository: MangaDexRepository
    ): ViewModel() {

    private val _uistate = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uistate


    public fun syncLibrary(){
        _uistate.update {
            it.copy(
                library = mangaDexRepository.library.toList()
            )
        }
    }

    fun loadIt(boolean: Boolean){
        _uistate.update {
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
