package com.example.poomagnet.manganatoService

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitInstance {
    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .baseUrl("https://manganato.com")
        .build()

    val api: natoService = retrofit.create(natoService::class.java)
}