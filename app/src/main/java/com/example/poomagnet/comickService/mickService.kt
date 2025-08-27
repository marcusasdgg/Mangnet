package com.example.poomagnet.comickService

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface mickService {
    @GET("advanced_search?s=all")
    suspend fun mangaSearchSimple(
        @Query("keyw") title: String?,
        @Query("page") offset: Int,
        @Query("orby") orderBy: String?,
        @Query("g_i") includedTags: String?,
        @Query("g_e") excludedTags: String?,
        @Query("sts") status: String?
    ): String

    @GET("https://manganato.com/genre-all")
    suspend fun tagInit(): String

    @GET
    suspend fun getInfo(@Url mangaUrl : String): String

    @GET("https://chapmanganato.to/{mangaId}/{chapterId}")
    suspend fun getChapterPages(@Path("mangaId") mangaId: String, @Path("chapterId") chapterId: String) : String
}