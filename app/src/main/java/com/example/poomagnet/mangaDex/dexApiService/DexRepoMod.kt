package com.example.poomagnet.mangaDex.dexApiService

import android.content.Context
import com.example.poomagnet.downloadService.DownloadService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DexRepoMod {
    @Provides
    @Singleton
    fun provideImageSearchRepository(context: Context, downloadService: DownloadService): MangaDexRepository {
        return MangaDexRepository(context, downloadService)
    }
}