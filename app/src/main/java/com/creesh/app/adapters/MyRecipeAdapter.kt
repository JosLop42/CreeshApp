package com.creesh.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.creesh.app.api.models.UserRecipe
import com.creesh.app.databinding.ItemRecipeBinding

class MyRecipeAdapter(
    private var items: List<UserRecipe>,
    private val onClick: (UserRecipe) -> Unit = {}
) : RecyclerView.Adapter<MyRecipeAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemRecipeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvRecipeName.text     = item.title
        holder.binding.tvRecipeCategory.text = item.description?.take(40) ?: "Mi receta"
        if (!item.imageUrl.isNullOrBlank()) {
            Glide.with(holder.itemView.context)
                .load(item.imageUrl)
                .centerCrop()
                .into(holder.binding.ivRecipeThumb)
        } else {
            holder.binding.ivRecipeThumb.setImageResource(android.R.drawable.ic_menu_camera)
        }
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<UserRecipe>) {
        items = newItems
        notifyDataSetChanged()
    }
}
