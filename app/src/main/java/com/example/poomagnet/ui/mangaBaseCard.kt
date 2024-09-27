package com.example.poomagnet.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.poomagnet.R
import com.example.poomagnet.mangaDex.dexApiService.MangaInfo
import com.example.poomagnet.mangaDex.dexApiService.mangaState
import com.example.poomagnet.ui.HomeScreen.displayType

//this should work for both versions of mangaInfo, i.e the one including the covertArturl and the one with the downloaded image.
@Composable
fun VerticalCardTest(modifier: Modifier = Modifier, manga: MangaInfo){
    Log.d("TAG", "url given is "+ manga.coverArtUrl)
    if (true) { //manga.coverArt != null
        Card(modifier = modifier.sizeIn(300.dp,160.dp,300.dp,160.dp)) {
            Row(modifier = Modifier, horizontalArrangement = Arrangement.Start) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(manga.coverArtUrl)
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(R.drawable.prevthumbnail),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                )
                Column {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp), contentAlignment = Alignment.Center) {
                        Text(manga.title, fontSize = 20.sp)
                    }
                }
            }
        }
    }
}



@Composable
fun DoubleStackCard(modifier: Modifier = Modifier, manga: MangaInfo, click: (MangaInfo) -> Unit, loadImage: suspend (String, String) -> String) {
    Log.d("TAG", "url given is "+ manga.coverArtUrl)
    Card(modifier = modifier
        .height(250.dp)
        .width(110.dp), shape = CardDefaults.shape, onClick = { click(manga) }){
        Box {
            // Main content of the card
            if (manga.inLibrary) {
                var uri by remember{ mutableStateOf("")}
                LaunchedEffect(Unit){
                    uri = loadImage(manga.id, manga.id+".jpeg")
                    Log.d("TAG", "VerticalCard: iamge loaded with uri $uri")
                }
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(uri)
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(R.drawable.prevthumbnail),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                )
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(manga.coverArtUrl)
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(R.drawable.prevthumbnail),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Text(
                text = manga.title,  // Adjust text as needed
                fontSize = 14.sp,
                color = Color.White,
                maxLines = 2,  // Limit text to 2 lines
                overflow = TextOverflow.Ellipsis,  // Add ellipsis if text overflows
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp)
                    .fillMaxWidth()  // Ensure text takes up full width of the card
            )
        }
    }
}

//@Preview
//@Composable
//fun DoubleStackCardPreviewScreen() {
//    val list: MutableList<MangaInfo> = mutableListOf()
//    val dbl = MangaInfo("a","b","c",listOf("d"), "e",mangaState.IN_PROGRESS,"f",listOf("g"),null,"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTMdM9MEQ0ExL1PmInT3U5I8v63YXBEdoIT0Q&s",0)
//    for(i in 0..11){
//        list.add(dbl)
//    }
//
//    LazyVerticalGrid(
//        columns = GridCells.Fixed(2), // 2 items per row
//        modifier = Modifier.fillMaxSize(),
//        contentPadding = PaddingValues(16.dp),
//        horizontalArrangement = Arrangement.spacedBy(16.dp),
//        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        items(list) { manga ->
//            DoubleStackCard(
//                manga = manga,
//                click = {}
//            )
//        }
//    }
//}





@Composable
fun VerticalCard(modifier: Modifier = Modifier, manga: MangaInfo, onclick: () -> Unit, engageChapter: (String) -> Unit, openLast: () -> Unit, loadImage: suspend (String, String) -> String, somethingChanged: Boolean){
    Card(modifier = modifier
        .sizeIn(300.dp, 160.dp, 300.dp, 160.dp)
        .clickable { onclick() }) {
        Row(modifier = Modifier, horizontalArrangement = Arrangement.Start) {
            if (manga.inLibrary){
                Log.d("TAG", "VerticalCard: sending uri ${manga.coverArtUrl.split("/").last().substringBeforeLast(".")+".jpeg"}")
                var image by remember { mutableStateOf("") }
                LaunchedEffect(somethingChanged) {
                    image = loadImage(manga.id,manga.id+".jpeg")
                    Log.d("TAG", "VerticalCard: iamge loaded with uri $image")
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
                        .fillMaxHeight()
                        .fillMaxWidth(0.32f)
                )
            }else {

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(manga.coverArtUrl)
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(R.drawable.prevthumbnail),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.32f)
                )
            }
            Column(Modifier.fillMaxWidth()) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp), contentAlignment = Alignment.Center) {
                    Text(manga.title, fontSize = 18.sp, modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .align(Alignment.CenterStart)
                        .padding(11.dp, 0.dp, 0.dp, 0.dp), overflow = TextOverflow.Ellipsis, maxLines = 2)
                    IconButton(
                        onClick = {openLast()},
                        Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(Icons.Default.History, "", Modifier.padding(0.dp,0.dp,9.dp,0.dp))
                    }

                }
                LazyColumn(modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .fillMaxHeight()) {
                    Log.d("TAG", "VerticalCard: found ${manga.chapterList?.second?.size} chapters")
                    items(manga.chapterList?.second ?: listOf()) { chapter ->
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(35.dp)
                            .clickable {
                                engageChapter(chapter.id)
                            },
                        ) {
                            Text("Ch. ${chapter.chapter} ${chapter.name}", modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(8.dp, 0.dp)
                                .fillMaxWidth(), maxLines = 1, overflow = TextOverflow.Ellipsis, color = if (chapter.finished){Color.Gray} else Color.Unspecified)
                            Divider(Modifier.fillMaxWidth(),color = Color.LightGray, thickness =  0.5.dp, )
                        }
                    }
                }
            }

        }
    }
}


