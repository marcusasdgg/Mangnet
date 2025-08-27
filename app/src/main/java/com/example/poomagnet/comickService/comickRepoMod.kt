package com.example.poomagnet.comickService

import android.content.Context
import com.example.poomagnet.downloadService.DownloadService
import com.example.poomagnet.manganatoService.MangaNatoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object comickRepoMod {
    @Provides
    @Singleton
    fun provideComickRepo(context: Context, downloadService: DownloadService): ComickRepository {
        return ComickRepository(context, downloadService)
    }
}