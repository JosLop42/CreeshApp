package com.creesh.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.creesh.app.R
import com.creesh.app.adapters.ChefAdapter
import com.creesh.app.adapters.MyRecipeAdapter
import com.creesh.app.databinding.FragmentProfileBinding
import com.creesh.app.utils.SessionManager
import com.creesh.app.viewmodel.RecipeViewModel
import com.creesh.app.viewmodel.SocialViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val recipeViewModel: RecipeViewModel by activityViewModels()
    private val socialViewModel: SocialViewModel by activityViewModels()
    private lateinit var chefAdapter: ChefAdapter
    private lateinit var myRecipeAdapter: MyRecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserInfo()
        recipeViewModel.loadFavorites()

        binding.btnEditName.setOnClickListener { showEditNameDialog() }

        // Mis recetas
        myRecipeAdapter = MyRecipeAdapter(emptyList()) { recipe ->
            recipeViewModel.setSelectedUserRecipe(recipe)
            findNavController().navigate(R.id.action_profileFragment_to_myRecipeDetailFragment)
        }
        binding.rvMyRecipes.layoutManager = GridLayoutManager(context, 2)
        binding.rvMyRecipes.adapter = myRecipeAdapter
        recipeViewModel.loadMyRecipes()

        recipeViewModel.myRecipes.observe(viewLifecycleOwner) { recipes ->
            binding.tvRecipesCount.text = recipes.size.toString()
            if (recipes.isEmpty()) {
                binding.tvNoRecipes.visibility = View.VISIBLE
                binding.rvMyRecipes.visibility = View.GONE
            } else {
                binding.tvNoRecipes.visibility = View.GONE
                binding.rvMyRecipes.visibility = View.VISIBLE
                myRecipeAdapter.updateList(recipes)
            }
        }

        recipeViewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrBlank()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            }
        }

        // Cocineros que sigo
        chefAdapter = ChefAdapter { chef ->
            socialViewModel.setSelectedChef(chef)
            findNavController().navigate(R.id.action_profileFragment_to_chefProfileFragment)
        }
        binding.rvFollowingChefs.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFollowingChefs.adapter = chefAdapter

        socialViewModel.followedChefIds.observe(viewLifecycleOwner) { refreshFollowing() }
        socialViewModel.chefs.observe(viewLifecycleOwner) { refreshFollowing() }

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

    private fun setupUserInfo() {
        val email = SessionManager.getEmail() ?: return
        val savedName = SessionManager.getDisplayName()
        val namePart  = email.substringBefore("@")

        val displayName = savedName ?: namePart
            .replace(".", " ").replace("_", " ")
            .split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

        val initials = displayName.split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().uppercase() }

        binding.tvUserName.text       = displayName
        binding.tvUserEmail.text      = email
        binding.tvAvatarInitials.text = initials.ifEmpty { displayName.take(2).uppercase() }
    }

    private fun showEditNameDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 20, 60, 0)
        }
        val etName = EditText(requireContext()).apply {
            hint = "Nombre completo"
            setText(SessionManager.getDisplayName() ?: "")
        }
        layout.addView(etName)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar nombre")
            .setView(layout)
            .setPositiveButton("Guardar") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isNotBlank()) {
                    recipeViewModel.updateDisplayName(name)
                    binding.tvUserName.text = name
                    val initials = name.split(" ").filter { it.isNotEmpty() }
                        .take(2).joinToString("") { it.first().uppercase() }
                    binding.tvAvatarInitials.text = initials.ifEmpty { name.take(2).uppercase() }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun refreshFollowing() {
        val ids = socialViewModel.followedChefIds.value ?: emptySet()
        val allChefs = socialViewModel.chefs.value ?: emptyList()
        val followedChefs = allChefs.filter { it.id in ids }
        binding.tvFollowingCount.text = followedChefs.size.toString()
        if (followedChefs.isEmpty()) {
            binding.tvNoFollowing.visibility = View.VISIBLE
            binding.rvFollowingChefs.visibility = View.GONE
        } else {
            binding.tvNoFollowing.visibility = View.GONE
            binding.rvFollowingChefs.visibility = View.VISIBLE
            chefAdapter.submitList(followedChefs)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
