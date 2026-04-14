package com.creesh.app.api

import com.creesh.app.api.models.RandomUserResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface RandomUserApi {
    @GET("api/")
    suspend fun getUsers(
        @Query("results") results: Int = 12,
        @Query("seed")    seed: String = "creesh2024",
        @Query("inc")     include: String = "name,email,picture,login,location"
    ): RandomUserResponse
}
