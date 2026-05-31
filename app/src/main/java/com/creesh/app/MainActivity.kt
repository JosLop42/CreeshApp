package com.creesh.app

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.creesh.app.api.SupabaseAuthClient
import com.creesh.app.api.SupabaseAuthApi
import com.creesh.app.databinding.ActivityMainBinding
import com.creesh.app.utils.SessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionManager.init(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.logoutItem -> {
                    showLogoutDialog()
                    false
                }
                else -> NavigationUI.onNavDestinationSelected(item, navController)
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val hideNav = destination.id in setOf(
                R.id.recipeDetailFragment, R.id.chefProfileFragment,
                R.id.settingsFragment, R.id.loginFragment,
                R.id.registerFragment, R.id.myRecipeDetailFragment
            )
            binding.bottomNavigation.visibility = if (hideNav) View.GONE else View.VISIBLE

            if (!hideNav) {
                val checkedId = when (destination.id) {
                    R.id.profileFragment -> R.id.profileFragment
                    else                 -> R.id.homeFragment
                }
                binding.bottomNavigation.menu.findItem(checkedId)?.isChecked = true
            }
        }

        SessionManager.onSessionExpired = {
            Toast.makeText(this, "Sesión expirada, inicia sesión de nuevo", Toast.LENGTH_LONG).show()
            navController.navigate(
                R.id.loginFragment,
                null,
                NavOptions.Builder().setPopUpTo(R.id.homeFragment, true).build()
            )
        }

        if (!SessionManager.isLoggedIn()) {
            navController.navigate(
                R.id.loginFragment,
                null,
                NavOptions.Builder()
                    .setPopUpTo(R.id.homeFragment, true)
                    .build()
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (SessionManager.isSessionExpired()) {
            SessionManager.clearSession()
            Toast.makeText(this, "Sesión expirada por inactividad", Toast.LENGTH_LONG).show()
            navController.navigate(
                R.id.loginFragment,
                null,
                NavOptions.Builder().setPopUpTo(R.id.homeFragment, true).build()
            )
            return
        }
        if (SessionManager.isLoggedIn()) {
            tryRefreshToken()
        }
    }

    private fun tryRefreshToken() {
        val refreshToken = SessionManager.getRefreshToken() ?: return
        lifecycleScope.launch {
            try {
                val api = SupabaseAuthClient.retrofit.create(SupabaseAuthApi::class.java)
                val response = api.refreshToken("refresh_token", mapOf("refresh_token" to refreshToken))
                if (response.isSuccessful) {
                    val body = response.body()
                    val newToken  = body?.accessToken ?: return@launch
                    val newRefresh = body.refreshToken
                    val userId    = body.user?.id ?: SessionManager.getUserId() ?: return@launch
                    val email     = body.user?.email ?: SessionManager.getEmail() ?: ""
                    SessionManager.saveSession(userId, newToken, email, newRefresh)
                }
            } catch (e: Exception) { /* silencioso — si falla, se usa el token actual */ }
        }
    }

    override fun onPause() {
        super.onPause()
        if (SessionManager.isLoggedIn()) SessionManager.saveLastActiveTime()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Cerrar sesión") { _, _ ->
                SessionManager.clearSession()
                navController.navigate(
                    R.id.loginFragment,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.homeFragment, true)
                        .build()
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
