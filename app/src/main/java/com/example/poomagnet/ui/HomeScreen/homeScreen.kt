package com.example.poomagnet.ui.HomeScreen

import android.graphics.ColorSpace.Rgb
import android.graphics.Paint.Align
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poomagnet.App.ScreenType
import com.example.poomagnet.R
import com.example.poomagnet.mangaDex.dexApiService.MangaInfo
import com.example.poomagnet.ui.VerticalCard
import java.util.logging.Filter

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen( modifier: Modifier = Modifier, hideBottomBar: () -> Unit = {}, viewModel: HomeViewModel, setCurrentManga: (MangaInfo) -> Unit, readChapter: (String, MangaInfo) -> Unit, currentScreen: ScreenType, openLast: (MangaInfo)-> Unit, snackBar: SnackbarHostState, onUpdate: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

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


    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.ifLoading,
        onRefresh = {viewModel.loadIt(true)},
        refreshThreshold = 160.dp,
        refreshingOffset = 180.dp
    )

    Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
        LazyColumn(modifier = modifier.fillMaxWidth().fillMaxHeight()) {
            items(uiState.library) { manga ->
                VerticalCard(manga = manga, modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp, 0.dp), onclick = { setCurrentManga(manga) }, engageChapter = { id ->
                    readChapter(id,manga)
                }, openLast = {
                    openLast(manga)
                }
                )
                Spacer(Modifier.fillMaxWidth().height(10.dp))
            }
        }
        PullRefreshIndicator(
            refreshing = uiState.ifLoading,
            state = pullRefreshState,
            Modifier.align(Alignment.TopCenter)
        )
    }

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




