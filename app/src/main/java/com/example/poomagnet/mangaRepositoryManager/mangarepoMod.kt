package com.example.poomagnet.mangaRepositoryManager

import android.content.Context
import com.example.poomagnet.comickService.ComickRepository
import com.example.poomagnet.mangaDex.dexApiService.MangaDexRepository
import com.example.poomagnet.manganatoService.MangaNatoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object mangaRepoMod {
    @Provides
    @Singleton
    fun provideImageSearchRepository(repo: MangaDexRepository, context: Context, natoRepository: MangaNatoRepository, mickRepo: ComickRepository): MangaRepositoryManager {
        return MangaRepositoryManager(repo, natoRepository, mickRepo, context)
    }
}