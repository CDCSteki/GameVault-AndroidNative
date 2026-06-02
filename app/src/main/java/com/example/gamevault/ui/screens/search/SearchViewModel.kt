package com.example.gamevault.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gamevault.data.local.entity.SearchHistoryEntity
import com.example.gamevault.data.remote.dto.GameDto
import com.example.gamevault.data.repository.GameRepository
import com.example.gamevault.data.repository.SearchRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchFilters(
    val genre: String? = null,
    val platform: String? = null,
    val minRating: String? = null,
    val year: String? = null,
    val ordering: String? = null
)

data class SearchUiState(
    val query: String = "",
    val searchResults: List<GameDto> = emptyList(),
    val searchHistory: List<SearchHistoryEntity> = emptyList(),
    val filters: SearchFilters = SearchFilters(),
    val isLoading: Boolean = false,
    val isFilterSheetVisible: Boolean = false,
    val hasSearched: Boolean = false,
    val errorMessage: String? = null,
    val totalResults: Int = 0
)

class SearchViewModel(
    private val searchRepository: SearchRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadSearchHistory()
    }

    private fun loadSearchHistory() {
        viewModelScope.launch {
            searchRepository.getRecentSearches().collect { history ->
                _uiState.value = _uiState.value.copy(searchHistory = history)
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(
            query = query,
            errorMessage = null
        )
        // Debounce search
        searchJob?.cancel()
        if (query.length >= 2) {
            searchJob = viewModelScope.launch {
                delay(500)
                performSearch(query)
            }
        } else if (query.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                searchResults = emptyList(),
                hasSearched = false
            )
        }
    }

    fun onSearchSubmit() {
        val query = _uiState.value.query
        if (query.isBlank()) return
        searchJob?.cancel()
        viewModelScope.launch {
            performSearch(query)
        }
    }

    private suspend fun performSearch(query: String) {
        val filters = _uiState.value.filters
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        searchRepository.saveSearch(query)

        val result = searchRepository.searchGames(
            query = query,
            genres = filters.genre,
            platforms = filters.platform,
            metacritic = filters.minRating,
            dates = filters.year?.let { "$it-01-01,$it-12-31" },
            ordering = filters.ordering
        )

        _uiState.value = _uiState.value.copy(
            searchResults = result.getOrElse { emptyList() },
            isLoading = false,
            hasSearched = true,
            errorMessage = if (result.isFailure) "Search failed. Check your connection." else null
        )
    }

    fun onHistoryItemClick(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        viewModelScope.launch {
            performSearch(query)
        }
    }

    fun onDeleteHistoryItem(id: Int) {
        viewModelScope.launch {
            searchRepository.deleteSearchById(id)
        }
    }

    fun onClearHistory() {
        viewModelScope.launch {
            searchRepository.clearAllHistory()
        }
    }

    fun onToggleFilterSheet() {
        _uiState.value = _uiState.value.copy(
            isFilterSheetVisible = !_uiState.value.isFilterSheetVisible
        )
    }

    fun onApplyFilters(filters: SearchFilters) {
        _uiState.value = _uiState.value.copy(
            filters = filters,
            isFilterSheetVisible = false
        )
        val query = _uiState.value.query
        if (query.isNotBlank()) {
            viewModelScope.launch {
                performSearch(query)
            }
        }
    }

    fun onClearFilters() {
        _uiState.value = _uiState.value.copy(filters = SearchFilters())
    }

    class Factory(
        private val searchRepository: SearchRepository,
        private val gameRepository: GameRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(searchRepository, gameRepository) as T
        }
    }
}