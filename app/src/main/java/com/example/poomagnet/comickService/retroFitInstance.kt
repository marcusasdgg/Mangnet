package com.example.poomagnet.comickService

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object retroFitInstance {
    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://api.mangadex.org")
        .build()

    val api: mickService = retrofit.create(mickService::class.java)
}