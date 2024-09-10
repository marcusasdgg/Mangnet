package com.example.poomagnet.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poomagnet.mangaDex.dexApiService.MangaInfo
import com.example.poomagnet.ui.HomeScreen.mangaInfo

@Composable
fun VerticalCardTest(modifier: Modifier = Modifier, manga: MangaInfo){
    if (manga.coverArt != null) {
        Card(modifier = modifier.sizeIn(300.dp,160.dp,300.dp,160.dp)) {
            Row(modifier = Modifier, horizontalArrangement = Arrangement.Start) {
                Image(manga.coverArt, contentDescription = "")
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