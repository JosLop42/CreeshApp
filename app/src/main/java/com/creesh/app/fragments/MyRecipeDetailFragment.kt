package com.creesh.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.creesh.app.adapters.CommentAdapter
import com.creesh.app.databinding.FragmentMyRecipeDetailBinding
import com.creesh.app.viewmodel.RecipeViewModel

class MyRecipeDetailFragment : Fragment() {

    private var _binding: FragmentMyRecipeDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecipeViewModel by activityViewModels()
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        commentAdapter = CommentAdapter(emptyList())
        binding.rvComments.layoutManager = LinearLayoutManager(context)
        binding.rvComments.adapter = commentAdapter

        val recipe = viewModel.selectedUserRecipe.value ?: run {
            findNavController().navigateUp()
            return
        }

        binding.tvHeaderTitle.text = recipe.title
        viewModel.loadRecipeDetail(recipe.id)

        viewModel.recipeDetail.observe(viewLifecycleOwner) { detail ->
            detail ?: return@observe
            binding.tvRecipeTitle.text = detail.title

            if (!detail.imageUrl.isNullOrBlank()) {
                Glide.with(this).load(detail.imageUrl).centerCrop().into(binding.ivRecipeImage)
            }

            if (!detail.description.isNullOrBlank()) {
                binding.tvDescription.text       = detail.description
                binding.tvDescription.visibility = View.VISIBLE
            }

            if (!detail.instructions.isNullOrBlank()) {
                binding.tvInstructions.text            = detail.instructions
                binding.tvInstructions.visibility      = View.VISIBLE
                binding.tvInstructionsLabel.visibility = View.VISIBLE
            }
        }

        viewModel.recipeIngredients.observe(viewLifecycleOwner) { ingredients ->
            binding.tvIngredients.text = if (ingredients.isEmpty()) {
                "Sin ingredientes registrados"
            } else {
                ingredients.joinToString("\n") { item ->
                    val qty  = item.quantity?.let { "$it " } ?: ""
                    val unit = item.unit?.let { "$it " } ?: ""
                    "• $qty$unit${item.name}"
                }
            }
        }

        viewModel.recipeLikes.observe(viewLifecycleOwner) { count ->
            binding.tvLikesCount.text = count.toString()
        }

        viewModel.recipeComments.observe(viewLifecycleOwner) { comments ->
            binding.tvCommentCount.text = "(${comments.size})"
            if (comments.isEmpty()) {
                binding.tvNoComments.visibility = View.VISIBLE
                binding.rvComments.visibility   = View.GONE
            } else {
                binding.tvNoComments.visibility = View.GONE
                binding.rvComments.visibility   = View.VISIBLE
                commentAdapter.updateList(comments)
            }
        }

        binding.btnSendComment.setOnClickListener {
            val text = binding.etComment.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.addComment(recipe.id, text)
                binding.etComment.text?.clear()
            }
        }

        viewModel.commentError.observe(viewLifecycleOwner) { error ->
            error?.let {
                android.widget.Toast.makeText(requireContext(), it, android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
