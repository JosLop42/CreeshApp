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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.creesh.app.R
import com.creesh.app.adapters.IngredientAdapter
import com.creesh.app.api.models.Chef
import com.creesh.app.databinding.FragmentRecipeDetailBinding
import com.creesh.app.viewmodel.RecipeViewModel
import com.creesh.app.viewmodel.SocialViewModel

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecipeViewModel by activityViewModels()
    private val socialViewModel: SocialViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        viewModel.selectedMeal.observe(viewLifecycleOwner) { meal ->
            meal ?: return@observe

            // Si no tiene detalles completos, los pedimos por ID
            if (meal.instructions == null && meal.ingredient1 == null) {
                viewModel.getMealById(meal.id)
                return@observe
            }

            // Imagen (no se traduce)
            Glide.with(this)
                .load(meal.thumbnail)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.ivRecipeHeader)

            // Favorito (desactivado temporalmente)
            binding.btnFavorite.isEnabled = false

            // Contenido (mientras traduce muestra el original)
            binding.tvRecipeTitle.text    = meal.name
            binding.tvRecipeCategory.text = listOfNotNull(meal.category, meal.area?.let { "· $it" })
                .joinToString(" ")
            binding.tvInstructions.text   = "Traduciendo..."
            binding.rvIngredients.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = IngredientAdapter(meal.getIngredientList())
            }

            // Tags
            meal.tags?.let { tags ->
                val list = tags.split(",").filter { it.isNotBlank() }
                binding.tvTags.text       = list.joinToString("  ") { "#${it.trim()}" }
                binding.tvTags.visibility = View.VISIBLE
            } ?: run { binding.tvTags.visibility = View.GONE }

            // Tarjeta del cocinero
            val chef = socialViewModel.getChefForMeal(meal)
            if (chef != null) {
                showChefCard(chef)
            } else {
                // Esperar a que carguen los chefs
                socialViewModel.chefs.observe(viewLifecycleOwner) { chefs ->
                    if (chefs.isNotEmpty()) {
                        val c = socialViewModel.getChefForMeal(meal)
                        if (c != null) showChefCard(c)
                    }
                }
            }

            // Iniciar traducción
            viewModel.translateMeal(meal)
        }

        viewModel.translatedContent.observe(viewLifecycleOwner) { content ->
            content ?: return@observe
            binding.tvRecipeTitle.text  = content.name
            binding.tvInstructions.text = content.instructions
            binding.rvIngredients.adapter = IngredientAdapter(content.ingredients)
        }
    }

    private fun showChefCard(chef: Chef) {
        binding.cardChef.visibility       = View.VISIBLE
        binding.tvDetailChefName.text     = chef.fullName
        binding.tvDetailChefSpecialty.text = chef.specialty

        Glide.with(this)
            .load(chef.photoUrl)
            .circleCrop()
            .placeholder(R.drawable.circle_bg_orange)
            .into(binding.ivChefAvatar)

        updateDetailFollowButton(socialViewModel.isFollowing(chef.id))
        binding.btnDetailFollow.setOnClickListener {
            socialViewModel.toggleFollow(chef.id)
            updateDetailFollowButton(socialViewModel.isFollowing(chef.id))
        }

        // Tap en la tarjeta → perfil del cocinero
        binding.cardChef.setOnClickListener {
            socialViewModel.setSelectedChef(chef)
            findNavController().navigate(R.id.action_recipeDetailFragment_to_chefProfileFragment)
        }
    }

    private fun updateDetailFollowButton(isFollowing: Boolean) {
        if (isFollowing) {
            binding.btnDetailFollow.text = "Siguiendo"
            binding.btnDetailFollow.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#F7F7F7"))
            binding.btnDetailFollow.setTextColor(Color.parseColor("#F4831F"))
        } else {
            binding.btnDetailFollow.text = "Seguir"
            binding.btnDetailFollow.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#F4831F"))
            binding.btnDetailFollow.setTextColor(Color.WHITE)
        }
    }

    private fun updateFavoriteButton(isFav: Boolean) {
        binding.btnFavorite.setImageResource(
            if (isFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
