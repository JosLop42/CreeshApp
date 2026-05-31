package com.creesh.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.creesh.app.api.RandomUserClient
import com.creesh.app.api.models.Chef
import com.creesh.app.api.models.Meal
import com.creesh.app.api.models.fallbackChefs
import com.creesh.app.api.models.toChef
import com.creesh.app.utils.SessionManager
import kotlinx.coroutines.launch
import kotlin.math.abs

class SocialViewModel : ViewModel() {

    private val _chefs = MutableLiveData<List<Chef>>(emptyList())
    val chefs: LiveData<List<Chef>> = _chefs

    private val _selectedChef = MutableLiveData<Chef?>()
    val selectedChef: LiveData<Chef?> = _selectedChef

    private val _followedChefIds = MutableLiveData<MutableSet<String>>(mutableSetOf())
    val followedChefIds: LiveData<MutableSet<String>> = _followedChefIds

    private val _isLoadingChefs = MutableLiveData(false)
    val isLoadingChefs: LiveData<Boolean> = _isLoadingChefs

    init {
        _chefs.value = fallbackChefs()
        val userId = SessionManager.getUserId()
        if (userId != null) {
            _followedChefIds.value = SessionManager.getFollowedChefs(userId)
        }
        loadChefsFromApi()
    }

    fun loadChefs() {
        loadChefsFromApi()
    }

    private fun loadChefsFromApi() {
        viewModelScope.launch {
            _isLoadingChefs.value = true
            try {
                val response = RandomUserClient.api.getUsers(results = 12, seed = "creesh2024")
                val apiChefs = response.results.map { it.toChef() }
                if (apiChefs.isNotEmpty()) _chefs.value = apiChefs
            } catch (e: java.net.UnknownHostException) {
                // Sin red: los fallbackChefs ya están cargados, no hacer nada
            } catch (e: java.net.SocketTimeoutException) {
                // Timeout: idem
            } catch (e: Exception) {
                // Cualquier otro error: idem
            } finally {
                _isLoadingChefs.value = false
            }
        }
    }

    /** Devuelve siempre el mismo cocinero para una receta dada (determinístico). */
    fun getChefForMeal(meal: Meal): Chef? {
        val list = _chefs.value ?: return null
        if (list.isEmpty()) return null
        val hash = abs((meal.category ?: meal.area ?: meal.id).hashCode())
        return list[hash % list.size]
    }

    fun setSelectedChef(chef: Chef) {
        _selectedChef.value = chef
    }

    fun toggleFollow(chefId: String) {
        val set = _followedChefIds.value ?: mutableSetOf()
        if (set.contains(chefId)) set.remove(chefId) else set.add(chefId)
        _followedChefIds.value = set
        SessionManager.getUserId()?.let { SessionManager.saveFollowedChefs(it, set) }
    }

    fun isFollowing(chefId: String): Boolean =
        _followedChefIds.value?.contains(chefId) == true
}
