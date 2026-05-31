package com.creesh.app.utils

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("creesh_session", Context.MODE_PRIVATE)
    }

    fun saveSession(userId: String, token: String, email: String, refreshToken: String? = null) {
        prefs.edit()
            .putString("user_id", userId)
            .putString("token", token)
            .putString("email", email)
            .apply()
        if (refreshToken != null) prefs.edit().putString("refresh_token", refreshToken).apply()
    }

    fun getUserId(): String?     = prefs.getString("user_id", null)
    fun getToken(): String?      = prefs.getString("token", null)
    fun getEmail(): String?      = prefs.getString("email", null)
    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)

    // Nombre visible del usuario (persiste entre sesiones)
    fun saveDisplayName(name: String) {
        val userId = getUserId() ?: return
        prefs.edit().putString("display_name_$userId", name).apply()
    }

    fun getDisplayName(): String? {
        val userId = getUserId() ?: return null
        return prefs.getString("display_name_$userId", null)
    }

    // Chefs seguidos (persiste entre sesiones del mismo usuario)
    fun saveFollowedChefs(userId: String, ids: Set<String>) {
        prefs.edit().putString("follows_$userId", ids.joinToString(",")).apply()
    }

    fun getFollowedChefs(userId: String): MutableSet<String> {
        val raw = prefs.getString("follows_$userId", "") ?: ""
        return if (raw.isBlank()) mutableSetOf() else raw.split(",").toMutableSet()
    }

    fun saveLastActiveTime() {
        prefs.edit().putLong("last_active", System.currentTimeMillis()).apply()
    }

    fun isSessionExpired(timeoutMinutes: Int = 30): Boolean {
        if (!isLoggedIn()) return false
        val lastActive = prefs.getLong("last_active", 0L)
        if (lastActive == 0L) return false
        return System.currentTimeMillis() - lastActive > timeoutMinutes * 60_000L
    }

    fun isLoggedIn(): Boolean = getUserId() != null && getToken() != null

    // Callback que MainActivity registra para redirigir a login cuando el token expira
    var onSessionExpired: (() -> Unit)? = null

    // Solo borra datos de sesión — los datos del usuario (follows, nombre) se conservan
    fun clearSession() {
        prefs.edit()
            .remove("user_id")
            .remove("token")
            .remove("email")
            .remove("last_active")
            .apply()
    }
}
