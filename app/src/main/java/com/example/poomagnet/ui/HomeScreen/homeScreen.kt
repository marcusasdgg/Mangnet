package com.example.poomagnet.ui.HomeScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poomagnet.App.ScreenType
import com.example.poomagnet.mangaRepositoryManager.MangaInfo
import com.example.poomagnet.ui.DoubleStackCard
import com.example.poomagnet.ui.SortDrawer.HomeSortDrawer
import com.example.poomagnet.ui.VerticalCard

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier, hideBottomBar: () -> Unit = {}, viewModel: HomeViewModel, setCurrentManga: (MangaInfo) -> Unit, readChapter: (String, MangaInfo) -> Unit, currentScreen: ScreenType, openLast: (MangaInfo)-> Unit, snackBar: SnackbarHostState, onUpdate: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.syncLibrary()
    }
    LaunchedEffect(uiState.ifLoading) {
        if (uiState.ifLoading){
            viewModel.updateAll()
            snackBar.showSnackbar("Updating Library")
            viewModel.loadIt(false)
        }
    }

    LaunchedEffect(uiState.somethingChanged) {
        if (uiState.somethingChanged){
            viewModel.recompose()
            viewModel.resetFlag()
        }
    }


    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.ifLoading,
        onRefresh = {viewModel.loadIt(true)},
        refreshThreshold = 160.dp,
        refreshingOffset = 180.dp
    )

    Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
        when (uiState.typeView){
            displayType.VERTICALCARD -> {
                Column(modifier = modifier.fillMaxWidth().fillMaxHeight().verticalScroll(scrollState)) {
                    uiState.library.forEach { manga ->
                        VerticalCard(manga = manga, modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp, 0.dp), onclick = { setCurrentManga(manga) }, engageChapter = { id ->
                            readChapter(id,manga)
                        }, openLast = {
                            openLast(manga)
                        },
                            loadImage = {elm1, elm2 -> viewModel.loadImageFromLibrary(elm1,elm2)},
                            somethingChanged = uiState.somethingChanged
                        )
                        Spacer(Modifier.fillMaxWidth().height(10.dp))
                    }
                }
            }
            displayType.SINGLESCREEN -> TODO()
            displayType.LISTSCROLL -> TODO()
            displayType.TWOGRID -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), // 2 items per row
                    modifier = modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(uiState.library) { manga ->
                        DoubleStackCard(
                            manga = manga,
                            click = setCurrentManga,
                            loadImage = {elm1, elm2 -> viewModel.loadImageFromLibrary(elm1,elm2) }
                        )
                    }
                }
            }
        }

        PullRefreshIndicator(
            refreshing = uiState.ifLoading,
            state = pullRefreshState,
            Modifier.align(Alignment.TopCenter)
        )
    }

    HomeSortDrawer(Modifier,viewModel)
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBarV2(toggleDrop: (Boolean) -> Unit, uiState: HomeUiState, changeDrop: (FilterOptions) -> Unit, currentScreen: ScreenType){
    TopAppBar(title = {
        Button(onClick = {toggleDrop(true)}, colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = Color.White
        )) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
                Text(uiState.currentMenuOption.toString(), fontSize = 28.sp)
                Icon(
                    imageVector =  Icons.Default.ExpandMore,
                    contentDescription = "Chevron Icon",
                    modifier = Modifier.offset(y = 2.dp)
                )
            }
        }
        DropdownMenu(expanded = uiState.expandedMenu, onDismissRequest = {toggleDrop(false)}) {
            DropdownMenuItem(
                text = { Text("All") },
                onClick = {
                    changeDrop(FilterOptions.All)
                    toggleDrop(false)
                }
            )
            DropdownMenuItem(
                text = { Text("Comedy") },
                onClick = {
                    changeDrop(FilterOptions.Comedy)
                    toggleDrop(false)
                }
            )
            DropdownMenuItem(
                text = { Text("Romance") },
                onClick = {
                    changeDrop(FilterOptions.Romance)
                    toggleDrop(false)
                }
            )
        }
    })
}




