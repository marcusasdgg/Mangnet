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
    suspend fun mangaSearchSimple(@Query("title") title: String, @Query("offset") offset: Int, @Query("includes[]") includes: List<String>, @Query("includedTags[]") included: List<String>?, @Query("excludedTags[]") excludes: List<String>?, @QueryMap queries: Map<String, String>): Map<String, Any?>

    @GET("/manga/{mangaId}?includes[]=cover_art")
    suspend fun getMangaCoverUrl(@Path("mangaId") mangaId: String): Map<String, Any?>

    @GET
    suspend fun downloadFile(@Url url: String): Response<ResponseBody>

    @GET("/at-home/server/{cInfo}")
    suspend fun getChapterInfo(@Path("cInfo") chapterInfo: String): Bitmap

    @GET("manga/tag")
    suspend fun getTagList(): Map<String,Any?>
}

