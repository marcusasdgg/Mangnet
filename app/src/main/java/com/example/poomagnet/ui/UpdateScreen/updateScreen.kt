package com.example.poomagnet.ui.UpdateScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.poomagnet.mangaRepositoryManager.SlimChapter
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
        viewModel.syncLibrary()
    }
    AnimatedVisibility(
        visible =visible,
        enter = fadeIn(animationSpec = tween(80)),
        exit = fadeOut(animationSpec = tween(80)),
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 15.dp)) {
            items(uiState.showList.toList()){ item ->
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                    Text(item.first, fontSize = 16.sp)
                    item.second.forEach { elm ->
                        ChapterListing(Modifier, elm, onChapterClick, {f, g -> viewModel.loadImageFromLibrary(f,g)})
                    }
                }
            }
        }
    }
}

@Composable
fun ChapterListing(modifier: Modifier = Modifier, chapter: SlimChapter, onChapterClick: (String, String) -> Unit, loadImage: suspend (String, String) -> String){
    val context = LocalContext.current
    var uri by remember { mutableStateOf("")}
    LaunchedEffect(Unit){
        uri = loadImage(chapter.mangaId,chapter.id)
    }
    Box(
        Modifier
            .fillMaxWidth().padding(horizontal = 6.dp)
            .height(60.dp)
            .clickable { onChapterClick(chapter.mangaId, chapter.id) }){
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically){
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
                Text(chapter.mangaName, fontSize = 14.sp, modifier = Modifier, textAlign = TextAlign.Start)
                Spacer(Modifier.height(5.dp))
                Text("Vol. ${chapter.volume} Ch. ${chapter.chapter} - ${chapter.name}", fontSize = 14.sp, modifier = Modifier.padding(10.dp,0.dp,0.dp,0.dp), textAlign = TextAlign.Start)
            }
        }
    }
}


