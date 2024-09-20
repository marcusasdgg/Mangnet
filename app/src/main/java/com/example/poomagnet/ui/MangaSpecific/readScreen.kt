package com.example.poomagnet.ui.MangaSpecific

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowInsets
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.poomagnet.mangaDex.dexApiService.ChapterContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


//use accompianist pager.

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReadingScreen(modifier: Modifier = Modifier, viewModel: MangaSpecificViewModel){
    var showBars by remember {mutableStateOf(false)}
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val s = uiState.currentChapter?.contents

    LaunchedEffect(Unit, uiState.currentChapter?.contents) {
        Log.d("TAG", "current chapter: ${uiState.currentChapter} ")
        if (s !== null){
            when(s){
                is ChapterContents.Online -> {
                    for (i in s.imagePaths){
                        preloadImage(context, i.first)
                    }
                } else -> {

                }
            }
        }
    }


    val configuration = LocalConfiguration.current
    //wrap this whole thing in a scaffold so we can have bottom and top bar.

    when(s){
        is ChapterContents.Online -> {
            val pagerState = rememberPagerState {s.imagePaths.size }
            LaunchedEffect(pagerState.currentPage) {
                viewModel.updateCurrentpage(pagerState.currentPage+1)
            }
            HorizontalPager(state = pagerState, modifier.fillMaxSize()) { page ->
                val imageUrl = s.imagePaths[page].first
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .clickable { viewModel.toggleReadBar() },

                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Image $page",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

        } else -> {

        }
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReadScreen(modifier: Modifier = Modifier, viewModel: MangaSpecificViewModel, returner: () -> Unit){
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current
    val window = (view.context as Activity).window
    val insetsController = WindowCompat.getInsetsController(window,view)

    if (!view.isInEditMode){
        LaunchedEffect(uiState.readBarVisible) {
            if (uiState.readBarVisible){
                insetsController.apply { show(WindowInsetsCompat.Type.systemBars()) }
            } else {
                insetsController.apply {
                    hide(WindowInsetsCompat.Type.systemBars())
                    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
        }
    }

    Scaffold(
        topBar = { MangaTopBar(Modifier, viewModel,returner)},
        bottomBar = { MangaBotBar(Modifier,viewModel)}
    ) { innerPadding ->
        Box(Modifier.fillMaxSize()){
            ReadingScreen(Modifier.fillMaxSize(), viewModel)
            Text("${uiState.currentPage}/${uiState.currentChapter?.pageCount?.toInt()}",
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(innerPadding))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaTopBar(modifier: Modifier = Modifier, viewModel: MangaSpecificViewModel, returner: () -> Unit){
    val uiState by viewModel.uiState.collectAsState()
    AnimatedVisibility(
        visible = uiState.readBarVisible,
        enter = fadeIn(animationSpec = tween(50)),
        exit = fadeOut(animationSpec = tween(50))
    ) {
        TopAppBar(
            title = {Text("Vol.${uiState.currentChapter?.volume} Ch. ${uiState.currentChapter?.chapter} ${uiState.currentChapter?.name}")},
            navigationIcon = {
                IconButton(onClick = returner) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = null
                    )
                }
            },
        )
    }
}

@Composable
fun MangaBotBar(modifier: Modifier = Modifier, viewModel: MangaSpecificViewModel){
    val uiState by viewModel.uiState.collectAsState()
    AnimatedVisibility(
        visible = uiState.readBarVisible,
        enter = fadeIn(animationSpec = tween(50)),
        exit = fadeOut(animationSpec = tween(50))
    ) {
        BottomAppBar(actions = {
            IconButton(onClick = {}) {
                Icon(
                    Icons.AutoMirrored.Default.ArrowBackIos,
                    contentDescription = null
                )
            }
        }, floatingActionButton = {
            IconButton(onClick = {}) {
                Icon(
                    Icons.AutoMirrored.Default.ArrowForwardIos,
                    contentDescription = null
                )
            }
        }, modifier = Modifier.height(110.dp))
    }
}

suspend fun preloadImage(context: Context, imageUrl: String) {
    val imageLoader = ImageLoader(context)
    val request = ImageRequest.Builder(context)
        .data(imageUrl)
        .allowHardware(false) // If you need to avoid hardware bitmaps (optional)
        .build()

    withContext(Dispatchers.IO) {
        // Execute the request to preload the image
        val result = imageLoader.execute(request)
        if (result !is SuccessResult) {
            Log.d("TAG", "preloadImage: failed")
        }
    }
}