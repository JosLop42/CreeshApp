package com.creesh.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.creesh.app.api.models.Chef
import com.creesh.app.databinding.ItemChefBinding

class ChefAdapter(
    private val onChefClick: (Chef) -> Unit
) : ListAdapter<Chef, ChefAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemChefBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chef: Chef) {
            binding.tvChefName.text      = chef.fullName
            binding.tvChefSpecialty.text = chef.specialty
            Glide.with(binding.root)
                .load(chef.photoUrl)
                .circleCrop()
                .placeholder(android.R.color.darker_gray)
                .into(binding.ivChefPhoto)
            binding.root.setOnClickListener { onChefClick(chef) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemChefBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<Chef>() {
        override fun areItemsTheSame(o: Chef, n: Chef) = o.id == n.id
        override fun areContentsTheSame(o: Chef, n: Chef) = o == n
    }
}
