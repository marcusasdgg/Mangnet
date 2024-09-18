package com.example.poomagnet.App

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.poomagnet.App.data.BottomList
import com.example.poomagnet.ui.HomeScreen.HomeScreen
import com.example.poomagnet.ui.HomeScreen.HomeTopBarV2
import com.example.poomagnet.ui.HomeScreen.HomeViewModel
import com.example.poomagnet.ui.MangaSpecific.MangaAppBar
import com.example.poomagnet.ui.MangaSpecific.MangaScreen
import com.example.poomagnet.ui.MangaSpecific.mangaSpecificViewModel
import com.example.poomagnet.ui.SearchScreen.SearchScreen
import com.example.poomagnet.ui.SearchScreen.SearchTopBar
import com.example.poomagnet.ui.SearchScreen.SearchViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun App() {
    val viewModel: AppViewModel = viewModel()
    val uiState = viewModel.uiState.collectAsState().value
    val homeViewModel: HomeViewModel = viewModel()
    val homeUiState = homeViewModel.uiState.collectAsState().value
    val searchViewModel: SearchViewModel =  hiltViewModel()
    val mangaViewModel: mangaSpecificViewModel = hiltViewModel()



    val simpleBack: () -> Unit = { viewModel.viewModelScope.launch {
        mangaViewModel.makeVisible(false)
        delay(80)
        viewModel.hideBotBar(false)
        viewModel.changeToPrevious()
        Log.d("TAG", "App: end refresh")
    } }

    BackHandler {
        simpleBack();
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
                        searchViewModel.revealBottomSheet(true)
                    }
                    viewModel.changeScreen(item)
                },
            )
        }},
        topBar = { if (!uiState.topHidden) {
            when (uiState.currentScreen) {
                ScreenType.Home -> HomeTopBarV2(homeViewModel::toggleDropDown, homeUiState,homeViewModel::changeDropDown)
                ScreenType.Search -> SearchTopBar(Modifier.fillMaxWidth(), searchViewModel)
                ScreenType.MangaSpecific -> MangaAppBar(Modifier.fillMaxWidth(), simpleBack, mangaViewModel)
                else -> HomeTopBarV2(homeViewModel::toggleDropDown, homeUiState,homeViewModel::changeDropDown)
            }
        } }

    ) { innerPadding ->
        when (uiState.currentScreen) {
            ScreenType.Home -> HomeScreen(modifier = Modifier.padding(innerPadding))
            ScreenType.Search -> SearchScreen(modifier = Modifier.padding(innerPadding), searchViewModel = searchViewModel, setCurrentManga =  { elm ->
                viewModel.changeScreen(ScreenType.MangaSpecific)
                mangaViewModel.selectCurrentManga(elm)
            })
            ScreenType.Update -> Text("Update", Modifier.padding(innerPadding))
            ScreenType.Settings -> {
                HomeScreen(modifier = Modifier.padding(innerPadding))
            }
            ScreenType.MangaSpecific -> {
                viewModel.hideBotBar(true)
                MangaScreen(Modifier.padding(innerPadding),mangaViewModel)
            }
        }
    }

}


//addcomment