package com.creesh.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.creesh.app.R
import com.creesh.app.adapters.ChefAdapter
import com.creesh.app.databinding.FragmentProfileBinding
import com.creesh.app.viewmodel.RecipeViewModel
import com.creesh.app.viewmodel.SocialViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val recipeViewModel: RecipeViewModel by activityViewModels()
    private val socialViewModel: SocialViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chefAdapter = ChefAdapter { chef ->
            socialViewModel.setSelectedChef(chef)
            findNavController().navigate(R.id.action_profileFragment_to_chefProfileFragment)
        }
        binding.rvFollowingChefs.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = chefAdapter
        }

        // Actualizar contadores y lista de siguiendo
        fun refreshFollowing(ids: Set<String>) {
            binding.tvFollowingCount.text = ids.size.toString()
            val followed = (socialViewModel.chefs.value ?: emptyList()).filter { it.id in ids }
            chefAdapter.submitList(followed)
            binding.tvNoFollowing.visibility  = if (followed.isEmpty()) View.VISIBLE else View.GONE
            binding.rvFollowingChefs.visibility = if (followed.isEmpty()) View.GONE else View.VISIBLE
        }

        socialViewModel.followedChefIds.observe(viewLifecycleOwner) { ids ->
            refreshFollowing(ids)
        }
        socialViewModel.chefs.observe(viewLifecycleOwner) {
            refreshFollowing(socialViewModel.followedChefIds.value ?: emptySet())
        }

        recipeViewModel.favorites.observe(viewLifecycleOwner) { favs ->
            binding.tvFavoritesCount.text = favs.size.toString()
        }

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
