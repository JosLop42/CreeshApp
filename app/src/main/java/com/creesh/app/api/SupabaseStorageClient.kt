package com.creesh.app.api

import com.creesh.app.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor

object SupabaseStorageClient {

    private const val KEY        = "sb_publishable_m4xncBXcYAFyzxr73YCpiw_3FmXb70i"
    private const val UPLOAD_URL = "https://thvktcqdmufbdvorrpgh.supabase.co/storage/v1/object/recipe-images/"
    private const val PUBLIC_URL = "https://thvktcqdmufbdvorrpgh.supabase.co/storage/v1/object/public/recipe-images/"

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        })
        .build()

    data class UploadResult(val url: String? = null, val error: String? = null)

    suspend fun uploadImage(filePath: String, imageBytes: ByteArray, mimeType: String): UploadResult =
        withContext(Dispatchers.IO) {
            try {
                val token = SessionManager.getToken()
                if (token == null) return@withContext UploadResult(error = "Sin sesión activa")

                val body = imageBytes.toRequestBody(mimeType.toMediaType())
                val request = Request.Builder()
                    .url("$UPLOAD_URL$filePath")
                    .post(body)
                    .addHeader("apikey", KEY)
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("x-upsert", "true")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    UploadResult(url = PUBLIC_URL + filePath)
                } else {
                    UploadResult(error = "HTTP ${response.code}: $responseBody")
                }
            } catch (e: Exception) {
                UploadResult(error = "Excepción: ${e.message}")
            }
        }
}
