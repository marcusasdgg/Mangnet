package com.example.poomagnet.App

import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.poomagnet.App.data.BottomList
import com.example.poomagnet.R
import com.example.poomagnet.ui.HomeScreen.HomeScreen
import com.example.poomagnet.ui.HomeScreen.HomeTopBarV2
import com.example.poomagnet.ui.HomeScreen.HomeViewModel
import com.example.poomagnet.ui.MangaExpanded.MangaScreen
import com.example.poomagnet.ui.SearchScreen.SearchScreen
import com.example.poomagnet.ui.SearchScreen.SearchTopBar
import com.example.poomagnet.ui.SearchScreen.SearchViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay


@Composable
fun App() {
    val viewModel: AppViewModel = viewModel()
    val uiState = viewModel.uiState.collectAsState().value
    val homeViewModel: HomeViewModel = viewModel()
    val homeUiState = homeViewModel.uiState.collectAsState().value
    val searchViewModel: SearchViewModel =  hiltViewModel()
    val mediaPlayer = MediaPlayer.create(LocalContext.current, R.raw.ambatu) // Replace 'your_sound_file' with the name of your MP3 file (without the extension)

    BackHandler {
        viewModel.hideBotBar(false)
        viewModel.changeToPrevious()
    }

    
    Scaffold(
        modifier = Modifier,
        bottomBar = {if (!uiState.botHidden) {
            BottomNavBar(
                modifier = Modifier,
                BottomList.infoList,
                currentTab = uiState.currentScreen,
                onButtonPressed = { item ->
                    if (uiState.currentScreen == ScreenType.Search && item == ScreenType.Search) {
                        mediaPlayer.start()
                        searchViewModel.revealBottomSheet(true)
                    }
                    viewModel.changeScreen(item)
                },
            )
        }},
        topBar = { if (!uiState.topHidden) {
            when (uiState.currentScreen) {
                ScreenType.Home -> HomeTopBarV2(homeViewModel::toggleDropDown, homeUiState,homeViewModel::changeDropDown)
                ScreenType.Search -> SearchTopBar(Modifier, searchViewModel)
                ScreenType.MangaSpecific -> {}
                else -> HomeTopBarV2(homeViewModel::toggleDropDown, homeUiState,homeViewModel::changeDropDown)
            }
        } }

    ) { innerPadding ->
        when (uiState.currentScreen) {
            ScreenType.Home -> HomeScreen(modifier = Modifier.padding(innerPadding))
            ScreenType.Search -> SearchScreen(modifier = Modifier.padding(innerPadding), searchViewModel = searchViewModel, viewModel::selectCurrentManga)
            ScreenType.Update -> Text("Update", Modifier.padding(innerPadding))
            ScreenType.Settings -> {
                HomeScreen(modifier = Modifier.padding(innerPadding))
            }
            ScreenType.MangaSpecific -> {
                viewModel.hideBotBar(true)
                MangaScreen(Modifier.padding(innerPadding),uiState.currentManga)
            }
        }
    }

}


//addcomment