 package com.example.poomagnet.ui.MangaSpecific

import android.app.Activity
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.poomagnet.mangaRepositoryManager.ChapterContents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext


 //use accompianist pager.
//create a list of functions first

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReadingScreen(modifier: Modifier = Modifier, viewModel: MangaSpecificViewModel, pagerState: PagerState, context: Context, list: MutableState<List<@Composable () -> Unit>>){

    val uiState by viewModel.uiState.collectAsState()
    val sstate = rememberScrollState();

    LaunchedEffect(pagerState, pagerState.pageCount) {

        snapshotFlow { pagerState.currentPageOffsetFraction}.collect { offset ->
            if (pagerState.currentPage == pagerState.pageCount-2 && offset > 0f) {
                Log.d("TAG", "ReadingScreen: is last page scroll")
                if (uiState.nextChapter !== null){
                    viewModel.loadNextChapter()
                    viewModel.setFlag(true)
                    delay(80)
                    pagerState.scrollToPage(0)
                    viewModel.setPage(1)
                    Log.d("TAG", "ReadingScreen: ${uiState.currentManga?.chapterList?.firstOrNull { elm -> elm.finished }}")
                }else {
                    viewModel.getNextChapter(context)
                    viewModel.loadNextChapter()
                    viewModel.setFlag(true)
                    delay(80)
                    pagerState.scrollToPage(0)
                    viewModel.setPage(1)
                    Log.d("TAG", "ReadingScreen: ${uiState.currentManga?.chapterList?.firstOrNull { elm -> elm.finished }}")
                }
            } else {
                if (pagerState.currentPage == pagerState.pageCount/2){
                    viewModel.getNextChapter(context)
                    Log.d("TAG", "ReadingScreen: getting next chapter")
                }
                if (pagerState.currentPage+1 == uiState.currentChapter?.pageCount?.toInt()){
                    viewModel.markThisAsDone()
                }
                Log.d("TAG", "ReadingScreen: changing to ${pagerState.pageCount}")
                sstate.scrollTo(0)
                viewModel.setPage(pagerState.currentPage+1)
            }
        }

    }


    HorizontalPager(pagerState,
        modifier
            .fillMaxSize()
            .verticalScroll(sstate)) { page ->
        list.value[page]()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageView(modifier: Modifier = Modifier, imageUrl: String, onClick: () -> Unit, context: Context, leftZone:  (CoroutineContext) -> Unit, rightZone:(CoroutineContext) -> Unit, ifDownloaded: Boolean, loadImage: suspend ()->String, refereUrl: String, pagerState: PagerState){
    val coroutineScope = rememberCoroutineScope()

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val screenWidth = size.width
                    when {
                        offset.x < screenWidth * 0.35f -> {
                            // Left zone
                            leftZone(coroutineScope.coroutineContext)
                        }

                        offset.x < screenWidth * 0.65f -> {
                            // Center zone
                            onClick()
                        }

                        else -> {
                            // Right zone
                            rightZone(coroutineScope.coroutineContext)
                        }
                    }
                }
            }
        , contentAlignment = Alignment.Center){



        if (ifDownloaded){
            Log.d("TAG", "ImageView: loading image")
            var image by remember { mutableStateOf("")}
            LaunchedEffect(Unit) {
                image = loadImage()
            }
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(image)
                    .crossfade(true)
                    .build(),
                contentDescription = "Image",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxSize()
            )
        }else {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .addHeader("referer", refereUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Image",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
//        Box(
//            Modifier
//                .align(Alignment.CenterStart)
//                .fillMaxHeight()
//                .fillMaxWidth(0.35f)
//                .background(Color.Red.copy(alpha = 0.3f)) // Add a semi-transparent background for visibility
//                .border(2.dp, Color.White) // Change border color to white for contrast
//                .clickable { leftZone() }
//                .zIndex(1f)
//        )
//
//        // Right zone with border
//        Box(
//            Modifier
//                .align(Alignment.CenterEnd)
//                .fillMaxHeight()
//                .fillMaxWidth(0.35f)
//                .background(Color.Green.copy(alpha = 0.3f)) // Semi-transparent green for visibility
//                .border(2.dp, Color.White)
//                .zIndex(1f)
//                .clickable { rightZone(coroutineScope.coroutineContext) }
//        )
//
//        // Center zone with border
//        Box(
//            Modifier
//                .align(Alignment.Center)
//                .fillMaxHeight()
//                .fillMaxWidth(0.3f)
//                .background(Color.Blue.copy(alpha = 0.3f)) // Semi-transparent blue
//                .border(2.dp, Color.White)
//                .zIndex(1f)
//                .clickable(
//                    onClick = { onClick() },
//                    indication = null,
//                    interactionSource = remember { MutableInteractionSource() }
//                )
//        )
    }

}

