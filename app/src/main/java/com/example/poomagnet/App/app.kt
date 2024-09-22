package com.example.poomagnet.App

import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import com.example.poomagnet.ui.MangaSpecific.MangaSpecificViewModel
import com.example.poomagnet.ui.SearchScreen.SearchScreen
import com.example.poomagnet.ui.SearchScreen.SearchTopBar
import com.example.poomagnet.ui.SearchScreen.SearchViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun App() {
    val viewModel: AppViewModel = viewModel()
    val uiState = viewModel.uiState.collectAsState().value
    val homeViewModel: HomeViewModel = hiltViewModel()
    val homeUiState = homeViewModel.uiState.collectAsState().value
    val searchViewModel: SearchViewModel =  hiltViewModel()
    val mangaViewModel: MangaSpecificViewModel = hiltViewModel()


    val simpleBack: () -> Unit = { viewModel.viewModelScope.launch {
        mangaViewModel.makeVisible(false)
        mangaViewModel.updateLibraryEquivalent()
        delay(80)
        viewModel.hideBotBar(false)
        viewModel.changeToPrevious()
        viewModel.hideTopBar(false)
        Log.d("TAG", "App: end refresh")
    } }

    BackHandler {
        simpleBack();
    }

    val currentScrollStateSearch = rememberLazyGridState()

    
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
                ScreenType.Home -> HomeTopBarV2(homeViewModel::toggleDropDown, homeUiState,homeViewModel::changeDropDown, uiState.currentScreen)
                ScreenType.Search -> SearchTopBar(Modifier.fillMaxWidth(), searchViewModel)
                ScreenType.MangaSpecific -> MangaAppBar(Modifier.fillMaxWidth(), simpleBack, mangaViewModel)
                else -> HomeTopBarV2(homeViewModel::toggleDropDown, homeUiState,homeViewModel::changeDropDown, uiState.currentScreen)
            }
        } }

    ) { innerPadding ->
        when (uiState.currentScreen) {
            ScreenType.Home -> HomeScreen(modifier = Modifier.padding(innerPadding), {},homeViewModel, setCurrentManga =  { elm ->
                viewModel.changeScreen(ScreenType.MangaSpecific)
                mangaViewModel.selectCurrentManga(elm)
            }, readChapter = { chapId, manga ->
                mangaViewModel.selectCurrentManga(manga)
                mangaViewModel.viewModelScope.launch {
                    mangaViewModel.getChapterUrls(chapId)
                    mangaViewModel.enterReadMode(true)
                    viewModel.hideTopBar(true)
                    viewModel.hideBotBar(true)
                    mangaViewModel.setFlag(true)
                    viewModel.changeScreen(ScreenType.MangaSpecific)
                }
            }, currentScreen = uiState.currentScreen,
                openLast = {manga ->
                    mangaViewModel.viewModelScope.launch {
                        mangaViewModel.selectCurrentManga(manga)
                        if (mangaViewModel.uiState.value.currentManga !== null){
                            var id =  mangaViewModel.uiState.value.currentManga!!.chapterList?.second?.last()!!.id
                            if (mangaViewModel.uiState.value.currentManga?.lastReadChapter?.first != "" && mangaViewModel.uiState.value.currentManga?.lastReadChapter?.first !== null){
                                id = mangaViewModel.uiState.value.currentManga?.lastReadChapter?.first!!
                            }
                            mangaViewModel.getChapterUrls(id)
                        }
                        mangaViewModel.enterReadMode(true)
                        viewModel.hideTopBar(true)
                        viewModel.hideBotBar(true)
                        mangaViewModel.setFlag(true)
                        viewModel.changeScreen(ScreenType.MangaSpecific)
                    }
                })
            ScreenType.Search -> SearchScreen(modifier = Modifier.padding(innerPadding), searchViewModel = searchViewModel, setCurrentManga =  { elm ->
                viewModel.changeScreen(ScreenType.MangaSpecific)
                mangaViewModel.selectCurrentManga(elm)
            }, currentScrollStateSearch)
            ScreenType.Update -> Text("Update", Modifier.padding(innerPadding))
            ScreenType.Settings -> {
                HomeScreen(modifier = Modifier.padding(innerPadding),{}, homeViewModel, {}, {a, b ->}, ScreenType.Settings, {})
            }
            ScreenType.MangaSpecific -> {
                viewModel.hideBotBar(true)
                MangaScreen(Modifier.padding(innerPadding),mangaViewModel, searchViewModel::addManga, viewModel::hideTopBar)
            }
        }
    }

}

//add a dot on the right side of the mangacard instead of the current icon to indicate when something has been updated with new stuff.
//addcomment