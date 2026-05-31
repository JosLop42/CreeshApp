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

        // Búsqueda desde Home
        arguments?.getString("search_query")?.takeIf { it.isNotBlank() }?.let { query ->
            binding.searchView.setQuery(query, true)
        }

        binding.btnRetry.setOnClickListener {
            hideEmptyStates()
            viewModel.loadDiscoverRecipes()
            viewModel.loadHiddenGems()
        }

        binding.swipeRefresh.setOnRefreshListener {
            hideEmptyStates()
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
            if (viewModel.activeCommunity.value == null) {
                discoverAdapter.submitList(meals)
            }
            binding.swipeRefresh.isRefreshing = false
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { meals ->
            binding.swipeRefresh.isRefreshing = false
            val inCommunity = viewModel.activeCommunity.value != null
            val query = binding.searchView.query
            when {
                inCommunity -> {
                    discoverAdapter.submitList(meals)
                    binding.layoutNoResults.visibility   = View.GONE
                    binding.rvDiscoverRecipes.visibility = View.VISIBLE
                }
                !query.isNullOrBlank() -> {
                    discoverAdapter.submitList(meals)
                    binding.layoutNoResults.visibility   = if (meals.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvDiscoverRecipes.visibility = if (meals.isEmpty()) View.GONE else View.VISIBLE
                }
                meals.isNotEmpty() -> discoverAdapter.submitList(meals)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty() && viewModel.randomMeals.value.isNullOrEmpty()) {
                binding.layoutNoConnection.visibility = View.VISIBLE
            }
        }

        viewModel.activeCommunity.observe(viewLifecycleOwner) { community ->
            if (community != null) {
                binding.tvSectionDiscover.text        = "Recetas de: $community"
                binding.layoutHiddenGems.visibility   = View.GONE
                binding.rvHiddenGems.visibility       = View.GONE
            } else {
                binding.tvSectionDiscover.text        = "Descubrir recetas"
                binding.layoutHiddenGems.visibility   = View.VISIBLE
                binding.rvHiddenGems.visibility       = View.VISIBLE
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

    private fun hideEmptyStates() {
        binding.layoutNoResults.visibility    = View.GONE
        binding.layoutNoConnection.visibility = View.GONE
        binding.rvDiscoverRecipes.visibility  = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