// Needed when disabling the indication
//)

@Composable
fun endScreen(modifier: Modifier = Modifier, currentChapter: String, nextChapter: String = "", onClick: () -> Unit, leftZone: (CoroutineContext) -> Unit, rightZone: (CoroutineContext) -> Unit){
    val co = rememberCoroutineScope()
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black), contentAlignment = Alignment.Center){

        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally){
            Text(currentChapter)
        }
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .fillMaxWidth(0.3f)
                .clickable { leftZone(co.coroutineContext) })
        Box(
            Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .fillMaxWidth(0.3f)
                .clickable { rightZone(co.coroutineContext) })
        Box(
            Modifier
                .align(Alignment.Center)
                .fillMaxHeight()
                .fillMaxWidth(0.4f)
                .clickable(
                    onClick = { onClick() },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() })
        )
    }

}


@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReadScreen(modifier: Modifier = Modifier, viewModel: MangaSpecificViewModel, returner: () -> Unit){
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current
    val window = (view.context as Activity).window
    val insetsController = WindowCompat.getInsetsController(window,view)

    LaunchedEffect(Unit) {
        viewModel.toggleHomeBar(false)
        viewModel.toggleReadBar(false)
    }

    LaunchedEffect(uiState.currentChapter) {
        Log.d("TAG", "ReadScreen: next chapter not found?")
        if (uiState.currentChapter == null){
            viewModel.toggleHomeBar(true)
            viewModel.toggleReadBar(true)
            returner()
        }
    }



    val list = remember { mutableStateOf(listOf<@Composable () -> Unit>()) }
    val pagerState = rememberPagerState {list.value.size}
    LaunchedEffect(uiState.nextFlag) {
        delay(80)
        Log.d("TAG", "ReadingScreen: flag hook triggered")
        Log.d("TAG", "ReadScreen: current chapter now is ${uiState.currentChapter}")
        if (uiState.nextFlag){
            val chapter = uiState.currentChapter
            if (chapter !== null){
                val contents = chapter.contents
                if (contents !== null){
                    Log.d("TAG", "ReadingScreen: loading pages")
                    when (contents){
                        is ChapterContents.Online -> {
                            val firstList: List<@Composable () -> Unit> = contents.imagePaths.map { elm ->
                                {
                                    LaunchedEffect(Unit) {
                                        viewModel.toggleHomeBar(false)
                                        viewModel.toggleReadBar(false)
                                    }
                                    ImageView(Modifier, imageUrl = elm, onClick = {viewModel.toggleReadBar() ; viewModel.toggleHomeBar()}, context, { its ->
                                    viewModel.viewModelScope.launch {
                                        if (pagerState.currentPage >= pagerState.pageCount - 2){
                                            if (uiState.nextChapter == null){
                                                viewModel.markThisAsDone()
                                                viewModel.getNextChapter(context = context)
                                                viewModel.loadNextChapter()
                                                pagerState.scrollToPage(0)
                                            } else {
                                                viewModel.markThisAsDone()
                                                viewModel.loadNextChapter()
                                                viewModel.setFlag(true)
                                                pagerState.scrollToPage(0)
                                            }
                                        } else {
                                            withContext(its){
                                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                                viewModel.setPage(pagerState.currentPage + 1)
                                            }
                                        }
                                    }
                                }, { its ->
                                    viewModel.viewModelScope.launch(Dispatchers.Main) {
                                        if (pagerState.currentPage == 0){
                                            if (uiState.previousChapter == null){
                                                viewModel.markThisAsDone()
                                                viewModel.getPreviousChapter(context)
                                                viewModel.loadPreviousChapter()
                                                pagerState.scrollToPage(0)
                                            } else {
                                                viewModel.markThisAsDone()
                                                viewModel.loadPreviousChapter()
                                                viewModel.setFlag(true)
                                                pagerState.scrollToPage(0)
                                            }
                                        } else {
                                            withContext(its) {
                                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                                viewModel.setPage(pagerState.currentPage - 1)
                                            }
                                        }
                                    }
                                }, ifDownloaded = false, loadImage = {""}, refereUrl = viewModel.getRefererUrl(), pagerState)}
                            }
                            if (firstList.size == 0){
                                //inject warning for now but who cares.
                                viewModel.toggleHomeBar(true)
                                viewModel.toggleReadBar(true)
                                Toast.makeText(context, "Next chapter was unable to be loaded", Toast.LENGTH_SHORT).show()
                                returner()
                            }
                            Log.d("TAG", "ReadScreen has : ${firstList.size}")
                            list.value = firstList + { endScreen(Modifier, "End of Chapter", "",
                                {viewModel.toggleReadBar() ; viewModel.toggleHomeBar()},
                                { its ->
                                    viewModel.viewModelScope.launch(Dispatchers.Main) {
                                        if (pagerState.currentPage >= pagerState.pageCount - 2){
                                            if (uiState.nextChapter == null){
                                                viewModel.markThisAsDone()
                                                viewModel.getNextChapter(context = context)
                                                viewModel.loadNextChapter()
                                                pagerState.scrollToPage(0)
                                            } else {
                                                viewModel.markThisAsDone()
                                                viewModel.loadNextChapter()
                                                viewModel.setFlag(true)
                                                pagerState.scrollToPage(0)
                                            }
                                        } else {
                                            withContext(its) {
                                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                                viewModel.setPage(pagerState.currentPage + 1)
                                            }
                                        }
                                    }
                                },
                                {its -> viewModel.viewModelScope.launch(Dispatchers.Main) {
                                    if (pagerState.currentPage == 0){
                                        if (uiState.previousChapter == null){
                                            viewModel.markThisAsDone()
                                            viewModel.getPreviousChapter(context)
                                            viewModel.loadPreviousChapter()
                                            pagerState.scrollToPage(0)
                                        } else {
                                            viewModel.markThisAsDone()
                                            viewModel.loadPreviousChapter()
                                            viewModel.setFlag(true)
                                            pagerState.scrollToPage(0)
                                        }
                                    } else {
                                        withContext(its) {
                                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                            viewModel.setPage(pagerState.currentPage - 1)
                                        }
                                    }
                                }}
                                )} + {Box(
                                Modifier
                                    .background(Color.Black)
                                    .fillMaxSize())}
                        }
                        is ChapterContents.Downloaded -> {
                            Log.d("TAG", "ReadScreen: is downloaded chapter and: ${contents.imagePaths}")
                            val firstList: List<@Composable () -> Unit> = contents.imagePaths.map { elm ->
                                {
                                    LaunchedEffect(Unit) {
                                        viewModel.toggleHomeBar(false)
                                        viewModel.toggleReadBar(false)
                                    }
                                    ImageView(Modifier, imageUrl = elm, onClick = {viewModel.toggleReadBar() ; viewModel.toggleHomeBar()}, context, { its ->
                                        viewModel.viewModelScope.launch {
                                            if (pagerState.currentPage >= pagerState.pageCount - 2){
                                                if (uiState.nextChapter == null){
                                                    viewModel.markThisAsDone()
                                                    viewModel.getNextChapter(context = context)
                                                    viewModel.loadNextChapter()
                                                    pagerState.scrollToPage(0)
                                                } else {
                                                    viewModel.markThisAsDone()
                                                    viewModel.loadNextChapter()
                                                    viewModel.setFlag(true)
                                                    pagerState.scrollToPage(0)
                                                }
                                            } else {
                                                withContext(its){
                                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                                    viewModel.setPage(pagerState.currentPage + 1)
                                                }
                                            }
                                        }
                                    }, { its ->
                                        viewModel.viewModelScope.launch(Dispatchers.Main) {
                                            if (pagerState.currentPage == 0){
                                                if (uiState.previousChapter == null){
                                                    viewModel.markThisAsDone()
                                                    viewModel.getPreviousChapter(context)
                                                    viewModel.loadPreviousChapter()
                                                    pagerState.scrollToPage(0)
                                                } else {
                                                    viewModel.markThisAsDone()
                                                    viewModel.loadPreviousChapter()
                                                    viewModel.setFlag(true)
                                                    pagerState.scrollToPage(0)
                                                }
                                            } else {
                                                withContext(its) {
                                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                                    viewModel.setPage(pagerState.currentPage - 1)
                                                }
                                            }
                                        }
                                    }, ifDownloaded = true, loadImage = {
                                        val s = viewModel.loadContentimage(uiState.currentManga!!.id ,uiState.currentChapter!!.id, elm)
                                        Log.d("TAG", "ReadScreen: $s")
                                        s
                                    }, "", pagerState)}
                            }
                            if (firstList.size == 0){
                                //inject warning for now but who cares.
                                Log.d("TAG", "chapter has no pages ")
                            }
                            Log.d("TAG", "ReadScreen has : ${firstList.size}")
                            list.value = firstList + { endScreen(Modifier, "End of Chapter", "",
                                {viewModel.toggleReadBar() ; viewModel.toggleHomeBar()},
                                { its ->
                                    viewModel.viewModelScope.launch(Dispatchers.Main) {
                                        if (pagerState.currentPage >= pagerState.pageCount - 2){
                                            if (uiState.nextChapter == null){
                                                viewModel.markThisAsDone()
                                                viewModel.getNextChapter(context = context)
                                                viewModel.loadNextChapter()
                                                pagerState.scrollToPage(0)
                                            } else {
                                                viewModel.markThisAsDone()
                                                viewModel.loadNextChapter()
                                                viewModel.setFlag(true)
                                                pagerState.scrollToPage(0)
                                            }
                                        } else {
                                            withContext(its) {
                                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                                viewModel.setPage(pagerState.currentPage + 1)
                                            }
                                        }
                                    }
                                },
                                {its -> viewModel.viewModelScope.launch(Dispatchers.Main) {
                                    if (pagerState.currentPage == 0){
                                        if (uiState.previousChapter == null){
                                            viewModel.markThisAsDone()
                                            viewModel.getPreviousChapter(context)
                                            viewModel.loadPreviousChapter()
                                            pagerState.scrollToPage(0)
                                        } else {
                                            viewModel.markThisAsDone()
                                            viewModel.loadPreviousChapter()
                                            viewModel.setFlag(true)
                                            pagerState.scrollToPage(0)
                                        }
                                    } else {
                                        withContext(its) {
                                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                            viewModel.setPage(pagerState.currentPage - 1)
                                        }
                                    }
                                }}
                            )} + {Box(
                                Modifier
                                    .background(Color.Black)
                                    .fillMaxSize())}
                        }
                    }

                }
            }
            viewModel.setFlag(false)
        }
    }






    BackHandler {
        viewModel.viewModelScope.launch {
            viewModel.toggleHomeBar(true)
            viewModel.toggleReadBar(true)
            viewModel.updateLastChapterRead(uiState.currentChapter!!.id, uiState.currentPage)
            returner()
        }
    }

    if (!view.isInEditMode){
        LaunchedEffect(uiState.homeBarVisible) {
            if (uiState.homeBarVisible){
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
        bottomBar = { MangaBotBar(Modifier,viewModel, { pagerState.scrollToPage(0) })}
    ) { innerPadding ->
        Box(Modifier.fillMaxSize()){
            ReadingScreen(Modifier.fillMaxSize(), viewModel, pagerState,context, list)
            if (uiState.currentPage <= (uiState.currentChapter?.pageCount?.toInt() ?: 0)){
                Text("${uiState.currentPage}/${uiState.currentChapter?.pageCount?.toInt()}",
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(innerPadding))
            }
        }
    }
    LaunchedEffect(Unit) {
        delay(80)
        Log.d("TAG", "ReadScreen: number of pages is ${list.value.size}")
        Log.d("TAG", "ReadScreen: current id is ${uiState.currentChapter?.id} and latest read is ${uiState.currentManga?.lastReadChapter?.first}")
        if (uiState.currentManga?.lastReadChapter?.first == uiState.currentChapter?.id ){
            Log.d("TAG", "ReadScreen: this si latest")
            pagerState.scrollToPage(uiState.currentManga?.lastReadChapter?.second?.minus(1) ?: 0)
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
            title = {Text("${if (uiState.currentChapter?.volume == -1.0) "" else "Vol. ${uiState.currentChapter?.volume}"} Ch. ${if (uiState.currentChapter?.chapter == -1.0)"Undefined" else uiState.currentChapter?.chapter} ${uiState.currentChapter?.name}")},
            navigationIcon = {
                IconButton(onClick = { viewModel.viewModelScope.launch {
                    viewModel.toggleHomeBar(true)
                    viewModel.toggleReadBar(true)
                    viewModel.updateLastChapterRead(uiState.currentChapter!!.id, uiState.currentPage)
                    returner()
                }}) {
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
fun MangaBotBar(modifier: Modifier = Modifier, viewModel: MangaSpecificViewModel, scrolltoZero: suspend ()->Unit){
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    AnimatedVisibility(
        visible = uiState.readBarVisible,
        enter = fadeIn(animationSpec = tween(50)),
        exit = fadeOut(animationSpec = tween(50))
    ) {
        BottomAppBar(actions = {
            Box(
                Modifier
                    .fillMaxSize()
                    .fillMaxHeight()) {
                IconButton(onClick = {
                    viewModel.viewModelScope.launch {
                        if (uiState.previousChapter == null){
                            Log.d("TAG", "MangaBotBar: loading previous chapter")
                            viewModel.getPreviousChapter(context)
                            viewModel.loadPreviousChapter()
                            viewModel.setFlag(true)
                            scrolltoZero()
                        } else {
                            viewModel.loadPreviousChapter()
                            viewModel.setFlag(true)
                            scrolltoZero()
                        }
                    }
                },Modifier.align(Alignment.CenterStart)) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBackIos,
                        contentDescription = null
                    )
                }

                IconButton(onClick = {
                    viewModel.viewModelScope.launch {
                        if (uiState.nextChapter !== null){
                            val contents = uiState.nextChapter!!.contents
                            if (contents !== null){
                                when (contents){
                                    is ChapterContents.Online -> {
                                        if (contents.imagePaths.isEmpty()){
                                            viewModel.getNextChapter(context = context)
                                            viewModel.loadNextChapter()
                                            viewModel.setFlag(true)
                                            scrolltoZero()
                                        } else {
                                            viewModel.markThisAsDone()
                                            viewModel.loadNextChapter()
                                            viewModel.setFlag(true)
                                            scrolltoZero()
                                        }
                                    }
                                    is ChapterContents.Downloaded -> {
                                        viewModel.markThisAsDone()
                                        viewModel.loadNextChapter()
                                        viewModel.setFlag(true)
                                        scrolltoZero()
                                    }
                                }
                            } else {
                                // contents is null load it
                                viewModel.markThisAsDone()
                                viewModel.getNextChapter(context = context)
                                viewModel.loadNextChapter()
                                viewModel.setFlag(true)
                                scrolltoZero()
                            }
                        } else {
                            viewModel.markThisAsDone()
                            viewModel.getNextChapter(context = context)
                            viewModel.loadNextChapter()
                            viewModel.setFlag(true)
                            scrolltoZero()
                        }

                    }
                },Modifier.align(Alignment.CenterEnd)) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowForwardIos,
                        contentDescription = null
                    )
                }
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
        val result = imageLoader.enqueue(request)
    }
}

 @OptIn(ExperimentalFoundationApi::class)
 private suspend fun PointerInputScope.detectVerticalScrollWithZones(
     scrollState: ScrollState,
     coroutineScope: CoroutineScope,
     leftZone: (CoroutineContext) -> Unit,
     rightZone: (CoroutineContext) -> Unit,
     centerClick: () -> Unit,
     pagerState: PagerState
 ) {
     awaitPointerEventScope {
         while (true) {
             val event = awaitPointerEvent()
             val change = event.changes.firstOrNull() ?: continue

             if (change.pressed) {
                 val dragAmount = change.positionChange().y
                 val xdragAmount = change.positionChange().x
                 if (dragAmount != 0f || xdragAmount != 0f) {
                     if (dragAmount != 0f){
                         scrollState.dispatchRawDelta(-dragAmount) // Allow scrolling
                         change.consume()
                         continue
                     }

                     Log.d("TAG", "drag detected horizontal detected")
                     continue
                 }


                 val position = change.position
                 val width = size.width

                 // Detect a tap gesture
                 if (!event.changes.any { it.isConsumed }) {
                     Log.d("TAG", "detectVerticalScrollWithZones: click detected")
                     when {
                         position.x < width * 0.35f -> {
                             Log.d("TAG", "detectVerticalScrollWithZones: click on left")
                             leftZone(coroutineScope.coroutineContext) // Left zone click
                         }
                         position.x > width * 0.65f -> {
                             Log.d("TAG", "detectVerticalScrollWithZones: click on right")
                             rightZone(coroutineScope.coroutineContext) // Right zone click
                         }
                         else -> {
                             Log.d("TAG", "detectVerticalScrollWithZones: click on center")
                             centerClick() // Center zone click
                         }
                     }
                 }
                 change.consume()

                 // Handle scroll gestures by passing them to the scroll state


             }
         }
     }
 }