package com.example.poomagnet.App

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.poomagnet.R
import com.example.poomagnet.ui.theme.AppTheme

data class BottomNavInfo(
    @StringRes val name: Int,
    val icon: ImageVector,
    val screenType: ScreenType,
    val route: String,
)

enum class ScreenType{
    Home, Search, Update, Settings
}


@Composable
fun BottomNavBar(
    modifier: Modifier = Modifier,
    infoList: List<BottomNavInfo>,
    currentTab: ScreenType,
    onButtonPressed: (ScreenType) -> Unit,
    ) {
    Log.d("APPMAIN", currentTab.name)
    NavigationBar(modifier) {
        for (item in infoList)
        NavigationBarItem(
            selected = currentTab == item.screenType,
            onClick = {
                onButtonPressed(item.screenType)
            },
            icon = {
                if (currentTab == ScreenType.Search && item.screenType == ScreenType.Search){
                    Icon(imageVector = Icons.Default.FilterList, contentDescription = stringResource(
                        id = item.name

                    ))
                } else if(currentTab == ScreenType.Home && item.screenType == ScreenType.Home){
                    Icon(imageVector = Icons.AutoMirrored.Default.List, contentDescription = stringResource(
                        id = item.name
                    ))
                } else {
                    Icon(imageVector = item.icon, contentDescription = stringResource(
                        id = item.name
                    ))
                }

           },
            label = {if (currentTab == ScreenType.Search && item.screenType == ScreenType.Search){
                Text("Filter")
            } else {
                Text(stringResource(id = item.name))
            }}
        )
    }
}



@Composable
@Preview(showBackground = true)
private fun BarPreview() {
        val infoList = listOf(
            BottomNavInfo(R.string.Library, Icons.Default.Book, ScreenType.Home, ""),
            BottomNavInfo(R.string.Update, Icons.Default.Refresh, ScreenType.Update, ""),
            BottomNavInfo(R.string.Search, Icons.Default.Search, ScreenType.Search, ""),
            BottomNavInfo(R.string.settings, Icons.Default.Settings, ScreenType.Settings, "")
        )
        AppTheme(darkTheme = true) {
            var screenType by remember { mutableStateOf(ScreenType.Home) }
            BottomNavBar(
                infoList = infoList,
                currentTab = screenType,
                onButtonPressed = { new -> screenType = new }
            )
        }

}

//falsecomment