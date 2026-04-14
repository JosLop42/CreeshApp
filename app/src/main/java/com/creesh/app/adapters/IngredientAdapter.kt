package com.creesh.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.creesh.app.databinding.ItemIngredientBinding

class IngredientAdapter(
    private val ingredients: List<Pair<String, String>>
) : RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val binding = ItemIngredientBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return IngredientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(ingredients[position])
    }

    override fun getItemCount() = ingredients.size

    inner class IngredientViewHolder(private val binding: ItemIngredientBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(ingredient: Pair<String, String>) {
            binding.tvIngredientName.text = ingredient.first
            binding.tvIngredientMeasure.text = ingredient.second
        }
    }
}
