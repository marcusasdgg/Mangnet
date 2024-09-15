package com.example.poomagnet.App.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.BreakfastDining
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import com.example.poomagnet.App.BottomNavInfo
import com.example.poomagnet.App.ScreenType
import com.example.poomagnet.R

object BottomList {
    val infoList = listOf(
        BottomNavInfo(R.string.Library, Icons.Default.Book, ScreenType.Home, "Home"),
        BottomNavInfo(R.string.Update, Icons.Default.Refresh, ScreenType.Update, "Update"),
        BottomNavInfo(R.string.Search, Icons.Default.Search, ScreenType.Search, "Search"),
        BottomNavInfo(R.string.settings, Icons.Default.Settings, ScreenType.Settings, "Settings"),
    )
}