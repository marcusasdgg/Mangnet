package com.example.poomagnet.comickService

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface mickService {
    // gets all tags slugified
    @GET("genre/")
    suspend fun getTagList() : Map<String, Any?>

    @GET("/v1.0/search/")
    suspend fun searchAllManga(
        @Query("q") search: String,
        @Query("genres") genres: List<String>?,
        @Query("excludes") genre_excludes: List<String>?,
        @Query("tags") tags: List<String>?,
        @Query("excluded-tags") excluded_tags: List<String>?,
        @Query("demographic") demo: List<Int>?,
        @Query("page") page: Int,
        @Query("content_rating") contentRating: String,
        @Query("sort") sortBy: String,
    )

    @GET("/comic/{HID}/chapters?lang=en")
    suspend fun getChapterList(@Path("HID") id: String, @Query("limit") limit: Int, @Query("page") page: Int)

    @GET("/chapter/{HID}?tachiyomi=true")
    suspend fun getChapterPagesInfo(@Path("HID") chapterId: String) : Map<String,Any>?
}