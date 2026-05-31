package com.creesh.app.api.models

import com.google.gson.annotations.SerializedName

data class RecipeDetail(
    @SerializedName("id")           val id: String,
    @SerializedName("title")        val title: String,
    @SerializedName("description")  val description: String?,
    @SerializedName("instructions") val instructions: String?,
    @SerializedName("image_url")    val imageUrl: String?,
    @SerializedName("created_at")   val createdAt: String?
)

data class IngredientItem(
    @SerializedName("name")       val name: String,
    @SerializedName("quantity")   val quantity: String?,
    @SerializedName("unit")       val unit: String?,
    @SerializedName("sort_order") val sortOrder: Int
)

data class CommentItem(
    @SerializedName("id")         val id: String,
    @SerializedName("user_id")    val userId: String,
    @SerializedName("content")    val content: String,
    @SerializedName("created_at") val createdAt: String
)

data class CommentRequest(
    @SerializedName("recipe_id") val recipeId: String,
    @SerializedName("user_id")   val userId: String,
    @SerializedName("content")   val content: String
)
