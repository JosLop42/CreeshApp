package com.creesh.app.api

import com.creesh.app.api.models.AuthRequest
import com.creesh.app.api.models.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseAuthApi {

    @POST("signup")
    suspend fun signUp(@Body request: AuthRequest): Response<AuthResponse>

    @POST("token")
    suspend fun signIn(
        @Query("grant_type") grantType: String,
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @POST("token")
    suspend fun refreshToken(
        @Query("grant_type") grantType: String,
        @Body body: Map<String, String>
    ): Response<AuthResponse>
}
