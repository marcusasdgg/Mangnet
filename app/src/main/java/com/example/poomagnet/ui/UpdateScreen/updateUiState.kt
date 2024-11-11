package com.example.poomagnet.ui.UpdateScreen

import com.example.poomagnet.mangaRepositoryManager.slimChapter

data class updateUiState (
    val showList: Map<String, List<slimChapter>> = mapOf()
)