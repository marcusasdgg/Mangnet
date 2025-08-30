package com.example.poomagnet.manganatoService

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitInstance {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC // or Level.BASIC, Level.HEADERS based on your needs
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .client(client) // attach the custom OkHttpClient
        .addConverterFactory(ScalarsConverterFactory.create())
        .baseUrl("https://natomanga.com")
        .build()

    val api: natoService = retrofit.create(natoService::class.java)
}