package com.example.poomagnet.ui.HomeScreen

data class mangaInfo(
    val coverImage: Int, //handle to image
    val availableChapters: List<Int>, //an array of available chapters containg name and urls
    val description: String,
)

data class Chapter(
    val name: String,
    val images: List<Int>, //pictures of Chapter.
    val ifDownloaded: Boolean,
)