package com.creesh.app

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.creesh.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Manejo manual para interceptar el botón "Salir"
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.logoutItem -> {
                    showLogoutDialog()
                    false   // no navegamos
                }
                else -> NavigationUI.onNavDestinationSelected(item, navController)
            }
        }

        // Sincronizar selección del bottom nav con el destino actual
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.recipeDetailFragment,
                R.id.chefProfileFragment,
                R.id.settingsFragment -> {
                    binding.bottomNavigation.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Cerrar sesión") { _, _ ->
                Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                navController.navigate(R.id.homeFragment)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
