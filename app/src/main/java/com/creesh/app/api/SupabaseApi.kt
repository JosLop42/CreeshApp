package com.creesh.app.api

import com.creesh.app.api.models.CommentItem
import com.creesh.app.api.models.CommentRequest
import com.creesh.app.api.models.CommunityItem
import com.creesh.app.api.models.FavoriteItem
import com.creesh.app.api.models.IngredientItem
import com.creesh.app.api.models.IngredientRequest
import com.creesh.app.api.models.RecipeDetail
import com.creesh.app.api.models.RecipeRequest
import com.creesh.app.api.models.RecipeResponse
import com.creesh.app.api.models.UserRecipe
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApi {

    @GET("favorites")
    suspend fun getFavorites(
        @Query("user_id") userIdFilter: String
    ): List<FavoriteItem>

    @Headers("Prefer: return=minimal")
    @POST("favorites")
    suspend fun addFavorite(@Body item: FavoriteItem)

    @DELETE("favorites")
    suspend fun removeFavorite(
        @Query("user_id") userIdFilter: String,
        @Query("meal_id") mealIdFilter: String
    )

    @Headers("Prefer: return=representation")
    @POST("recipes")
    suspend fun addRecipe(@Body recipe: RecipeRequest): List<RecipeResponse>

    @Headers("Prefer: return=minimal")
    @POST("ingredients")
    suspend fun addIngredients(@Body ingredients: List<IngredientRequest>)

    @GET("recipes")
    suspend fun getMyRecipes(
        @Query("author_id") authorIdFilter: String,
        @Query("order")     order: String = "created_at.desc"
    ): List<UserRecipe>

    @GET("communities")
    suspend fun getCommunities(): List<CommunityItem>

    @GET("recipes")
    suspend fun getRecipeById(@Query("id") idFilter: String): List<RecipeDetail>

    @GET("ingredients")
    suspend fun getIngredients(
        @Query("recipe_id") recipeIdFilter: String,
        @Query("order")     order: String = "sort_order.asc"
    ): List<IngredientItem>

    @GET("comments")
    suspend fun getComments(
        @Query("recipe_id") recipeIdFilter: String,
        @Query("order")     order: String = "created_at.desc"
    ): List<CommentItem>

    @GET("favorites")
    suspend fun getLikesByMealId(@Query("meal_id") mealIdFilter: String): List<FavoriteItem>

    @Headers("Prefer: return=minimal")
    @POST("comments")
    suspend fun addComment(@Body comment: CommentRequest)

    @Headers("Prefer: return=minimal")
    @PATCH("profiles")
    suspend fun updateProfile(
        @Query("id") idFilter: String,
        @Body update: Map<String, String>
    )
}
