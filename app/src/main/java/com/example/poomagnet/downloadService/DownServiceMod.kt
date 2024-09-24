package com.example.poomagnet.downloadService

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DownServiceMod {
    @Provides
    @Singleton
    fun provideDownloadService(@ApplicationContext context: Context): DownloadService {
        return DownloadService(context)
    }
}