package com.example.poomagnet.ui.SortDrawer

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.example.poomagnet.ui.HomeScreen.HomeViewModel
import com.example.poomagnet.ui.SearchScreen.SearchUiState
import com.example.poomagnet.ui.SearchScreen.SearchViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun sortDrawer(modifier: Modifier = Modifier, viewModel: SearchViewModel){
    val uistate = viewModel.uiState.collectAsState().value

    if (uistate.showDrawer){
        ModalBottomSheet(onDismissRequest = {viewModel.revealBottomSheet(false)}) {

        }
    }
}


@Composable
fun sortDrawer(modifier: Modifier = Modifier, viewModel: HomeViewModel){

}