import android.graphics.Bitmap
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url


interface MangaDexApiService {
    @GET("manga")
    suspend fun mangaSearchSimple(@Query("title") title: String): Map<String, Any?>

    @GET("/manga/{mangaId}?includes[]=cover_art")
    suspend fun getMangaCoverUrl(@Path("mangaId") mangaId: String): Map<String, Any?>

    @GET
    suspend fun downloadFile(@Url url: String): Map<String, Any?>

    @GET("/at-home/server/{cInfo}")
    suspend fun getChapterInfo(@Path("cInfo") chapterInfo: String): Bitmap

}

