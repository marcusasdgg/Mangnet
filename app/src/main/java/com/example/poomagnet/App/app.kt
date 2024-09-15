package com.example.poomagnet.App

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.poomagnet.App.data.BottomList
import com.example.poomagnet.ui.HomeScreen.HomeScreen
import com.example.poomagnet.ui.HomeScreen.HomeTopBarV2
import com.example.poomagnet.ui.HomeScreen.HomeViewModel
import com.example.poomagnet.ui.SearchScreen.SearchScreen
import com.example.poomagnet.ui.SearchScreen.SearchTopBar
import com.example.poomagnet.ui.SearchScreen.SearchViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val viewModel: AppViewModel = viewModel()
    val uiState = viewModel.uiState.collectAsState().value
    val homeViewModel: HomeViewModel = viewModel()
    val homeUiState = homeViewModel.uiState.collectAsState().value
    val searchViewModel: SearchViewModel =  hiltViewModel()

    
    Scaffold(
        modifier = Modifier,
        bottomBar = {if (!uiState.botHidden) {
            BottomNavBar(
                modifier = Modifier,
                BottomList.infoList,
                currentTab = uiState.currentScreen,
                onButtonPressed = { item ->
                    if (uiState.currentScreen == item) {
                        
                    }
                    viewModel.changeScreen(item)
                }
            )
        }},
        topBar = { if (!uiState.topHidden) {
            when (uiState.currentScreen) {
                ScreenType.Home -> HomeTopBarV2(homeViewModel::toggleDropDown, homeUiState,homeViewModel::changeDropDown)
                ScreenType.Search -> SearchTopBar(Modifier, searchViewModel)
                else -> HomeTopBarV2(homeViewModel::toggleDropDown, homeUiState,homeViewModel::changeDropDown)
            }
        } }

    ) { innerPadding ->
        when (uiState.currentScreen) {
            ScreenType.Home -> HomeScreen(modifier = Modifier.padding(innerPadding))
            ScreenType.Search -> SearchScreen(modifier = Modifier.padding(innerPadding), searchViewModel = searchViewModel)
            ScreenType.Update -> Text("Update", Modifier.padding(innerPadding))
            ScreenType.Settings -> {
                HomeScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }

}


//addcomment