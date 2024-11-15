package com.example.poomagnet.downloadService

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Url

interface DownService{
    @GET
    suspend fun downloadFile(@Url url: String,@Header("Referer") referer: String? ): Response<ResponseBody>
}