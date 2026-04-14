package com.creesh.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.creesh.app.api.models.Meal
import com.creesh.app.databinding.ItemRecipeBinding

class RecipeAdapter(
    private val onItemClick: (Meal) -> Unit
) : ListAdapter<Meal, RecipeAdapter.RecipeViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecipeViewHolder(private val binding: ItemRecipeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(meal: Meal) {
            binding.tvRecipeName.text = meal.name
            binding.tvRecipeCategory.text = meal.category ?: meal.area ?: "Receta"
            Glide.with(binding.root.context)
                .load(meal.thumbnail)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.ivRecipeThumb)
            binding.root.setOnClickListener { onItemClick(meal) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Meal>() {
        override fun areItemsTheSame(old: Meal, new: Meal) = old.id == new.id
        override fun areContentsTheSame(old: Meal, new: Meal) = old == new
    }
}
