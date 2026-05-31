package com.creesh.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creesh.app.api.RetrofitClient
import com.creesh.app.api.SupabaseApi
import com.creesh.app.api.SupabaseClient
import com.creesh.app.api.SupabaseStorageClient
import com.creesh.app.api.TranslationClient
import com.creesh.app.api.models.CategoryItem
import com.creesh.app.api.models.FavoriteItem
import com.creesh.app.api.models.CommentItem
import com.creesh.app.api.models.CommentRequest
import com.creesh.app.api.models.CommunityItem
import com.creesh.app.api.models.IngredientItem
import com.creesh.app.api.models.IngredientRequest
import com.creesh.app.api.models.Meal
import com.creesh.app.api.models.RecipeDetail
import com.creesh.app.api.models.RecipeRequest
import com.creesh.app.api.models.UserRecipe
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed class PublishState {
    object Idle : PublishState()
    object Loading : PublishState()
    data class Success(val title: String) : PublishState()
    data class Error(val message: String) : PublishState()
}

data class TranslatedContent(
    val name: String,
    val instructions: String,
    val ingredients: List<Pair<String, String>>
)

class RecipeViewModel : ViewModel() {

    private val api = RetrofitClient.api
    private val translationApi = TranslationClient.api
    private val supabaseApi = SupabaseClient.retrofit.create(SupabaseApi::class.java)

    private val userId get() = com.creesh.app.utils.SessionManager.getUserId() ?: "demo_user"

    private val _translatedContent = MutableLiveData<TranslatedContent?>()
    val translatedContent: LiveData<TranslatedContent?> = _translatedContent

    private val _randomMeals = MutableLiveData<List<Meal>>()
    val randomMeals: LiveData<List<Meal>> = _randomMeals

    private val _hiddenGems = MutableLiveData<List<Meal>>()
    val hiddenGems: LiveData<List<Meal>> = _hiddenGems

    private val _searchResults = MutableLiveData<List<Meal>>()
    val searchResults: LiveData<List<Meal>> = _searchResults

    private val _selectedMeal = MutableLiveData<Meal>()
    val selectedMeal: LiveData<Meal> = _selectedMeal

    private val _categories = MutableLiveData<List<CategoryItem>>()
    val categories: LiveData<List<CategoryItem>> = _categories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _favorites = MutableLiveData<List<FavoriteItem>>(emptyList())
    val favorites: LiveData<List<FavoriteItem>> = _favorites

    private val _activeCommunity = MutableLiveData<String?>(null)
    val activeCommunity: LiveData<String?> = _activeCommunity

    private val _publishState = MutableLiveData<PublishState>(PublishState.Idle)
    val publishState: LiveData<PublishState> = _publishState

    private val _myRecipes = MutableLiveData<List<UserRecipe>>(emptyList())
    val myRecipes: LiveData<List<UserRecipe>> = _myRecipes

    private val _communities = MutableLiveData<List<CommunityItem>>(emptyList())
    val communities: LiveData<List<CommunityItem>> = _communities

    private val _selectedUserRecipe = MutableLiveData<UserRecipe?>()
    val selectedUserRecipe: LiveData<UserRecipe?> = _selectedUserRecipe

    private val _recipeDetail = MutableLiveData<RecipeDetail?>()
    val recipeDetail: LiveData<RecipeDetail?> = _recipeDetail

    private val _recipeIngredients = MutableLiveData<List<IngredientItem>>(emptyList())
    val recipeIngredients: LiveData<List<IngredientItem>> = _recipeIngredients

    private val _recipeComments = MutableLiveData<List<CommentItem>>(emptyList())
    val recipeComments: LiveData<List<CommentItem>> = _recipeComments

    private val _recipeLikes = MutableLiveData<Int>(0)
    val recipeLikes: LiveData<Int> = _recipeLikes

