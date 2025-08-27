package com.example.poomagnet.comickService

import android.content.Context
import com.example.poomagnet.downloadService.DownloadService
import javax.inject.Inject
import com.example.poomagnet.comickService.retroFitInstance

class ComickRepository @Inject constructor(val context: Context, private val downloadService: DownloadService){
    private val apiService = retroFitInstance.api



}