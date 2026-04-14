package com.creesh.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.creesh.app.R
import com.creesh.app.adapters.RecipeAdapter
import com.creesh.app.adapters.RecipeHorizontalAdapter
import com.creesh.app.databinding.FragmentDiscoverBinding
import com.creesh.app.viewmodel.RecipeViewModel

class DiscoverFragment : Fragment() {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecipeViewModel by activityViewModels()

    private lateinit var hiddenGemsAdapter: RecipeHorizontalAdapter
    private lateinit var discoverAdapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupObservers()
        setupSearch()

        if (viewModel.activeCommunity.value == null) {
            viewModel.loadHiddenGems()
            viewModel.loadDiscoverRecipes()
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.clearCommunityFilter()
            viewModel.loadDiscoverRecipes()
            viewModel.loadHiddenGems()
        }
    }

    private fun setupAdapters() {
        hiddenGemsAdapter = RecipeHorizontalAdapter { meal ->
            viewModel.setSelectedMeal(meal)
            findNavController().navigate(R.id.action_discoverFragment_to_recipeDetailFragment)
        }
        binding.rvHiddenGems.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = hiddenGemsAdapter
        }

        discoverAdapter = RecipeAdapter { meal ->
            viewModel.setSelectedMeal(meal)
            findNavController().navigate(R.id.action_discoverFragment_to_recipeDetailFragment)
        }
        binding.rvDiscoverRecipes.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = discoverAdapter
        }
    }

    private fun setupObservers() {
        viewModel.hiddenGems.observe(viewLifecycleOwner) { meals ->
            hiddenGemsAdapter.submitList(meals)
        }

        viewModel.randomMeals.observe(viewLifecycleOwner) { meals ->
            discoverAdapter.submitList(meals)
            binding.swipeRefresh.isRefreshing = false
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { meals ->
            if (meals.isNotEmpty()) {
                discoverAdapter.submitList(meals)
            }
            binding.swipeRefresh.isRefreshing = false
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                android.widget.Toast.makeText(requireContext(), it, android.widget.Toast.LENGTH_LONG).show()
            }
        }

        viewModel.activeCommunity.observe(viewLifecycleOwner) { community ->
            if (community != null) {
                binding.tvSectionDiscover.text = "Recetas de: $community"
                binding.rvHiddenGems.visibility = View.GONE
            } else {
                binding.tvSectionDiscover.text = "Discover Recipes"
                binding.rvHiddenGems.visibility = View.VISIBLE
            }
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.takeIf { it.isNotBlank() }?.let {
                    viewModel.searchMeals(it)
                    binding.tvSectionDiscover.text = "Resultados para \"$it\""
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    binding.tvSectionDiscover.text = "Descubrir recetas"
                    viewModel.loadDiscoverRecipes()
                }
                return false
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
