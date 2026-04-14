package com.creesh.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.creesh.app.R
import com.creesh.app.databinding.FragmentHomeBinding
import com.creesh.app.viewmodel.RecipeViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecipeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadHiddenGems()

        // Receta hero
        viewModel.hiddenGems.observe(viewLifecycleOwner) { meals ->
            if (meals.isNotEmpty()) {
                showHeroContent()
                val meal = meals.first()
                Glide.with(this)
                    .load(meal.thumbnail)
                    .centerCrop()
                    .into(binding.ivHeroImage)
                binding.tvHeroRecipeName.text = meal.name
                binding.cardHero.setOnClickListener {
                    viewModel.setSelectedMeal(meal)
                    findNavController().navigate(R.id.action_homeFragment_to_recipeDetailFragment)
                }
            }
        }

        // Error: mostrar overlay en el hero con mensaje y botón de reintentar
        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (!errorMsg.isNullOrEmpty() && viewModel.hiddenGems.value.isNullOrEmpty()) {
                showHeroError(errorMsg)
            }
        }

        binding.btnRetry.setOnClickListener {
            showHeroLoading()
            viewModel.loadHiddenGems()
        }

        binding.btnDiscover.setOnClickListener {
            findNavController().navigate(R.id.discoverFragment)
        }
        binding.btnCommunities.setOnClickListener {
            findNavController().navigate(R.id.communitiesFragment)
        }
        binding.btnUploadRecipe.setOnClickListener {
            findNavController().navigate(R.id.uploadRecipeFragment)
        }
        binding.btnFavorites.setOnClickListener {
            findNavController().navigate(R.id.favoritesFragment)
        }
    }

    private fun showHeroContent() {
        binding.layoutHeroError.visibility   = View.GONE
        binding.layoutHeroContent.visibility = View.VISIBLE
    }

    private fun showHeroError(message: String) {
        binding.layoutHeroError.visibility   = View.VISIBLE
        binding.layoutHeroContent.visibility = View.GONE
        binding.tvHeroErrorMessage.text      = message
    }

    private fun showHeroLoading() {
        binding.layoutHeroError.visibility   = View.GONE
        binding.layoutHeroContent.visibility = View.VISIBLE
        binding.tvHeroRecipeName.text        = ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
