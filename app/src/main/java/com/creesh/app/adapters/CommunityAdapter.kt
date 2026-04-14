package com.creesh.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.creesh.app.databinding.ItemCommunityBinding

data class Community(val name: String, val members: String, val emoji: String)

class CommunityAdapter(
    private val communities: List<Community>,
    private val onClick: (Community) -> Unit
) : RecyclerView.Adapter<CommunityAdapter.CommunityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
        val binding = ItemCommunityBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CommunityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
        holder.bind(communities[position])
    }

    override fun getItemCount() = communities.size

    inner class CommunityViewHolder(private val binding: ItemCommunityBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(community: Community) {
            binding.tvCommunityEmoji.text = community.emoji
            binding.tvCommunityName.text = community.name
            binding.tvCommunityMembers.text = community.members
            binding.root.setOnClickListener { onClick(community) }
        }
    }
}
