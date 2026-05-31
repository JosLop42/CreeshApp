package com.creesh.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.creesh.app.api.models.FavoriteItem
import com.creesh.app.databinding.ItemRecipeBinding

class FavoriteAdapter(
    private var items: List<FavoriteItem>,
    private val onClick: (FavoriteItem) -> Unit
) : RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemRecipeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvRecipeName.text     = item.mealTitle
        holder.binding.tvRecipeCategory.text = item.mealCategory ?: "Receta"
        Glide.with(holder.itemView.context)
            .load(item.mealImage)
            .centerCrop()
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.binding.ivRecipeThumb)
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<FavoriteItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
