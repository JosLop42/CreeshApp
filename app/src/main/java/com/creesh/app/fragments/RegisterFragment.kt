package com.creesh.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.creesh.app.R
import com.creesh.app.databinding.FragmentRegisterBinding
import com.creesh.app.viewmodel.AuthState
import com.creesh.app.viewmodel.AuthViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnRegister.setOnClickListener {
            val email    = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirm  = binding.etConfirmPassword.text.toString()
            authViewModel.register(email, password, confirm)
        }

        binding.tvGoLogin.setOnClickListener { findNavController().navigateUp() }

        authViewModel.authState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.visibility  = if (state is AuthState.Loading) View.VISIBLE else View.GONE
            binding.btnRegister.isEnabled   = state !is AuthState.Loading
            binding.tvError.visibility      = View.GONE

            when (state) {
                is AuthState.Success -> navigateToHome()
                is AuthState.NeedsEmailConfirmation -> showConfirmEmailDialog()
                is AuthState.Error -> {
                    binding.tvError.text       = state.message
                    binding.tvError.visibility = View.VISIBLE
                }
                else -> Unit
            }
        }
    }

    private fun navigateToHome() {
        findNavController().navigate(
            R.id.homeFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.loginFragment, true)
                .build()
        )
    }

    private fun showConfirmEmailDialog() {
        authViewModel.resetState()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirma tu email")
            .setMessage("Te enviamos un enlace de confirmación. Revisa tu bandeja de entrada y haz clic en el enlace para activar tu cuenta.")
            .setPositiveButton("Entendido") { _, _ ->
                findNavController().navigateUp()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
