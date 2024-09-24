package com.example.poomagnet.downloadService

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object DownloadRetrofitInstance {
    private val retrofit: Retrofit = try {
        Retrofit.Builder()
            .baseUrl("https://www.google.com") // Ensure a valid base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    } catch (e: Exception) {
        Log.e("DownloadRetrofit", "Error initializing Retrofit", e)
        throw e // Rethrow or handle as appropriate
    }

    val api: DownService = try {
        retrofit.create(DownService::class.java)
    } catch (e: Exception) {
        Log.e("DownloadRetrofit", "Error creating API service", e)
        throw e // Rethrow or handle as appropriate
    }
}