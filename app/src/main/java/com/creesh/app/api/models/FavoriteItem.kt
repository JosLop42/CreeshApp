package com.creesh.app.api.models

import com.google.gson.annotations.SerializedName

data class FavoriteItem(
    @SerializedName("user_id")       val userId: String,
    @SerializedName("meal_id")       val mealId: String,
    @SerializedName("meal_title")    val mealTitle: String,
    @SerializedName("meal_image")    val mealImage: String?,
    @SerializedName("meal_category") val mealCategory: String?
)
