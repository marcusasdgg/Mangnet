package com.example.poomagnet.ui.MangaExpanded

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.poomagnet.mangaDex.dexApiService.MangaInfo
import kotlinx.coroutines.delay

@Composable
fun MangaScreen(modifier: Modifier = Modifier, manga: MangaInfo?) {
    val visible = remember { mutableStateOf(false) }

    // Trigger visibility change after some delay
    LaunchedEffect(Unit) {
        delay(60) // Delay before showing the content
        visible.value = true
    }
    AnimatedVisibility(
        visible = visible.value,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(manga?.alternateTitles.toString() ?: "something went terribly wrong" )
        }
    }

}
