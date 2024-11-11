package com.example.poomagnet.manganatoService

import android.content.Context
import com.example.poomagnet.downloadService.DownloadService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object natoRepoMod {
    @Provides
    @Singleton
    fun provideNatoRepo(context: Context, downloadService: DownloadService): MangaNatoRepository {
        return MangaNatoRepository(context, downloadService)
    }
}