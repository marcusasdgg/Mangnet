package com.example.poomagnet.manganatoService

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface natoService {
    @GET("advanced_search?s=all")
    suspend fun mangaSearchSimple(
        @Query("keyw") title: String?,
        @Query("page") offset: Int,
        @Query("orby") orderBy: String?,
        @Query("g_i") includedTags: String?,
        @Query("g_e") excludedTags: String?,
        @Query("sts") status: String?
    ): String

    @GET("https://natomanga.com/genre-all")
    suspend fun tagInit(): String

    @GET
    suspend fun getInfo(@Url mangaUrl : String): String

    @GET("https://chapmaneganato.to/{mangaId}/{chapterId}")
    suspend fun getChapterPages(@Path("mangaId") mangaId: String, @Path("chapterId") chapterId: String) : String

}
