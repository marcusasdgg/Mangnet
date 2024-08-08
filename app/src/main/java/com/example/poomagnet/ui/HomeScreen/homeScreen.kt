package com.example.poomagnet.ui.HomeScreen

import android.graphics.Paint.Align
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poomagnet.R

@Composable
fun HomeScreen( modifier: Modifier = Modifier, hideBottomBar: () -> Unit = {}) {
    val s = listOf(
        mangaInfo(R.drawable.prevthumbnail, listOf(1,2,3,4,5,6,7,8,9,10,11,12,), "description", "YuruCamp"),
        mangaInfo(R.drawable.prevthumbnail, listOf(1,2,3,4,5,6,7,8,9,10,11,12,), "description", "YuruCamp"),
        mangaInfo(R.drawable.prevthumbnail, listOf(1,2,3,4,5,6,7,8,9,10,11,12,), "description", "YuruCamp"),
        mangaInfo(R.drawable.prevthumbnail, listOf(1,2,3,4,5,6,7,8,9,10,11,12,), "description", "YuruCamp"),
        mangaInfo(R.drawable.prevthumbnail, listOf(1,2,3,4,5,6,7,8,9,10,11,12,), "description", "YuruCamp"),
        mangaInfo(R.drawable.prevthumbnail, listOf(1,2,3,4,5,6,7,8,9,10,11,12,), "description", "YuruCamp"),
        mangaInfo(R.drawable.prevthumbnail, listOf(1,2,3,4,5,6,7,8,9,10,11,12,), "description", "YuruCamp"),
        )
    Column {
        LazyColumn(modifier = modifier.fillMaxWidth()) {
            items(s) { manga ->
                VerticalCard(manga = manga, modifier =  Modifier.fillMaxWidth().padding(8.dp,0.dp))
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}


@Composable
fun HomeTopBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        verticalArrangement = Arrangement.SpaceBetween, // Space between items
        horizontalAlignment = Alignment.Start,

    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.CenterStart// Take up available vertical space
        ) {
            Text("Library", Modifier.padding(start = 20.dp), fontSize = 25.sp) //need to make this clickable
        }
        Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.fillMaxWidth())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun HomeTopBarV2(){
    TopAppBar(title = {Text("Library", fontSize = 28.sp)})
}




@Preview
@Composable
fun HomeTopPreview() {
    HomeTopBar()
}