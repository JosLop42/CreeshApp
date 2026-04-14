package com.creesh.app.api

import com.creesh.app.api.models.TranslationResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TranslationApi {
    @GET("get")
    suspend fun translate(
        @Query("q") text: String,
        @Query("langpair") langPair: String = "en|es"
    ): TranslationResponse
}
