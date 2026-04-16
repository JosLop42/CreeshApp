package com.creesh.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.creesh.app.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: sección de siguiendo y favoritos pendiente
        binding.tvNoFollowing.visibility = View.VISIBLE
        binding.rvFollowingChefs.visibility = View.GONE

        binding.itemChangePassword.setOnClickListener {
            Toast.makeText(context, "Cambiar contraseña", Toast.LENGTH_SHORT).show()
        }
        binding.itemEmailPreferences.setOnClickListener {
            Toast.makeText(context, "Preferencias de email", Toast.LENGTH_SHORT).show()
        }
        binding.itemPrivacyPolicy.setOnClickListener {
            Toast.makeText(context, "Política de privacidad", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
