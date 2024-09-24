package com.example.poomagnet.App


import android.app.Application
import android.app.DownloadManager
import android.content.Context
import com.example.poomagnet.downloadService.DownloadService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application.applicationContext


}