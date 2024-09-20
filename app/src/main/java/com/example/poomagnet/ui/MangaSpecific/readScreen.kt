package com.example.poomagnet.ui.MangaSpecific

import android.content.Context
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.poomagnet.mangaDex.dexApiService.Chapter
import com.example.poomagnet.mangaDex.dexApiService.ChapterContents
import com.example.poomagnet.mangaDex.dexApiService.isDownloaded
import com.example.poomagnet.mangaDex.dexApiService.isOnline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReadScreen(modifier: Modifier = Modifier, viewModel: MangaSpecificViewModel, returner: () -> Unit){
    val listState = rememberLazyListState()
    var showBars by remember {mutableStateOf(false)}
    val context = LocalContext.current


    val state = rememberSnapFlingBehavior(listState)
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

    LazyRow(modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, flingBehavior = state, state = listState) {
        if (s !== null){
            when(s){
               is ChapterContents.Downloaded -> {
                    items(s.imagePaths){ item ->
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color.Black)
                                .clickable { showBars = !showBars }, contentAlignment = Alignment.Center){
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(item.first)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
               }
                is ChapterContents.Online -> {
                    items(s.imagePaths){ item ->
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color.Black)
                                .clickable { showBars = !showBars }, contentAlignment = Alignment.Center){
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(item.first)
                                    .crossfade(true) // Enables crossfade animation for smooth loading
                                    .build(),
                                contentDescription = "Centered Image",
                                contentScale = ContentScale.Fit, // Stretch the image diagonally, keeping aspect ratio // Make the image fill the screen
                                modifier = Modifier.width(configuration.screenWidthDp.dp)
                            )
                        }
                    }
                }
            }
        } else {
            Log.d("TAG", "ReadScreen: aint working properly?")
        }

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