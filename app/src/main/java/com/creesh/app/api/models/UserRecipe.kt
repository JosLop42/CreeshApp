package com.creesh.app.api.models

import com.google.gson.annotations.SerializedName

data class UserRecipe(
    @SerializedName("id")          val id: String,
    @SerializedName("title")       val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("image_url")   val imageUrl: String?
)

data class CommunityItem(
    @SerializedName("id")   val id: Int,
    @SerializedName("name") val name: String
)
