package com.creesh.app.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.creesh.app.api.models.CommunityItem
import com.creesh.app.databinding.FragmentUploadRecipeBinding
import com.creesh.app.viewmodel.PublishState
import com.creesh.app.viewmodel.RecipeViewModel

class UploadRecipeFragment : Fragment() {

    private var _binding: FragmentUploadRecipeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RecipeViewModel by activityViewModels()
    private var selectedImageUri: Uri? = null
    private var communityList: List<CommunityItem> = emptyList()
    private var selectedCommunityId: Int? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivRecipePhoto.setImageURI(it)
            binding.tvAddPhoto.text = "Foto seleccionada"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadCommunities()

        viewModel.communities.observe(viewLifecycleOwner) { communities ->
            if (communities.isEmpty()) return@observe
            communityList = communities
            val emojis = mapOf(
                "Gym Rats"           to "💪",
                "Vegano"             to "🌱",
                "Vegetariano"        to "🥗",
                "Amantes de la Carne" to "🥩",
                "Mariscos"           to "🦞",
                "Postres y Dulces"   to "🍰",
                "Pasta e Italiana"   to "🍝",
                "Cocina Asiatica"    to "🍜"
            )
            val options = listOf("Sin comunidad") + communities.map { "${emojis[it.name] ?: "🍽️"} ${it.name}" }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, options)
            binding.actvCommunity.setAdapter(adapter)
            binding.actvCommunity.setOnItemClickListener { _, _, position, _ ->
                selectedCommunityId = if (position == 0) null else communities[position - 1].id
            }
        }

        binding.btnAddPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnAddIngredient.setOnClickListener {
            val current = binding.etIngredients.text.toString()
            if (current.isNotBlank()) {
                binding.etIngredients.setText("$current\n• ")
            } else {
                binding.etIngredients.setText("• ")
            }
            binding.etIngredients.setSelection(binding.etIngredients.text?.length ?: 0)
        }

        binding.btnPublish.setOnClickListener { submitRecipe() }

        viewModel.publishState.observe(viewLifecycleOwner) { state ->
            binding.progressUpload.visibility = if (state is PublishState.Loading) View.VISIBLE else View.GONE
            binding.btnPublish.isEnabled = state !is PublishState.Loading

            when (state) {
                is PublishState.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "¡Receta \"${state.title}\" publicada en CREESH!",
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.resetPublishState()
                    clearForm()
                }
                is PublishState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetPublishState()
                }
                else -> Unit
            }
        }
    }

    private fun submitRecipe() {
        val title        = binding.etRecipeName.text.toString().trim()
        val description  = binding.etDescription.text.toString().trim()
        val ingredients  = binding.etIngredients.text.toString().trim()
        val instructions = binding.etInstructions.text.toString().trim()

        if (title.isEmpty()) {
            binding.etRecipeName.error = "El nombre es requerido"
            return
        }
        if (ingredients.isEmpty()) {
            binding.etIngredients.error = "Agrega al menos un ingrediente"
            return
        }

        val ingredientList = ingredients
            .split("\n")
            .map { it.removePrefix("•").trim() }
            .filter { it.isNotEmpty() }

        val imageBytes = readImageBytes()
        val mimeType   = selectedImageUri?.let {
            requireContext().contentResolver.getType(it) ?: "image/jpeg"
        }

        viewModel.publishRecipe(title, description, ingredientList, instructions, selectedCommunityId, imageBytes, mimeType)
    }

    private fun readImageBytes(): ByteArray? {
        val uri = selectedImageUri ?: return null
        return try {
            requireContext().contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: Exception) {
            null
        }
    }

    private fun clearForm() {
        binding.etRecipeName.text?.clear()
        binding.etDescription.text?.clear()
        binding.etIngredients.text?.clear()
        binding.etInstructions.text?.clear()
        binding.actvCommunity.setText("")
        binding.ivRecipePhoto.setImageResource(android.R.drawable.ic_menu_camera)
        binding.tvAddPhoto.text = "Agregar Foto"
        selectedImageUri = null
        selectedCommunityId = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
