package com.example.poomagnet.ui.SortDrawer

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.North
import androidx.compose.material.icons.outlined.South
import androidx.compose.material.icons.sharp.ArrowDownward
import androidx.compose.material.icons.sharp.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorModel.Companion.Rgb
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poomagnet.R
import com.example.poomagnet.ui.HomeScreen.HomeViewModel
import com.example.poomagnet.ui.HomeScreen.VerticalCard
import com.example.poomagnet.ui.HomeScreen.mangaInfo
import com.example.poomagnet.ui.SearchScreen.Direction
import com.example.poomagnet.ui.SearchScreen.SearchUiState
import com.example.poomagnet.ui.SearchScreen.SearchViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun sortDrawer(modifier: Modifier = Modifier, viewModel: SearchViewModel){
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showDrawer){
        ModalBottomSheet(onDismissRequest = {viewModel.revealBottomSheet(false)}, Modifier.height(600.dp)) {
            Column(modifier = modifier.verticalScroll(rememberScrollState()).fillMaxSize()) {
                OrderBy(Modifier,viewModel)
                    Row(Modifier.fillMaxWidth().height(200.dp).border(1.dp,Color.Black)) {
                        //demographic and content rating
                    }

                    //tag list in shape of dual column following check box -> Text format.

                    //content rating at the end.

            }
        }
    }
}


@Composable
fun OrderBy(modifier: Modifier = Modifier, viewModel: SearchViewModel){
    val uiState by viewModel.uiState.collectAsState()
    Text("Order By:", modifier = Modifier.padding(10.dp,0.dp,0.dp,5.dp))
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // Two columns
        modifier = Modifier.fillMaxWidth().height(150.dp)
    ) {
        items(uiState.sortTags.entries.toList()){ (order, state) ->
            Row(Modifier.fillMaxWidth().height(150.dp/4) ) {
                Box(Modifier.fillMaxSize().clickable { viewModel.selectOrder(order,state) }, contentAlignment = Alignment.Center){
                    Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.fillMaxHeight().width(30.dp), contentAlignment = Alignment.Center) {
                            if (state.first){
                                Icon(if (state.second == Direction.Descending) Icons.Outlined.South else Icons.Outlined.North, "", tint = Color(0xFF3E8896))
                            }
                        }
                        Text(text = order.toString(), fontWeight = FontWeight.Light)
                    }
                }
            }
        }
    }

}



@Composable
fun tagSection(){

}




@Composable
fun sortDrawer(modifier: Modifier = Modifier, viewModel: HomeViewModel){

}