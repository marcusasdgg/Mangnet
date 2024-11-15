package com.example.poomagnet.ui.UpdateScreen

import com.example.poomagnet.mangaRepositoryManager.SlimChapter

data class updateUiState (
    val showList: Map<String, List<SlimChapter>> = mapOf()
)