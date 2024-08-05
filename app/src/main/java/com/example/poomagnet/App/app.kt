package com.example.poomagnet.App

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.poomagnet.App.data.BottomList
import com.example.poomagnet.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val viewModel: AppViewModel = viewModel()
    val uiState = viewModel.uiState.collectAsState().value

    Scaffold(
        modifier = Modifier,
        bottomBar = {if (!uiState.botHidden) {
            BottomNavBar(
                modifier = Modifier,
                BottomList.infoList,
                currentTab = uiState.currentScreen,
                onButtonPressed = { item ->
                    viewModel.changeScreen(item)
                }
            )
        }},

    ) { innerPadding ->
        when(uiState.currentScreen) {
            ScreenType.Home -> Text("Home", Modifier.padding(innerPadding))
            ScreenType.Search -> Text("Search", Modifier.padding(innerPadding))
            ScreenType.Update -> Text("Update", Modifier.padding(innerPadding))
            ScreenType.Settings -> Text("Settings", Modifier.padding(innerPadding))
        }
    }


}


