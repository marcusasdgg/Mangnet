package com.example.poomagnet.ui.UpdateScreen

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.poomagnet.mangaDex.dexApiService.slimChapter
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateTopBar(modifier: Modifier = Modifier){
    TopAppBar(modifier = Modifier, title = { Text("Updates")})
}

@Composable
fun UpdateScreen(modifier: Modifier = Modifier, viewModel: updateViewModel, onChapterClick: (String, String) -> Unit){
    val uiState by viewModel.uiState.collectAsState()
    var visible by remember{mutableStateOf(false)}
    LaunchedEffect(Unit) {
        delay(90)
        visible = true
    }
    AnimatedVisibility(
        visible =visible,
        enter = fadeIn(animationSpec = tween(80)),
        exit = fadeOut(animationSpec = tween(80)),
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(Modifier.fillMaxSize()) {
            items(uiState.showList.toList()){ item ->
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                    Text(item.first, fontSize = 16.sp)
                    item.second.forEach { elm ->
                        ChapterListing(Modifier, elm, onChapterClick)
                    }
                }
            }
        }
    }
}

@Composable
fun ChapterListing(modifier: Modifier = Modifier, chapter: slimChapter, onChapterClick: (String, String) -> Unit){
    val context = LocalContext.current
    Box(
        Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onChapterClick(chapter.mangaId, chapter.id) }){
        Row(Modifier.fillMaxSize().border(1.dp, Color.Black), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically){
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(chapter.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxHeight() // Set the width as a fraction of the parent width
                    .aspectRatio(1f)
                    .padding(15.dp,8.dp)
            )
            Column(Modifier.fillMaxWidth(0.8f), horizontalAlignment = Alignment.Start) {
                Text(chapter.name, fontSize = 14.sp, modifier = Modifier, textAlign = TextAlign.Start)
                Spacer(Modifier.height(5.dp))
                Text("Vol. ${chapter.volume} Ch. ${chapter.chapter} - ${chapter.name}", fontSize = 14.sp, modifier = Modifier.padding(10.dp,0.dp,0.dp,0.dp), textAlign = TextAlign.Start)
            }
        }
    }
}

@Preview
@Composable
fun UpdateScreen(){
    val listing = Pair("13/10/2004", listOf(slimChapter("not an Id", "Naruto In Buss", 3.1,3.0,"not a mangaId", "data:image/png;base64,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","Naruto" )))
    val showList: Map<String, List<slimChapter>> =  mapOf(listing, listing.copy(first = "13/9/2004"))
    LazyColumn(Modifier.fillMaxSize()) {
        items(showList.toList()){ item ->
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text(item.first, fontSize = 16.sp)
                item.second.forEach { elm ->
                    Log.d("TAG", "UpdateScreen: $elm")
                    ChapterListing(Modifier, elm) { f, g ->}
                }
            }
        }
    }
}
