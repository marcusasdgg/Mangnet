package com.example.poomagnet.ui.UpdateScreen

import com.example.poomagnet.mangaDex.dexApiService.SimpleDate
import com.example.poomagnet.mangaDex.dexApiService.slimChapter

data class updateUiState (
    val showList: Map<String, List<slimChapter>> = mapOf()
)