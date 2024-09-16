package com.example.poomagnet.ui.MangaExpanded

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.poomagnet.mangaDex.dexApiService.MangaInfo

@Composable
fun MangaScreen(modifier: Modifier = Modifier, manga: MangaInfo?) {
    Text(manga?.alternateTitles.toString() ?: "something went terribly wrong" )
}
