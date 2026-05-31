package com.creesh.app.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseClient {

    private const val URL = "https://thvktcqdmufbdvorrpgh.supabase.co/rest/v1/"
    private const val KEY = "sb_publishable_m4xncBXcYAFyzxr73YCpiw_3FmXb70i"

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val token   = com.creesh.app.utils.SessionManager.getToken()
            val builder = chain.request().newBuilder()
                .addHeader("apikey", KEY)
                .addHeader("Content-Type", "application/json")
            if (token != null) builder.addHeader("Authorization", "Bearer $token")
            val response = chain.proceed(builder.build())
            if (response.code == 401 && com.creesh.app.utils.SessionManager.isLoggedIn()) {
                com.creesh.app.utils.SessionManager.clearSession()
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    com.creesh.app.utils.SessionManager.onSessionExpired?.invoke()
                }
            }
            response
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
