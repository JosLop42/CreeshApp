package com.creesh.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.creesh.app.api.models.CommentItem
import com.creesh.app.databinding.ItemCommentBinding

class CommentAdapter(
    private var items: List<CommentItem>
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val initial = item.userId.take(1).uppercase()
        val shortId = item.userId.take(8)
        val date    = item.createdAt.take(10)

        holder.binding.tvCommentInitial.text = initial
        holder.binding.tvCommentUser.text    = "Usuario $shortId..."
        holder.binding.tvCommentDate.text    = date
        holder.binding.tvCommentContent.text = item.content
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<CommentItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