    fun loadDiscoverRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val letters = listOf("c", "b", "s", "p")
                val meals = mutableListOf<Meal>()
                for (letter in letters) {
                    val response = api.getMealsByLetter(letter)
                    response.meals?.take(3)?.let { meals.addAll(it) }
                }
                _randomMeals.value = meals.shuffled().take(10)
                _error.value = null
            } catch (e: UnknownHostException) {
                _error.value = "Sin conexión a internet"
            } catch (e: SocketTimeoutException) {
                _error.value = "Tiempo de espera agotado"
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadHiddenGems() {
        viewModelScope.launch {
            try {
                val meals = mutableListOf<Meal>()
                val response1 = api.getMealsByLetter("x")
                response1.meals?.let { meals.addAll(it) }
                val response2 = api.getMealsByLetter("y")
                response2.meals?.let { meals.addAll(it) }
                if (meals.isEmpty()) {
                    val response3 = api.getMealsByLetter("z")
                    response3.meals?.let { meals.addAll(it) }
                }
                _hiddenGems.value = if (meals.isNotEmpty()) meals.shuffled().take(5)
                else {
                    val fallback = api.getMealsByLetter("v")
                    fallback.meals?.shuffled()?.take(5) ?: emptyList()
                }
                _error.value = null
            } catch (e: UnknownHostException) {
                _error.value = "Sin conexión a internet"
            } catch (e: SocketTimeoutException) {
                _error.value = "Tiempo de espera agotado"
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
    }

    fun searchMeals(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.searchMeals(query)
                _searchResults.value = response.meals ?: emptyList()
                _error.value = null
            } catch (e: UnknownHostException) {
                _error.value = "Sin conexión a internet"
            } catch (e: SocketTimeoutException) {
                _error.value = "Tiempo de espera agotado"
            } catch (e: Exception) {
                _error.value = "Error en búsqueda: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getMealById(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getMealById(id)
                response.meals?.firstOrNull()?.let { _selectedMeal.value = it }
                _error.value = null
            } catch (e: UnknownHostException) {
                _error.value = "Sin conexión a internet"
            } catch (e: SocketTimeoutException) {
                _error.value = "Tiempo de espera agotado"
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterByCategory(category: String, communityName: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _activeCommunity.value = communityName
            try {
                val response = api.filterByCategory(category)
                _searchResults.value = response.meals ?: emptyList()
                _error.value = null
            } catch (e: UnknownHostException) {
                _error.value = "Sin conexión a internet"
            } catch (e: SocketTimeoutException) {
                _error.value = "Tiempo de espera agotado"
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearCommunityFilter() {
        _activeCommunity.value = null
        _searchResults.value = emptyList()
    }

    fun setSelectedMeal(meal: Meal) {
        _selectedMeal.value = meal
    }

    fun loadFavorites() {
        viewModelScope.launch {
            try {
                _favorites.value = supabaseApi.getFavorites("eq.$userId")
            } catch (e: Exception) {
                // si falla, mantener lista actual
            }
        }
    }

    fun toggleFavorite(meal: Meal) {
        viewModelScope.launch {
            try {
                if (isFavorite(meal.id)) {
                    supabaseApi.removeFavorite("eq.$userId", "eq.${meal.id}")
                } else {
                    supabaseApi.addFavorite(
                        FavoriteItem(
                            userId       = userId,
                            mealId       = meal.id,
                            mealTitle    = meal.name,
                            mealImage    = meal.thumbnail,
                            mealCategory = meal.category
                        )
                    )
                }
                loadFavorites()
            } catch (e: Exception) {
                // silencioso
            }
        }
    }

    fun loadMyRecipes() {
        val authorId = com.creesh.app.utils.SessionManager.getUserId() ?: return
        viewModelScope.launch {
            try {
                _myRecipes.value = supabaseApi.getMyRecipes("eq.$authorId", "created_at.desc")
            } catch (e: Exception) { /* silencioso */ }
        }
    }

    fun loadCommunities() {
        viewModelScope.launch {
            try {
                _communities.value = supabaseApi.getCommunities()
            } catch (e: Exception) { /* silencioso */ }
        }
    }

    fun publishRecipe(
        title: String,
        description: String,
        ingredientLines: List<String>,
        instructions: String,
        communityId: Int? = null,
        imageBytes: ByteArray? = null,
        mimeType: String? = null
    ) {
        val authorId = com.creesh.app.utils.SessionManager.getUserId() ?: run {
            _publishState.value = PublishState.Error("No hay sesión activa")
            return
        }
        _publishState.value = PublishState.Loading
        viewModelScope.launch {
            try {
                var imageUrl: String? = null
                var uploadError: String? = null
                if (imageBytes != null && mimeType != null) {
                    val ext = when {
                        mimeType.contains("png")  -> "png"
                        mimeType.contains("webp") -> "webp"
                        else                      -> "jpg"
                    }
                    val filePath = "$authorId/${System.currentTimeMillis()}.$ext"
                    val result = SupabaseStorageClient.uploadImage(filePath, imageBytes, mimeType)
                    imageUrl    = result.url
                    uploadError = result.error
                }

                val result = supabaseApi.addRecipe(
                    RecipeRequest(
                        authorId     = authorId,
                        title        = title,
                        description  = description.ifBlank { null },
                        instructions = instructions.ifBlank { null },
                        communityId  = communityId,
                        imageUrl     = imageUrl
                    )
                )
                val recipeId = result.firstOrNull()?.id ?: run {
                    _publishState.value = PublishState.Error("Error al obtener ID de receta")
                    return@launch
                }
                if (ingredientLines.isNotEmpty()) {
                    supabaseApi.addIngredients(
                        ingredientLines.mapIndexed { i, name ->
                            IngredientRequest(recipeId, name, i)
                        }
                    )
                }
                val successMsg = if (uploadError != null)
                    "$title ⚠️ Foto falló: $uploadError"
                else title
                _publishState.value = PublishState.Success(successMsg)
                loadMyRecipes()
            } catch (e: UnknownHostException) {
                _publishState.value = PublishState.Error("Sin conexión a internet")
            } catch (e: Exception) {
                _publishState.value = PublishState.Error("Error al publicar: ${e.message}")
            }
        }
    }

    fun resetPublishState() { _publishState.value = PublishState.Idle }

    fun updateDisplayName(name: String) {
        val userId = com.creesh.app.utils.SessionManager.getUserId() ?: return
        viewModelScope.launch {
            try {
                supabaseApi.updateProfile("eq.$userId", mapOf("full_name" to name))
            } catch (e: Exception) { /* si falla en red, el nombre igual queda guardado localmente */ }
            com.creesh.app.utils.SessionManager.saveDisplayName(name)
        }
    }

    fun setSelectedUserRecipe(recipe: UserRecipe) { _selectedUserRecipe.value = recipe }

    fun loadRecipeDetail(recipeId: String) {
        viewModelScope.launch {
            try {
                _recipeDetail.value = supabaseApi.getRecipeById("eq.$recipeId").firstOrNull()
                _recipeIngredients.value = supabaseApi.getIngredients("eq.$recipeId")
                _recipeComments.value    = supabaseApi.getComments("eq.$recipeId")
                _recipeLikes.value       = supabaseApi.getLikesByMealId("eq.$recipeId").size
            } catch (e: Exception) { /* silencioso */ }
        }
    }

    private val _commentError = MutableLiveData<String?>()
    val commentError: LiveData<String?> = _commentError

    fun addComment(recipeId: String, content: String) {
        val userId = com.creesh.app.utils.SessionManager.getUserId() ?: return
        viewModelScope.launch {
            try {
                supabaseApi.addComment(CommentRequest(recipeId, userId, content))
                _recipeComments.value = supabaseApi.getComments("eq.$recipeId")
                _commentError.value = null
            } catch (e: Exception) {
                _commentError.value = "No se pudo publicar el comentario: ${e.message}"
            }
        }
    }

    fun isFavorite(mealId: String): Boolean {
        return _favorites.value?.any { it.mealId == mealId } == true
    }

    fun translateMeal(meal: Meal) {
        _translatedContent.value = null
        viewModelScope.launch {
            try {
                // Traducir nombre
                val translatedName = translateChunked(meal.name)

                // Traducir ingredientes en una sola llamada (unidos por \n)
                val originalIngredients = meal.getIngredientList()
                val translatedIngredients = if (originalIngredients.isNotEmpty()) {
                    val joined = originalIngredients.joinToString("\n") { it.first }
                    val translatedJoined = translateChunked(joined)
                    val translatedNames = translatedJoined.split("\n")
                    originalIngredients.mapIndexed { i, (_, measure) ->
                        Pair(translatedNames.getOrElse(i) { originalIngredients[i].first }, measure)
                    }
                } else emptyList()

                // Traducir instrucciones
                val rawInstructions = meal.instructions
                    ?.replace("\r\n", "\n")
                    ?.replace("\r", "\n")
                    ?: ""
                val translatedInstructions = translateChunked(rawInstructions)

                _translatedContent.value = TranslatedContent(
                    name = translatedName,
                    instructions = translatedInstructions,
                    ingredients = translatedIngredients
                )
            } catch (e: Exception) {
                // Si falla la traducción, mostrar contenido original
                _translatedContent.value = TranslatedContent(
                    name = meal.name,
                    instructions = meal.instructions ?: "Sin instrucciones disponibles.",
                    ingredients = meal.getIngredientList()
                )
            }
        }
    }

    private suspend fun translateChunked(text: String): String {
        if (text.isBlank()) return text
        val chunks = splitIntoChunks(text, 450)
        val result = StringBuilder()
        for ((index, chunk) in chunks.withIndex()) {
            if (index > 0) result.append(" ")
            val translated = try {
                val response = translationApi.translate(chunk)
                if (response.responseStatus == 200) response.responseData.translatedText
                else chunk
            } catch (e: Exception) {
                chunk
            }
            result.append(translated)
        }
        return result.toString()
    }

    private fun splitIntoChunks(text: String, maxLen: Int): List<String> {
        if (text.length <= maxLen) return listOf(text)
        val sentences = text.split(Regex("(?<=[.!?])\\s+"))
        val chunks = mutableListOf<String>()
        val current = StringBuilder()
        for (sentence in sentences) {
            if (current.length + sentence.length + 1 > maxLen && current.isNotEmpty()) {
                chunks.add(current.toString().trim())
                current.clear()
            }
            if (current.isNotEmpty()) current.append(" ")
            current.append(sentence)
        }
        if (current.isNotEmpty()) chunks.add(current.toString().trim())
        return chunks.filter { it.isNotBlank() }
    }
}
