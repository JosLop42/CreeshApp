package com.creesh.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creesh.app.api.RetrofitClient
import com.creesh.app.api.TranslationClient
import com.creesh.app.api.models.CategoryItem
import com.creesh.app.api.models.Meal
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException

data class TranslatedContent(
    val name: String,
    val instructions: String,
    val ingredients: List<Pair<String, String>>
)

class RecipeViewModel : ViewModel() {

    private val api = RetrofitClient.api
    private val translationApi = TranslationClient.api

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

    private val _favorites = MutableLiveData<MutableList<Meal>>(mutableListOf())
    val favorites: LiveData<MutableList<Meal>> = _favorites

    private val _activeCommunity = MutableLiveData<String?>(null)
    val activeCommunity: LiveData<String?> = _activeCommunity

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

    fun toggleFavorite(meal: Meal) {
        val list = _favorites.value ?: mutableListOf()
        if (list.any { it.id == meal.id }) {
            list.removeAll { it.id == meal.id }
        } else {
            list.add(meal)
        }
        _favorites.value = list
    }

    fun isFavorite(mealId: String): Boolean {
        return _favorites.value?.any { it.id == mealId } == true
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
