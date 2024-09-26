package com.example.poomagnet.ui.MangaSpecific

import android.Manifest
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.Worker
import androidx.work.WorkerParameters
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.poomagnet.R
import com.example.poomagnet.mangaDex.dexApiService.MangaInfo
import com.example.poomagnet.mangaDex.dexApiService.SimpleDate
import com.example.poomagnet.mangaDex.dexApiService.isDownloaded
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MangaScreen(modifier: Modifier = Modifier, mangaViewModel: MangaSpecificViewModel, onAdd: () -> Unit, hideTopBar: (Boolean) -> Unit) {
    val uiState by mangaViewModel.uiState.collectAsState()
    val scrollstate = rememberScrollState()




    LaunchedEffect(Unit) {
        mangaViewModel.getChapterInfo()
    }

    AnimatedVisibility(
        visible = uiState.visible && !uiState.inReadMode,
        enter = fadeIn(animationSpec = tween(durationMillis = 80)),
        exit = fadeOut(animationSpec = tween(durationMillis = 80)),
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollstate)) {
            Row(Modifier.height(250.dp)) {
                Spacer(Modifier.width(15.dp))
                Column(
                    Modifier
                        .weight(1.8f)
                        .fillMaxHeight()) {
                    if (uiState.currentManga?.inLibrary == true && !uiState.currentManga!!.coverArtUrl.startsWith("https://")){
                        Log.d("TAG", "MangaScreen: in library mode")
                        var image by remember { mutableStateOf("")}
                        LaunchedEffect(uiState.currentManga!!.inLibrary) {
                            image = mangaViewModel.loadImageFromLibrary(uiState.currentManga!!.id, uiState.currentManga!!.coverArtUrl)
                            Log.d("TAG", "MangaScreen: new uri is $image")
                        }
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(image)
                                .crossfade(true)
                                .build(),
                            placeholder = painterResource(R.drawable.prevthumbnail),
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(15.dp))
                        )
                    }else {
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
                }
                Spacer(Modifier.width(15.dp))
                Column(
                    Modifier
                        .weight(2f)
                        .fillMaxHeight()){
                    Text(uiState.currentManga?.type ?: "null", Modifier.weight(1f))
                    Text(uiState.currentManga?.state.toString() ?: "null", Modifier.weight(1f))
                    AddToButton(
                        Modifier
                            .weight(2f)
                            .padding(20.dp, 20.dp), {mangaViewModel.addToLibrary(); onAdd()}, uiState.currentManga?.inLibrary ?: false)
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
            Spacer(Modifier.height(20.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(40.dp), contentAlignment = Alignment.CenterStart){
                Text("${uiState.currentManga?.chapterList?.second?.size} Chapters", Modifier.padding(10.dp,0.dp,0.dp,0.dp))
                    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            shape = RoundedCornerShape(100),
                            onClick = {
                                mangaViewModel.viewModelScope.launch {
                                    var currentManga = uiState.currentManga!!.chapterList?.second?.lastOrNull()?.id
                                    if (uiState.currentManga?.lastReadChapter?.first != ""  && uiState.currentManga?.lastReadChapter?.first !== null){
                                        currentManga = uiState.currentManga?.lastReadChapter?.first
                                    }
                                    if (currentManga !== null){
                                        Log.d("TAG", "MangaScreen: found chapteriD $currentManga")
                                        mangaViewModel.getChapterUrls(currentManga)
                                        mangaViewModel.enterReadMode(true)
                                        hideTopBar(true)
                                        mangaViewModel.setFlag(true)
                                    } else {
                                        Log.d("TAG", "MangaScreen: wtf result is null")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(10.dp, 2.dp)
                                .width(105.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.inversePrimary
                            )
                        ) {
                            Text(
                                text = if (uiState.currentManga!!.lastReadChapter.first == ""
                                ) "Start" else "Continue"
                                ,
                                fontSize = 14.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
            }
            Spacer(Modifier.height(20.dp))
            Column(){
                uiState.currentManga?.chapterList?.second?.forEach { elm ->
                    ChapterListing(Modifier.height(60.dp),
                        {mangaViewModel.viewModelScope.launch {
                            mangaViewModel.getChapterUrls(elm.id)
                            mangaViewModel.enterReadMode(true)
                            hideTopBar(true)
                            mangaViewModel.setFlag(true)
                        }
                        }
                        ,elm.chapter,elm.volume, elm.name,elm.date, elm.finished, elm.contents?.isDownloaded?:false, onDownload = {mangaViewModel.viewModelScope.launch {
                            mangaViewModel.downloadChapter(chapterId = elm.id)
                        }})
                }
            }
        }
    }

    AnimatedVisibility(
        visible = uiState.inReadMode,
        modifier = Modifier.fillMaxSize(),
        enter = fadeIn(animationSpec = tween(durationMillis = 80)),
        exit = fadeOut(animationSpec = tween(durationMillis = 80)),
    ) {
        val returner: () -> Unit = {
            mangaViewModel.viewModelScope.launch {
                delay(80)
                hideTopBar(false)
                mangaViewModel.enterReadMode(false)
                mangaViewModel.resetState()
                Log.d("TAG", "MangaScreen: ${mangaViewModel.uiState.value.currentManga?.chapterList?.second?.firstOrNull { elm -> elm.finished }}")
            }
        }

        if (uiState.inReadMode){
            ReadScreen(Modifier,mangaViewModel,{returner() })
        }


    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AddToButton(modifier: Modifier = Modifier, onclick: () -> Unit, selected: Boolean){

    Box(
        modifier
            .clip(RoundedCornerShape(10)) // Clip first to apply rounded corners
            .background(MaterialTheme.colorScheme.background) // Background after clipping
            .clickable { ; onclick() } ){
        Column(Modifier.fillMaxSize()) {
                Icon(imageVector = if (selected) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, "",
                    Modifier
                        .weight(1f)
                        .align(Alignment.CenterHorizontally), tint = if (!selected) Color.Gray else MaterialTheme.colorScheme.onPrimaryContainer)
            Text("Add${if (selected) "ed" else ""} to Library",
                Modifier
                    .weight(1f)
                    .align(Alignment.CenterHorizontally), color = if (!selected) Color.Gray else MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
fun ChapterListing(modifier: Modifier = Modifier, onclick: () -> Unit, chapter: Double, volume: Double, name: String, date: SimpleDate?, ifRead: Boolean, ifDownloaded: Boolean, onDownload: () -> Unit){
    val context = LocalContext.current
    Box(
        modifier
            .fillMaxWidth()
            .clickable { onclick() }){
        Text("${if (volume.toInt() == -1) "" else "Vol.${volume.toInt()}" } Ch. ${if (chapter == -1.0) "Undefined" else chapter.toString()} ${if (name !== "null") name else ""}",
            Modifier
                .align(Alignment.TopStart)
                .padding(20.dp, 5.dp, 0.dp, 0.dp)
                .fillMaxWidth(0.85f), overflow = TextOverflow.Ellipsis, maxLines = 1,
            color = if (ifRead) Color.Gray else Color.Unspecified,
        )
        IconButton(onClick = {
            Toast.makeText(context,"Downloading Chapter Come Back Later", Toast.LENGTH_LONG).show()
            onDownload()},
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(0.dp, 0.dp, 15.dp, 0.dp)
            ) {
            Icon(if(ifDownloaded) Icons.Filled.DownloadForOffline else Icons.Outlined.DownloadForOffline, "", tint = if (ifDownloaded) Color.White else Color.Gray)
        }

        Text(date.toString(),
            Modifier
                .align(Alignment.BottomStart)
                .padding(27.dp, 0.dp, 0.dp, 5.dp),
            color = if (ifRead) Color.Gray else Color.Unspecified,)
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



//maybe add a button that scrolls to bottom?
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaAppBar(modifier: Modifier = Modifier, onBack: () -> Unit, mangaViewModel: MangaSpecificViewModel){
    val state by mangaViewModel.uiState.collectAsState()
    var open by remember{ mutableStateOf(false)}

    LaunchedEffect(Unit) {
        Log.d("TAG", "MangaAppBar: launchedEffect ${state.visible}")
        delay(80)
        mangaViewModel.makeVisible(true)
    }

    AnimatedVisibility(
        visible = state.visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 80)),
        exit = fadeOut(animationSpec = tween(durationMillis = 80)),
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
                    Box{
                        IconButton(onClick = {open = true}) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null
                            )
                        }
                        DropdownMenu(
                            expanded = open,
                            onDismissRequest = { open = false } // Dismiss the menu when clicked outside
                        ) {
                            // Menu items, dynamically created based on the options list
                            DropdownMenuItem(text = {Text("first 5 chapters")}, onClick = {open = false; mangaViewModel.viewModelScope.launch {
                                val maxSize = state.currentManga?.chapterList?.second?.size ?: 0
                                val chapterList = state.currentManga?.chapterList?.second?.reversed()?.subList(0,min(maxSize,5))
                                chapterList?.forEach { elm->
                                    Log.d("TAG", "MangaAppBar: ${elm.id}")
                                    mangaViewModel.downloadChapter(chapterId = elm.id)
                                }
                                open = false
                            }})
                            DropdownMenuItem(text = {Text("first 10 chapters")}, onClick = {open = false; mangaViewModel.viewModelScope.launch {
                                val maxSize = state.currentManga?.chapterList?.second?.size ?: 0
                                val chapterList = state.currentManga?.chapterList?.second?.reversed()?.subList(
                                    0,
                                    min(maxSize, 10)
                                )
                                chapterList?.forEach { elm ->
                                    mangaViewModel.downloadChapter(chapterId = elm.id)
                                }
                            }
                            })
                            DropdownMenuItem(text = {Text("All chapters")}, onClick = {open = false ; mangaViewModel.viewModelScope.launch {
                                val maxSize = state.currentManga?.chapterList?.second?.size ?: 0
                                val chapterList = state.currentManga?.chapterList?.second?.reversed()?.subList(
                                    0,
                                    maxSize
                                )
                                chapterList?.forEach { elm ->
                                    mangaViewModel.downloadChapter(chapterId = elm.id)
                                }

                            }
                            })
                        }
                    }


                }
            )
    }

}

