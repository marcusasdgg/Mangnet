package com.example.poomagnet.App

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState
    val showBottombar =  MutableStateFlow(false)

    fun changeScreen(screenType: ScreenType) {
        _uiState.update {
            it.copy(
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

}