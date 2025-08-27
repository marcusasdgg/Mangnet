package com.example.poomagnet.comickService

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyStore
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import okhttp3.OkHttpClient

object retrofitInstance {

    private fun getTrustManager(): X509TrustManager {
        val trustManagerFactory = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm()
        )
        trustManagerFactory.init(null as KeyStore?)
        val trustManagers: Array<TrustManager> = trustManagerFactory.trustManagers
        return trustManagers.first { it is X509TrustManager } as X509TrustManager
    }

    private fun getTLS13Client(): OkHttpClient {
        val trustManager = getTrustManager()
        val sslContext = SSLContext.getInstance("TLSv1.3")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), null)

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .build()
    }

    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .client(getTLS13Client())
        .baseUrl("https://api.comick.fun")
        .build()

    val api: mickService = retrofit.create(mickService::class.java)
}