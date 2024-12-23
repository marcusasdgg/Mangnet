package com.example.poomagnet.ui.SearchScreen

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.poomagnet.mangaRepositoryManager.MangaInfo
import com.example.poomagnet.mangaRepositoryManager.Ordering
import com.example.poomagnet.mangaRepositoryManager.Sources
import com.example.poomagnet.ui.DoubleStackCard
import com.example.poomagnet.ui.SortDrawer.sortDrawer
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(modifier: Modifier = Modifier, searchViewModel: SearchViewModel = hiltViewModel()) {
    //includes source selector and search bar.
    val state = searchViewModel.uiState.collectAsState().value
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current //

    val animatedWidth by animateDpAsState(
        targetValue = if (state.searchExpanded) 370.dp else 220.dp, // Target width values
        animationSpec = tween(durationMillis = 300) // Adjust duration as needed
    )

    TopAppBar(title = {
        Box(
            modifier = Modifier
                .fillMaxHeight() // Ensure the Box takes up the full height of the TopAppBar
                .fillMaxWidth(), // Optionally, ensure the Box takes up the full width if needed
            contentAlignment = Alignment.Center // Center the content vertically
        ) {
        Row( horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .padding(0.dp, 10.dp, 10.dp, 13.dp)
            .fillMaxWidth()
            .fillMaxHeight()){
            if (!state.searchExpanded){
                Button(
                    onClick = { searchViewModel.dropdownSource(true) },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = Color.White
                    ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.width(105.dp),
                    ) {
                        Text(
                            state.sourceSelected.toString(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight(650)
                        )
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = "Chevron Icon",
                            modifier = Modifier.offset(y = 2.dp)
                        )
                    }
                }
                DropdownMenu(
                    expanded = state.sourceExpanded,
                    onDismissRequest = { searchViewModel.dropdownSource(false) }) {
                    DropdownMenuItem(
                        text = { Text("MangaDex") },
                        onClick = {
                            searchViewModel.changeSource(Sources.MANGADEX)
                            searchViewModel.dropdownSource(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("MangaNato") },
                        onClick = {
                            searchViewModel.changeSource(Sources.MANGANATO)
                            searchViewModel.dropdownSource(false)
                        }
                    )
                }
            }

            OutlinedTextField(value = state.searchText
                ,onValueChange = {searchViewModel.updateSearchText(it)},
                keyboardOptions = KeyboardOptions.Default.copy(  // Sets up keyboard options, specifying an IME action
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        Log.d("TAG", "SearchTopBar: search clicked")
                        focusManager.clearFocus()
                    }
                )
                ,
                placeholder = {Text("Search")},
                trailingIcon = {Icon(imageVector = Icons.Default.Search, // Use the built-in search icon
                    contentDescription = "Search Icon",)},
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .focusable()
                    .width(animatedWidth)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            searchViewModel.expandSearchBar(true)
                        } else {
                            searchViewModel.expandSearchBar(false)
                        }
                    }
            )
        }
    }}, modifier = modifier.height(124.5.dp))

}



@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    searchViewModel: SearchViewModel,
    setCurrentManga: (MangaInfo) -> Unit,
    currentScrollState: LazyGridState
) {
    val sheetState = rememberModalBottomSheetState()


    val uiState by searchViewModel.uiState.collectAsState()


    // i need this so that on first load of the search screen, the default tag is follow count, but once
    // changed i need to make it so that it is by Relevance
    LaunchedEffect(uiState.searchText, uiState.somethingChanged, uiState.somethingAdded) {
        searchViewModel.resetPageNo()
        Log.d("TAG", "SearchScreen: effect launched")
        if (uiState.firstLoad){
            Log.d("TAG", "First Load detected: ")
            searchViewModel.selectFirstOrder(Ordering.Followed_Count, Pair(true, Direction.Ascending))
            currentScrollState.scrollToItem(0)
            searchViewModel.executeSearch()
            Log.d("TAG", "Ding")
            searchViewModel.changeFirstLoad()
        } else if(uiState.secondLoad && !uiState.firstLoad && uiState.oldText !== uiState.searchText) {
            Log.d("TAG", "Second Load detected: ")
            searchViewModel.selectFirstOrder(Ordering.Relevance, Pair(true, Direction.Ascending))
            currentScrollState.scrollToItem(0)
            searchViewModel.executeSearch()
            searchViewModel.changeSecondLoad()
        } else {
            Log.d("TAG", "Normal Load detected: ${uiState.somethingChanged}")
            if (uiState.oldText != uiState.searchText || uiState.somethingChanged){
                Log.d("TAG", "SearchScreen: old text !== new test and something changed ${uiState.oldText} and ${uiState.searchText}")
                currentScrollState.scrollToItem(0)
                searchViewModel.executeSearch()
                searchViewModel.setFlag(false)
                Log.d("TAG", "Ding")
            }
            if (uiState.somethingAdded){
                Log.d("TAG", "SearchScreen: something added")
                searchViewModel.executeSearch()
                searchViewModel.resetAddManga()
            }
        }
    }



    LaunchedEffect(currentScrollState) {
        snapshotFlow { currentScrollState.layoutInfo }
            .collect { layoutInfo ->
                val totalItemsCount = layoutInfo.totalItemsCount
                val visibleItems = layoutInfo.visibleItemsInfo
                if (visibleItems.isNotEmpty()) {
                    val lastVisibleItemIndex = visibleItems.last().index
                    if (lastVisibleItemIndex == totalItemsCount - 1) {
                        searchViewModel.continueSearch()
                        delay(400)
                    }
                }
            }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = searchViewModel::loadit,
        refreshThreshold = 160.dp,
        refreshingOffset = 180.dp
    )


    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // 2 items per row
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            state = currentScrollState
        ) {
            items(uiState.searchListing) { manga ->
                DoubleStackCard(
                    manga = manga,
                    click = setCurrentManga,
                    loadImage = {elm1, elm2 -> searchViewModel.loadImageFromLibrary(elm1,elm2) }
                )
            }
        }

        PullRefreshIndicator(
            refreshing = uiState.isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
        )

    }

    sortDrawer(Modifier, searchViewModel)
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

//adding bottomSheet to bott nav bar will help.