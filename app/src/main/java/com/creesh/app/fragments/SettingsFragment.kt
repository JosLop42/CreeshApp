package com.creesh.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.creesh.app.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.itemChangePassword.setOnClickListener {
            Toast.makeText(context, "Cambiar contraseña", Toast.LENGTH_SHORT).show()
        }

        binding.itemEmailPreferences.setOnClickListener {
            Toast.makeText(context, "Preferencias de correo", Toast.LENGTH_SHORT).show()
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(
                context,
                if (isChecked) "Modo oscuro activado" else "Modo claro activado",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.switchDataSaver.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(
                context,
                if (isChecked) "Ahorro de datos activado" else "Ahorro de datos desactivado",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.itemHelpCenter.setOnClickListener {
            Toast.makeText(context, "Centro de ayuda", Toast.LENGTH_SHORT).show()
        }

        binding.itemPrivacyPolicy.setOnClickListener {
            Toast.makeText(context, "Política de privacidad", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogOut.setOnClickListener {
            Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
