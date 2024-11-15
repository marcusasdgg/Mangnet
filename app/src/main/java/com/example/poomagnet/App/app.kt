package com.example.poomagnet.App

import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
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
import com.example.poomagnet.ui.SettingsScreen.SettingsScreen
import com.example.poomagnet.ui.SettingsScreen.SettingsTopBar
import com.example.poomagnet.ui.SettingsScreen.SettingsViewModel
import com.example.poomagnet.ui.UpdateScreen.UpdateScreen
import com.example.poomagnet.ui.UpdateScreen.UpdateTopBar
import com.example.poomagnet.ui.UpdateScreen.updateViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun App(activityResultLauncher: ActivityResultLauncher<String>) {

    val viewModel: AppViewModel = viewModel()
    val uiState = viewModel.uiState.collectAsState().value
    val homeViewModel: HomeViewModel = hiltViewModel()
    val homeUiState = homeViewModel.uiState.collectAsState().value
    val searchViewModel: SearchViewModel =  hiltViewModel()
    val mangaViewModel: MangaSpecificViewModel = hiltViewModel()
    val updateViewModel: updateViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()



    val simpleBack: () -> Unit = { viewModel.viewModelScope.launch {
        mangaViewModel.makeVisible(false)
        mangaViewModel.updateLibraryEquivalent()
        searchViewModel.updateOldSearchText()
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
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {if (!uiState.botHidden) {
            BottomNavBar(
                modifier = Modifier,
                BottomList.infoList,
                currentTab = uiState.currentScreen,
                onButtonPressed = { item ->
                    if (uiState.currentScreen == ScreenType.Search && item == ScreenType.Search) {
                        searchViewModel.revealBottomSheet(true)
                    }
                    if (uiState.currentScreen == ScreenType.Home && item == ScreenType.Home) {
                        homeViewModel.revealBottomSheet(true)
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
                ScreenType.Update -> UpdateTopBar()
                ScreenType.Settings -> SettingsTopBar()
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
                            var id =  mangaViewModel.uiState.value.currentManga!!.chapterList?.last()!!.id
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
                }, snackbarHostState, updateViewModel::syncLibrary)
            ScreenType.Search -> SearchScreen(modifier = Modifier.padding(innerPadding), searchViewModel = searchViewModel, setCurrentManga =  { elm ->
                viewModel.changeScreen(ScreenType.MangaSpecific)
                mangaViewModel.selectCurrentManga(elm)
            }, currentScrollStateSearch)
            ScreenType.Update -> UpdateScreen(Modifier.padding(innerPadding), updateViewModel, onChapterClick = { id, chapId ->
                val manga = updateViewModel.findMangaInLibrary(id)
                if (manga !== null){
                    mangaViewModel.selectCurrentManga(manga)
                    mangaViewModel.viewModelScope.launch {
                        mangaViewModel.getChapterUrls(chapId)
                        mangaViewModel.enterReadMode(true)
                        viewModel.hideTopBar(true)
                        viewModel.hideBotBar(true)
                        mangaViewModel.setFlag(true)
                        viewModel.changeScreen(ScreenType.MangaSpecific)
                    }
                } else {
                    Log.d("TAG", "App: Id was invalid?")
                }
            })
            ScreenType.Settings -> {
               SettingsScreen(Modifier.padding(innerPadding), settingsViewModel, activityResultLauncher)
            }
            ScreenType.MangaSpecific -> {
                viewModel.hideBotBar(true)
                MangaScreen(Modifier.padding(innerPadding),mangaViewModel,
                    { searchViewModel.addManga()
                        updateViewModel.syncLibrary()
                    }, viewModel::hideTopBar)
            }
        }
    }

}

//add a dot on the right side of the mangacard instead of the current icon to indicate when something has been updated with new stuff.
//addcomment