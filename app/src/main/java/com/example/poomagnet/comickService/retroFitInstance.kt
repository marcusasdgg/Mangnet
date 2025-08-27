package com.example.poomagnet.comickService

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object retrofitInstance {
    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://api.comick.fun")
        .build()

    val api: mickService = retrofit.create(mickService::class.java)
}