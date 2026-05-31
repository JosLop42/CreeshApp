package com.creesh.app.api.models

import com.google.gson.annotations.SerializedName

data class RecipeRequest(
    @SerializedName("author_id")    val authorId: String,
    @SerializedName("title")        val title: String,
    @SerializedName("description")  val description: String?,
    @SerializedName("instructions") val instructions: String?,
    @SerializedName("community_id") val communityId: Int?,
    @SerializedName("image_url")    val imageUrl: String?
)

data class RecipeResponse(
    @SerializedName("id") val id: String
)

data class IngredientRequest(
    @SerializedName("recipe_id")  val recipeId: String,
    @SerializedName("name")       val name: String,
    @SerializedName("sort_order") val sortOrder: Int
)
