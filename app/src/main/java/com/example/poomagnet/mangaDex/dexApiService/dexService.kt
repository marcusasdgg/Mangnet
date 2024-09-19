import android.graphics.Bitmap
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Url


interface MangaDexApiService {
    @GET("manga?limit=50")
    suspend fun mangaSearchSimple(
        @Query("title") title: String,
        @Query("offset") offset: Int,
        @Query("includes[]") includes: List<String>,
        @Query("includedTags[]") included: List<String>?,
        @Query("excludedTags[]") excludes: List<String>?,
        @Query("publicationDemographic[]") demo: List<String>?,
        @Query("contentRating[]") rating: List<String>?,
        @QueryMap queries: Map<String, String>
    ): Map<String, Any?>

    @GET("/manga/{mangaId}?includes[]=cover_art?limit=50")
    suspend fun getMangaCoverUrl(@Path("mangaId") mangaId: String): Map<String, Any?>

    @GET
    suspend fun downloadFile(@Url url: String): Response<ResponseBody>

    @GET("/at-home/server/{cInfo}")
    suspend fun getChapterPagesInfo(@Path("cInfo") id: String): Map<String,Any?>


    @GET("/manga/{id}/feed?limit=100")
    suspend fun getChapterList(@Path("id") mangaId: String, @Query("offset") offset: Int): Map<String,Any?>

    @GET("manga/tag")
    suspend fun getTagList(): Map<String,Any?>

}

