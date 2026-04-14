package com.creesh.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.creesh.app.R
import com.creesh.app.adapters.Community
import com.creesh.app.adapters.CommunityAdapter
import com.creesh.app.databinding.FragmentCommunitiesBinding
import com.creesh.app.viewmodel.RecipeViewModel

class CommunitiesFragment : Fragment() {

    private var _binding: FragmentCommunitiesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecipeViewModel by activityViewModels()

    private val communities = listOf(
        Community("Gym Rats", "12.4k miembros", "💪"),
        Community("Vegano", "8.9k miembros", "🌱"),
        Community("Vegetariano", "7.2k miembros", "🥦"),
        Community("Amantes de la Carne", "15.1k miembros", "🥩"),
        Community("Mariscos", "5.3k miembros", "🦞"),
        Community("Postres & Dulces", "9.7k miembros", "🍰"),
        Community("Pasta & Italiana", "11.2k miembros", "🍝"),
        Community("Cocina Asiática", "13.8k miembros", "🍜"),
        Community("Variado", "3.1k miembros", "🍽️")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunitiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = CommunityAdapter(communities) { community ->
            val category = when (community.name) {
                "Vegano"               -> "Vegan"
                "Vegetariano"          -> "Vegetarian"
                "Amantes de la Carne"  -> "Beef"
                "Mariscos"             -> "Seafood"
                "Postres & Dulces"     -> "Dessert"
                "Pasta & Italiana"     -> "Pasta"
                "Cocina Asiática"      -> "Chinese"
                "Gym Rats"             -> "Chicken"
                else                   -> "Miscellaneous"
            }
            viewModel.filterByCategory(category, community.name)
            findNavController().navigate(R.id.action_communitiesFragment_to_discoverFragment)
        }

        binding.rvCommunities.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
