package com.example.poomagnet.ui.SearchScreen

import android.graphics.Paint.Align
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.poomagnet.ui.DoubleStackCard
import com.example.poomagnet.ui.VerticalCardTest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch


enum class Sources {
    ALL,
    MANGADEX,
    MANGANATO,
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(modifier: Modifier = Modifier, searchViewModel: SearchViewModel = hiltViewModel()) {
    //includes source selector and search bar.
    val state = searchViewModel.uiState.collectAsState().value
    val coroutineScope = rememberCoroutineScope()

    TopAppBar(title = {OutlinedTextField(value = state.searchText
        ,onValueChange = {searchViewModel.updateSearchText(it)},
        keyboardOptions = KeyboardOptions.Default.copy(  // Sets up keyboard options, specifying an IME action
            imeAction = ImeAction.Search
        ),
//        keyboardActions = KeyboardActions(
//            onSearch = {
//                coroutineScope.launch {
//                    searchViewModel.executeSearch()
//                }
//            }
//        )
    ) })

}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(modifier: Modifier = Modifier, searchViewModel: SearchViewModel) {
    //left side bar.

    val uiState = searchViewModel.uiState.collectAsState().value

    LaunchedEffect(uiState.searchText) {
        searchViewModel.executeSearch()
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // 2 items per row
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),

    ) {
        items(uiState.searchListing) { manga ->
            DoubleStackCard(
                manga = manga
            )
        }
    }

    //
}


@Preview
@Composable
fun previewSearchTOp() {
    SearchTopBar()
}

//search screen sort optimization.
//by pressing the search button again when on search screen,
// will pull up the Sort and filter tags option, making topsearch bar minimal with just
//the search bar and source list.



//switch ImageCard to use Coil instead of Image composable and make it donwload image link, then async download image 1 by 1 to make overal faster.`
//wrap the executesearch in an try-catch as if internet is down, the app will crash.