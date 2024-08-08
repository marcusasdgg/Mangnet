package com.example.poomagnet.ui.HomeScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poomagnet.R
import com.example.poomagnet.ui.theme.AppTheme

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
fun VerticalCard(modifier: Modifier = Modifier, manga: mangaInfo){
    Card(modifier = modifier.sizeIn(300.dp,160.dp,300.dp,160.dp)) {
        Row(modifier = Modifier, horizontalArrangement = Arrangement.Start) {
            Image(painterResource(id = manga.coverImage), contentDescription = "")
            Column {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp), contentAlignment = Alignment.Center) {
                    Text(manga.title, fontSize = 20.sp)
                }
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(manga.availableChapters.reversed()) { chapter ->
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(35.dp),
                        ) {
                            Text("chapter $chapter", modifier = Modifier.align(Alignment.CenterStart).padding(8.dp,0.dp))
                            Divider(color = Color.LightGray, thickness =  0.5.dp)
                        }
                    }
                }
            }

        }
    }
}

@Preview
@Composable
fun VertPreview() {
    AppTheme(darkTheme = true) {
        val s = mangaInfo(R.drawable.prevthumbnail, listOf(1,2,3,4,5,6,7,8,9,10,11,12,), "description", "YuruCamp");
        VerticalCard(manga = s)
    }
}