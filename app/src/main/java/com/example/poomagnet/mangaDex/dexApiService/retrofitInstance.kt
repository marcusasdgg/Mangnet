package com.example.poomagnet.mangaDex.dexApiService

import MangaDexApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://api.mangadex.org")
        .build()

    val api: MangaDexApiService = retrofit.create(MangaDexApiService::class.java)
}