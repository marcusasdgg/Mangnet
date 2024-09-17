package com.example.poomagnet.ui.SortDrawer

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorModel.Companion.Rgb
import androidx.compose.ui.state.ToggleableState
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

    if (uiState.showDrawer){ //uiState.showDrawer
        ModalBottomSheet(onDismissRequest = {viewModel.revealBottomSheet(false)}, Modifier.height(600.dp)) { //viewModel.revealBottomSheet(false)
            LazyColumn(modifier.fillMaxSize()) {
                item{
                    OrderBy(Modifier,viewModel)
                    Spacer(Modifier.height(15.dp))
                    Row(Modifier.fillMaxWidth().height(150.dp)) {
                        //demographic and content rating
                        Demographic(Modifier.weight(1f).fillMaxHeight(),viewModel)
                        ContentRating(Modifier.weight(1f).fillMaxHeight(),viewModel)
                    }
                    Spacer(Modifier.height(30.dp))
                }

                item {
                    TagListing(Modifier, viewModel)
                }
                //tag list in shape of dual column following check box -> Text format.

                //content rating at the end.
            }
        }
    }
}

//OrderBy(Modifier,viewModel)
//                    Row(Modifier.fillMaxWidth().height(150.dp)) {
//                        //demographic and content rating
//                        Demographic(Modifier.weight(1f),viewModel)
//                        Demographic(Modifier.weight(1f),viewModel)
//                    }
//                Spacer(Modifier.height(30.dp))
//                    TagListing(Modifier, viewModel)
//                    //tag list in shape of dual column following check box -> Text format.
//
//                    //content rating at the end.

//make entire thing clickable
@Composable
fun CheckATitle(modifier: Modifier = Modifier, title: String, checkboxState: ToggleableState, onclick: () -> Unit){
    Box(modifier.fillMaxWidth().fillMaxHeight().clickable { onclick() }){
        Row(Modifier.fillMaxWidth().fillMaxHeight(), verticalAlignment = Alignment.CenterVertically){
            TriStateCheckbox(checkboxState,onclick)
            Text(title)
        }
    }
}

@Composable
fun ContentRating(modifier: Modifier = Modifier, viewModel: SearchViewModel){
    val uiState by viewModel.uiState.collectAsState()
    Column(modifier = modifier.fillMaxWidth().fillMaxHeight()){
        Text("Content Rating:", Modifier.padding(10.dp,0.dp,0.dp,0.dp))
        for ((rating, state) in uiState.contentRating.entries){
            CheckATitle(Modifier.fillMaxWidth().weight(1f), rating.toString(),state) { viewModel.setContentRating(rating, state)}
        }
    }

}

@Composable
fun TagListing(modifier: Modifier = Modifier, viewModel: SearchViewModel){
    val uiState by viewModel.uiState.collectAsState()
    val halfindex = uiState.tagsIncluded.size/2
    val list = uiState.tagsIncluded.toList()
    Text("Genres:", Modifier.padding(10.dp,0.dp,0.dp,0.dp))
    Row(modifier){
        Column(Modifier.weight(1f)){
            for (i in 0..halfindex-1){
                CheckATitle(Modifier.fillMaxSize(),list[i].first.toString(),list[i].second) {viewModel.setTag(list[i].first, list[i].second) }
            }
        }
        Column(Modifier.weight(1f)){
            for (i in halfindex..uiState.tagsIncluded.size-1){
                CheckATitle(Modifier.fillMaxSize(),list[i].first.toString(),list[i].second) { viewModel.setTag(list[i].first, list[i].second) }
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

//add text in here
@Composable
fun Demographic(modifier: Modifier = Modifier, viewModel: SearchViewModel){
    val uiState by viewModel.uiState.collectAsState()
    Column(modifier.fillMaxWidth().fillMaxHeight()){
        Text("Demographic:", Modifier.padding(10.dp,0.dp,0.dp,0.dp))
        Column(modifier.fillMaxWidth().fillMaxHeight()) {
            for(demo in uiState.demographics){
                CheckATitle(
                    Modifier.fillMaxWidth().weight(1f),
                    title = demo.key.toString(),
                    checkboxState = demo.value,
                    onclick = {viewModel.setDemo(demo.key,demo.value)}
                )
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