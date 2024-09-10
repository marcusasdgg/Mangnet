package com.example.poomagnet.ui.HomeScreen

import android.graphics.ColorSpace.Rgb
import android.graphics.Paint.Align
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poomagnet.R
import java.util.logging.Filter

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
                VerticalCard(manga = manga, modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp, 0.dp))
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBarV2(toggleDrop: (Boolean) -> Unit, uiState: HomeUiState, changeDrop: (FilterOptions) -> Unit){
    TopAppBar(title = {
        Button(onClick = {toggleDrop(true)}, colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = Color.White
        )) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
                Text(uiState.currentMenuOption.toString(), fontSize = 28.sp)
                Icon(
                    imageVector =  Icons.Default.ExpandMore,
                    contentDescription = "Chevron Icon",
                    modifier = Modifier.offset(y = 2.dp)
                )
            }
        }
        DropdownMenu(expanded = uiState.expandedMenu, onDismissRequest = {toggleDrop(false)}) {
            DropdownMenuItem(
                text = { Text("All") },
                onClick = {
                    changeDrop(FilterOptions.All)
                    toggleDrop(false)
                }
            )
            DropdownMenuItem(
                text = { Text("Comedy") },
                onClick = {
                    changeDrop(FilterOptions.Comedy)
                    toggleDrop(false)
                }
            )
            DropdownMenuItem(
                text = { Text("Romance") },
                onClick = {
                    changeDrop(FilterOptions.Romance)
                    toggleDrop(false)
                }
            )
        }
    })
}




