package com.example.poomagnet.comickService

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface mickService {
    // gets all tags slugified
    @GET("genre/?tachiyomi=true")
    @Headers("Referer: https://comick.app/", "Origin: https://comick.app/", "X-Requested-With: eu.kanade.tachiyomi", "User-Agent: Android", "Connection: keep-alive")
    suspend fun getGenreList() : List<Map<String, Any?>>


    @GET("/v1.0/search/?type=comic&tachiyomi=true")
    @Headers("Referer: https://comick.app/", "Origin: https://comick.app/", "X-Requested-With: eu.kanade.tachiyomi", "User-Agent: Android", "Connection: keep-alive")
    suspend fun searchAllManga(
        @Query("q") search: String,
        @Query("genres") genres: List<String>?,
        @Query("excludes") genre_excludes: List<String>?,
        @Query("demographic") demo: List<Int>?,
        @Query("page") page: Int,
        @Query("content_rating") contentRating: String?,
        @Query("sort") sortBy: String?,
    ) : List<Map<String, Any?>>

    @GET("/comic/{HID}/chapters?lang=en&tachiyomi=true")
    @Headers("Referer: https://comick.app/", "Origin: https://comick.app/", "X-Requested-With: eu.kanade.tachiyomi", "User-Agent: Android", "Connection: keep-alive")
    suspend fun getChapterList(@Path("HID") id: String, @Query("limit") limit: Int, @Query("page") page: Int) : Map<String, Any?>

    @GET("/chapter/{HID}/get_images?tachiyomi=true")
    @Headers("Referer: https://comick.app/", "Origin: https://comick.app/", "X-Requested-With: eu.kanade.tachiyomi", "User-Agent: Android", "Connection: keep-alive")
    suspend fun getChapterPagesInfo(@Path("HID") chapterId: String) : List<Map<String,Any>>?
}