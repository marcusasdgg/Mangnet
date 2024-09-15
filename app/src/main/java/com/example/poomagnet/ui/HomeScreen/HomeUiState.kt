package com.example.poomagnet.ui.HomeScreen

data class HomeUiState(
    val typeView: displayType = displayType.VERTICALCARD,
    val expandedMenu: Boolean = false,
    val currentMenuOption: FilterOptions = FilterOptions.All
    //val followed manga = ???
)

enum class FilterOptions{
    All,
    Romance,
    Comedy,
    Action,
    Cultivation,
}