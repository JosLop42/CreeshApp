package com.creesh.app.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface SupabaseStorageApi {

    @POST("object/recipe-images/{filePath}")
    suspend fun uploadImage(
        @Path("filePath", encoded = true) filePath: String,
        @Body body: RequestBody
    ): Response<ResponseBody>
}
