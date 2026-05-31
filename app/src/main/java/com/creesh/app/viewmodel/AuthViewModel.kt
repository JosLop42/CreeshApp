package com.creesh.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creesh.app.api.SupabaseAuthClient
import com.creesh.app.api.SupabaseAuthApi
import com.creesh.app.api.models.AuthRequest
import com.creesh.app.utils.SessionManager
import kotlinx.coroutines.launch
import java.net.UnknownHostException

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val userId: String) : AuthState()
    object NeedsEmailConfirmation : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val authApi = SupabaseAuthClient.retrofit.create(SupabaseAuthApi::class.java)

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    fun login(email: String, password: String) {
        if (!validate(email, password)) return
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = authApi.signIn("password", AuthRequest(email.trim(), password))
                if (response.isSuccessful) {
                    val body = response.body()
                    val token = body?.accessToken
                    val userId = body?.user?.id
                    if (token != null && userId != null) {
                        SessionManager.saveSession(userId, token, body?.user?.email ?: email.trim(), body?.refreshToken)
                        _authState.value = AuthState.Success(userId)
                    } else {
                        _authState.value = AuthState.Error("Respuesta inesperada del servidor")
                    }
                } else {
                    _authState.value = AuthState.Error(errorMessage(response.code()))
                }
            } catch (e: UnknownHostException) {
                _authState.value = AuthState.Error("Sin conexión a internet")
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error al conectar con el servidor")
            }
        }
    }

    fun register(email: String, password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            _authState.value = AuthState.Error("Las contraseñas no coinciden")
            return
        }
        if (!validate(email, password)) return
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = authApi.signUp(AuthRequest(email.trim(), password))
                if (response.isSuccessful) {
                    val body = response.body()
                    val token = body?.accessToken
                    val userId = body?.user?.id
                    when {
                        token != null && userId != null -> {
                            SessionManager.saveSession(userId, token, body?.user?.email ?: email.trim(), body?.refreshToken)
                            _authState.value = AuthState.Success(userId)
                        }
                        body?.id != null -> {
                            // Email confirmation required
                            _authState.value = AuthState.NeedsEmailConfirmation
                        }
                        else -> {
                            _authState.value = AuthState.Error("Respuesta inesperada del servidor")
                        }
                    }
                } else {
                    _authState.value = AuthState.Error(errorMessage(response.code()))
                }
            } catch (e: UnknownHostException) {
                _authState.value = AuthState.Error("Sin conexión a internet")
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error al conectar con el servidor")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    private fun validate(email: String, password: String): Boolean {
        return when {
            email.isBlank() -> {
                _authState.value = AuthState.Error("Ingresa tu email"); false
            }
            password.length < 6 -> {
                _authState.value = AuthState.Error("La contraseña debe tener al menos 6 caracteres"); false
            }
            else -> true
        }
    }

    private fun errorMessage(code: Int) = when (code) {
        400 -> "Credenciales inválidas"
        422 -> "Email ya registrado o datos inválidos"
        429 -> "Demasiados intentos, espera un momento"
        else -> "Error del servidor ($code)"
    }
}
