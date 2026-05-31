package com.creesh.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.creesh.app.R
import com.creesh.app.adapters.FavoriteAdapter
import com.creesh.app.databinding.FragmentFavoritesBinding
import com.creesh.app.viewmodel.RecipeViewModel

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecipeViewModel by activityViewModels()

    private lateinit var adapter: FavoriteAdapter
    private var clickedFavorite = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FavoriteAdapter(emptyList()) { item ->
            clickedFavorite = true
            viewModel.getMealById(item.mealId)
        }

        binding.rvFavorites.layoutManager = GridLayoutManager(context, 2)
        binding.rvFavorites.adapter = adapter

        viewModel.favorites.observe(viewLifecycleOwner) { favorites ->
            if (favorites.isEmpty()) {
                binding.tvEmptyFavorites.visibility = View.VISIBLE
                binding.rvFavorites.visibility = View.GONE
            } else {
                binding.tvEmptyFavorites.visibility = View.GONE
                binding.rvFavorites.visibility = View.VISIBLE
                adapter.updateList(favorites)
            }
        }

        viewModel.selectedMeal.observe(viewLifecycleOwner) { meal ->
            if (clickedFavorite && meal != null) {
                clickedFavorite = false
                findNavController().navigate(R.id.action_favoritesFragment_to_recipeDetailFragment)
            }
        }

        viewModel.loadFavorites()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
