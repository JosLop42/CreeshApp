package com.creesh.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.creesh.app.api.models.Meal
import com.creesh.app.databinding.ItemRecipeHorizontalBinding

class RecipeHorizontalAdapter(
    private val onItemClick: (Meal) -> Unit
) : ListAdapter<Meal, RecipeHorizontalAdapter.HorizontalViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalViewHolder {
        val binding = ItemRecipeHorizontalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HorizontalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HorizontalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HorizontalViewHolder(private val binding: ItemRecipeHorizontalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(meal: Meal) {
            binding.tvGemName.text = meal.name
            binding.tvGemCategory.text = meal.category ?: meal.area ?: ""
            Glide.with(binding.root.context)
                .load(meal.thumbnail)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.ivGemThumb)
            binding.root.setOnClickListener { onItemClick(meal) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Meal>() {
        override fun areItemsTheSame(old: Meal, new: Meal) = old.id == new.id
        override fun areContentsTheSame(old: Meal, new: Meal) = old == new
    }
}
