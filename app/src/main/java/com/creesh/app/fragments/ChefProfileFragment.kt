package com.creesh.app.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.creesh.app.R
import com.creesh.app.adapters.RecipeAdapter
import com.creesh.app.databinding.FragmentChefProfileBinding
import com.creesh.app.viewmodel.RecipeViewModel
import com.creesh.app.viewmodel.SocialViewModel

class ChefProfileFragment : Fragment() {

    private var _binding: FragmentChefProfileBinding? = null
    private val binding get() = _binding!!
    private val socialViewModel: SocialViewModel by activityViewModels()
    private val recipeViewModel: RecipeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChefProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        val recipesAdapter = RecipeAdapter { meal ->
            recipeViewModel.setSelectedMeal(meal)
            findNavController().navigate(R.id.action_chefProfileFragment_to_recipeDetailFragment)
        }
        binding.rvChefRecipes.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = recipesAdapter
        }

        socialViewModel.selectedChef.observe(viewLifecycleOwner) { chef ->
            chef ?: return@observe

            binding.tvChefName.text       = chef.fullName
            binding.tvChefUsername.text   = chef.username
            binding.tvChefSpecialty.text  = chef.specialty
            binding.tvChefBio.text        = chef.bio
            binding.tvChefCountry.text    = "📍 ${chef.country}"
            binding.tvFollowersCount.text = formatCount(chef.followerCount)
            binding.tvRecipesCount.text   = chef.recipeCount.toString()

            Glide.with(this)
                .load(chef.photoUrl)
                .circleCrop()
                .placeholder(R.drawable.circle_bg_orange)
                .into(binding.ivChefPhoto)

            updateFollowButton(socialViewModel.isFollowing(chef.id))
            binding.btnFollow.setOnClickListener {
                socialViewModel.toggleFollow(chef.id)
                updateFollowButton(socialViewModel.isFollowing(chef.id))
            }

            // Cargar recetas relacionadas con la especialidad del cocinero
            recipeViewModel.filterByCategory(categoryForSpecialty(chef.specialty))
        }

        recipeViewModel.searchResults.observe(viewLifecycleOwner) { meals ->
            recipesAdapter.submitList(meals.take(10))
        }
    }

    private fun updateFollowButton(isFollowing: Boolean) {
        if (isFollowing) {
            binding.btnFollow.text                = "Siguiendo"
            binding.btnFollow.backgroundTintList  = ColorStateList.valueOf(Color.parseColor("#F7F7F7"))
            binding.btnFollow.setTextColor(Color.parseColor("#F4831F"))
        } else {
            binding.btnFollow.text                = "Seguir"
            binding.btnFollow.backgroundTintList  = ColorStateList.valueOf(Color.WHITE)
            binding.btnFollow.setTextColor(Color.parseColor("#F4831F"))
        }
    }

    private fun formatCount(count: Int): String = when {
        count >= 1000 -> "${count / 1000}.${(count % 1000) / 100}k"
        else -> count.toString()
    }

    private fun categoryForSpecialty(specialty: String): String = when {
        specialty.contains("Mediterr")  -> "Seafood"
        specialty.contains("Asiát")     -> "Chicken"
        specialty.contains("Repost")    -> "Dessert"
        specialty.contains("Mexic")     -> "Beef"
        specialty.contains("Italian")   -> "Pasta"
        specialty.contains("Molecul")   -> "Lamb"
        specialty.contains("Vegetar")   -> "Vegetarian"
        specialty.contains("BBQ")       -> "Beef"
        specialty.contains("Franc")     -> "Side"
        specialty.contains("Peru")      -> "Chicken"
        specialty.contains("Street")    -> "Miscellaneous"
        else                            -> "Chicken"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
