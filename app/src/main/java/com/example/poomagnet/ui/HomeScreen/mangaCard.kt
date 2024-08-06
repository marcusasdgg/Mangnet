package com.example.poomagnet.ui.HomeScreen

import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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
fun VerticalCard(modifier: Modifier, manga: mangaInfo){

}

