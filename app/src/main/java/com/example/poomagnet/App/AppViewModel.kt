package com.example.poomagnet.App

import androidx.compose.material3.rememberModalBottomSheetState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poomagnet.mangaDex.dexApiService.MangaDexRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


class AppViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState


    fun changeScreen(screenType: ScreenType) {
        _uiState.update {
            it.copy(
                previousScreen = it.currentScreen,
                currentScreen = screenType
            )
        }
    }

    fun hideBotBar(res: Boolean) {
        _uiState.update {
            it.copy(
                botHidden = res
            )
        }
    }

    fun hideTopBar(res: Boolean) {
        _uiState.update {
            it.copy(
                topHidden = res
            )
        }
    }



    fun changeToPrevious(){

        _uiState.update {
            it.copy(
                currentScreen = _uiState.value.previousScreen
            )
        }
    }


}