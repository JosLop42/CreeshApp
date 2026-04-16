package com.creesh.app.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

//Crea y configura el cliente HTTP
object RetrofitClient {

    private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS) //max 8s para conexión
        .readTimeout(10, TimeUnit.SECONDS) //max 10s para recibir respuesta
        .writeTimeout(8, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val api: MealDbApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()) //JSON → Kotlin
            .build()
            .create(MealDbApi::class.java)
    }
}
