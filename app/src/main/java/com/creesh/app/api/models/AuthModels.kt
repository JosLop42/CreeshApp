package com.creesh.app.api.models

import com.google.gson.annotations.SerializedName

data class AuthRequest(
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String
)

data class AuthResponse(
    @SerializedName("access_token")       val accessToken: String?,
    @SerializedName("refresh_token")      val refreshToken: String?,
    @SerializedName("user")               val user: AuthUser?,
    // Sign-up con confirmación de email (sin access_token)
    @SerializedName("id")                 val id: String?,
    @SerializedName("email_confirmed_at") val emailConfirmedAt: String?,
    // Errores
    @SerializedName("error")              val error: String?,
    @SerializedName("error_description")  val errorDescription: String?,
    @SerializedName("msg")                val msg: String?
)

data class AuthUser(
    @SerializedName("id")    val id: String,
    @SerializedName("email") val email: String?
)
