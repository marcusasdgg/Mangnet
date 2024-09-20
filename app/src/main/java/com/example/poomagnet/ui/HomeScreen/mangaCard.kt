package com.example.poomagnet.ui.HomeScreen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.poomagnet.R
import com.example.poomagnet.mangaDex.dexApiService.MangaInfo
import com.example.poomagnet.ui.theme.AppTheme
import com.example.poomagnet.ui.theme.onSurfaceDark

@Composable
fun MangaCard(modifier: Modifier = Modifier, type: displayType, manga: mangaInfo) {
    when (type) {
        displayType.VERTICALCARD -> {}
        displayType.LISTSCROLL -> {}
        displayType.SINGLESCREEN -> {}
        displayType.TWOGRID -> {}
    }
}

@Composable
fun VerticalCard(modifier: Modifier = Modifier, manga: MangaInfo){
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
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.32f)
            )
            Column(Modifier.fillMaxWidth(0.8f)) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp), contentAlignment = Alignment.Center) {
                    Text(manga.title, fontSize = 20.sp)
                }
                LazyColumn(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                    Log.d("TAG", "VerticalCard: found ${manga.chapterList?.second?.size} chapters")
                    items(manga.chapterList?.second ?: listOf()) { chapter ->
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(35.dp),
                        ) {
                            Text("Vol.${chapter.volume} Ch. ${chapter.chapter} ${chapter.name}", modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(8.dp, 0.dp).fillMaxWidth(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Divider(Modifier.fillMaxWidth(),color = Color.LightGray, thickness =  0.5.dp, )
                        }
                    }
                }
            }

        }
    }
}

