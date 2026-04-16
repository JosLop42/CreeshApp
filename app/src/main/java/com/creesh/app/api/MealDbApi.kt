package com.creesh.app.api

import com.creesh.app.api.models.CategoryResponse
import com.creesh.app.api.models.MealResponse
import retrofit2.http.GET
import retrofit2.http.Query

//librería que convierte automáticamente llamadas HTTP en funciones de Kotlin
//En vez de escribir código de red manualmente, defines una interfaz y Retrofit hace el resto.
interface MealDbApi {

    // Receta aleatoria
    @GET("random.php")
    suspend fun getRandomMeal(): MealResponse

    // Buscar por nombre
    @GET("search.php")
    suspend fun searchMeals(@Query("s") name: String): MealResponse

    // Obtener todas las categorías
    @GET("categories.php")
    suspend fun getCategories(): CategoryResponse

    // Filtrar por categoría
    @GET("filter.php")
    suspend fun filterByCategory(@Query("c") category: String): MealResponse

    // Ver detalle por ID
    @GET("lookup.php")
    suspend fun getMealById(@Query("i") id: String): MealResponse

    // Recetas por primera letra
    @GET("search.php")
    suspend fun getMealsByLetter(@Query("f") letter: String): MealResponse
}
