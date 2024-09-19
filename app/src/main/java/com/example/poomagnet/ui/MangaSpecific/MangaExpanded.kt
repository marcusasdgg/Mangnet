package com.example.poomagnet.ui.MangaSpecific

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.poomagnet.R
import com.example.poomagnet.mangaDex.dexApiService.MangaInfo
import kotlinx.coroutines.delay

@Composable
fun MangaScreen(modifier: Modifier = Modifier, mangaViewModel: MangaSpecificViewModel) {
    val uiState by mangaViewModel.uiState.collectAsState()

    AnimatedVisibility(
        visible = uiState.visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 80)),
        exit = fadeOut(animationSpec = tween(durationMillis = 80)),
        modifier = modifier.fillMaxSize()
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.height(250.dp)) {
                Spacer(Modifier.width(15.dp))
                Column(
                    Modifier
                        .weight(1.8f)
                        .fillMaxHeight()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(uiState.currentManga?.coverArtUrl)
                            .crossfade(true)
                            .build(),
                        placeholder = painterResource(R.drawable.prevthumbnail),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(15.dp))
                    )
                }
                Spacer(Modifier.width(15.dp))
                Column(
                    Modifier
                        .weight(2f)
                        .fillMaxHeight()){
                    Text(uiState.currentManga?.type ?: "null", Modifier.weight(1f))
                    Text(uiState.currentManga?.state.toString() ?: "null", Modifier.weight(1f))
                    AddToButton(Modifier.weight(2f).padding(20.dp,20.dp), {mangaViewModel.addToLibrary()}, uiState.currentManga?.inLibrary ?: false)
                }
            }
            Spacer(Modifier.height(10.dp))
            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .height(40.dp)) {
                items(uiState.currentManga?.tagList ?: listOf()){ item ->
                    Spacer(Modifier.width(5.dp))
                    tagIt(Modifier.fillMaxHeight(),item)
                }
            }
            Column(Modifier.weight(5f)){

            }
        }
    }

}

@Composable
fun AddToButton(modifier: Modifier = Modifier, onclick: () -> Unit, selected: Boolean){
    Box(modifier.clip(RoundedCornerShape(10)) // Clip first to apply rounded corners
        .background(MaterialTheme.colorScheme.background) // Background after clipping
        .clickable { onclick() } ){
        Column(Modifier.fillMaxSize()) {
                Icon(imageVector = if (selected) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, "", Modifier.weight(1f).align(Alignment.CenterHorizontally), tint = if (!selected) Color.Gray else MaterialTheme.colorScheme.onPrimaryContainer)
            Text("Add${if (selected) "ed" else ""} to Library", Modifier.weight(1f).align(Alignment.CenterHorizontally), color = if (!selected) Color.Gray else MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Preview
@Composable
fun tagIt(modifier: Modifier = Modifier, name: String = "Preview"){
    Button({ }, shape = RoundedCornerShape(100), colors = ButtonDefaults.outlinedButtonColors(
        containerColor = MaterialTheme.colorScheme.inversePrimary // Follows your theme's text color
    ),) {
        Text(name, color = Color(240, 234, 214))
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaAppBar(modifier: Modifier = Modifier, onBack: () -> Unit, mangaViewModel: MangaSpecificViewModel){
    val state by mangaViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        Log.d("TAG", "MangaAppBar: launchedEffect ${state.visible}")
        delay(80)
        mangaViewModel.makeVisible(true)
    }

    AnimatedVisibility(
        visible = state.visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = fadeOut(animationSpec = tween(durationMillis = 300)),
    ) {
            TopAppBar(
                modifier = modifier.fillMaxWidth(),
                title = {
                    Text(
                        state.currentManga?.title ?: "null",
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null
                        )
                    }
                }
            )
    }

}