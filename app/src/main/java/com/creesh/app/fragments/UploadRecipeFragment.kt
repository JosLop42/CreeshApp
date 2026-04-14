package com.creesh.app.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.creesh.app.databinding.FragmentUploadRecipeBinding

class UploadRecipeFragment : Fragment() {

    private var _binding: FragmentUploadRecipeBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null

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

        binding.btnAddPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnPublish.setOnClickListener {
            publishRecipe()
        }

        binding.btnAddIngredient.setOnClickListener {
            val current = binding.etIngredients.text.toString()
            if (current.isNotBlank()) {
                binding.etIngredients.setText("$current\n• ")
                binding.etIngredients.setSelection(binding.etIngredients.text?.length ?: 0)
            } else {
                binding.etIngredients.setText("• ")
                binding.etIngredients.setSelection(binding.etIngredients.text?.length ?: 0)
            }
        }
    }

    private fun publishRecipe() {
        val name = binding.etRecipeName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val ingredients = binding.etIngredients.text.toString().trim()
        val instructions = binding.etInstructions.text.toString().trim()

        if (name.isEmpty()) {
            binding.etRecipeName.error = "El nombre es requerido"
            return
        }
        if (description.isEmpty()) {
            binding.etDescription.error = "La descripción es requerida"
            return
        }
        if (ingredients.isEmpty()) {
            binding.etIngredients.error = "Agrega al menos un ingrediente"
            return
        }

        // Simular publicación exitosa
        binding.progressUpload.visibility = View.VISIBLE
        binding.btnPublish.isEnabled = false

        binding.root.postDelayed({
            binding.progressUpload.visibility = View.GONE
            binding.btnPublish.isEnabled = true
            Toast.makeText(
                requireContext(),
                "¡Receta \"$name\" publicada en CREESH!",
                Toast.LENGTH_LONG
            ).show()
            clearForm()
        }, 1500)
    }

    private fun clearForm() {
        binding.etRecipeName.text?.clear()
        binding.etDescription.text?.clear()
        binding.etIngredients.text?.clear()
        binding.etInstructions.text?.clear()
        binding.ivRecipePhoto.setImageResource(android.R.drawable.ic_menu_camera)
        binding.tvAddPhoto.text = "Agregar Foto"
        selectedImageUri = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
