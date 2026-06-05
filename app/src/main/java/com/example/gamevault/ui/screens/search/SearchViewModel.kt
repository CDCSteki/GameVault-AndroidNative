package com.example.gamevault.ui.screens.search

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gamevault.R
import com.example.gamevault.data.local.entity.SearchHistoryEntity
import com.example.gamevault.data.remote.dto.GameDto
import com.example.gamevault.data.repository.GameRepository
import com.example.gamevault.data.repository.SearchRepository
import com.example.gamevault.ui.util.NetworkConnectivityObserver
import com.example.gamevault.ui.util.NetworkStatus
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
    val defaultGames: List<GameDto> = emptyList(),
    val searchHistory: List<SearchHistoryEntity> = emptyList(),
    val filters: SearchFilters = SearchFilters(),
    val isLoading: Boolean = false,
    val isFilterSheetVisible: Boolean = false,
    val hasSearched: Boolean = false,
    @param:StringRes val errorMessageRes: Int? = null,
    val totalResults: Int = 0,
    val isOffline: Boolean = false
)

class SearchViewModel(
    private val searchRepository: SearchRepository,
    private val gameRepository: GameRepository,
    applicationContext: Context
) : ViewModel() {

    private val appContext = applicationContext.applicationContext
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadSearchHistory()
        observeNetworkAndLoad()
    }

    private fun observeNetworkAndLoad() {
        viewModelScope.launch {
            NetworkConnectivityObserver.observe(appContext).collect { status ->
                when (status) {
                    NetworkStatus.Available -> {
                        _uiState.value = _uiState.value.copy(isOffline = false)
                        val state = _uiState.value
                        if (state.defaultGames.isEmpty()) {
                            loadDefaultGames()
                        }
                        if (state.hasSearched || state.query.isNotBlank() || state.filters != SearchFilters()) {
                            searchJob?.cancel()
                            searchJob = viewModelScope.launch {
                                performSearch(state.query)
                            }
                        }
                    }
                    NetworkStatus.Unavailable -> {
                        searchJob?.cancel()
                        _uiState.value = _uiState.value.copy(
                            defaultGames = emptyList(),
                            searchResults = emptyList(),
                            isLoading = false,
                            isOffline = true,
                            errorMessageRes = R.string.general_error_connection
                        )
                    }
                }
            }
        }
    }

    private fun loadDefaultGames() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = gameRepository.getPopularGames(20)
            _uiState.value = _uiState.value.copy(
                defaultGames = result.getOrElse { emptyList() },
                isLoading = false,
                errorMessageRes = if (result.isFailure) R.string.general_error_connection else null
            )
        }
    }

    private fun loadSearchHistory() {
        viewModelScope.launch {
            searchRepository.getRecentSearches().collect { history ->
                _uiState.value = _uiState.value.copy(searchHistory = history)
            }
        }
    }

    fun onQueryChange(query: String) {
        if (_uiState.value.isOffline) {
            _uiState.value = _uiState.value.copy(query = query)
            return
        }

        _uiState.value = _uiState.value.copy(
            query = query,
            errorMessageRes = null
        )

        searchJob?.cancel()

        if (query.length >= 2) {
            searchJob = viewModelScope.launch {
                delay(500)
                performSearch(query)
            }
        } else if (query.isEmpty()) {
            val filters = _uiState.value.filters
            if (filters != SearchFilters()) {
                searchJob = viewModelScope.launch {
                    delay(500)
                    performSearch("")
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    searchResults = emptyList(),
                    hasSearched = false
                )
            }
        }
    }

    fun onSearchSubmit() {
        if (_uiState.value.isOffline) return
        val query = _uiState.value.query
        if (query.isBlank() && _uiState.value.filters == SearchFilters()) return

        searchJob?.cancel()
        viewModelScope.launch {
            performSearch(query)
        }
    }

    fun retry() {
        if (_uiState.value.query.isBlank() && _uiState.value.filters == SearchFilters()) {
            loadDefaultGames()
        } else {
            onSearchSubmit()
        }
    }

    private suspend fun performSearch(query: String) {
        val filters = _uiState.value.filters
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessageRes = null)

        if (query.isNotBlank()) {
            searchRepository.saveSearch(query)
        }

        val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

        val datesParam = filters.year?.let {
            "$it-01-01,$it-12-31"
        } ?: if (filters.ordering == "-released") {
            "1950-01-01,$currentDate"
        } else {
            null
        }

        val result = searchRepository.searchGames(
            query = query,
            genres = filters.genre,
            platforms = filters.platform,
            metacritic = filters.minRating,
            dates = datesParam,
            ordering = filters.ordering
        )

        _uiState.value = _uiState.value.copy(
            searchResults = result.getOrElse { emptyList() },
            isLoading = false,
            hasSearched = true,
            errorMessageRes = if (result.isFailure) R.string.general_error_connection else null
        )
    }

    fun onHistoryItemClick(query: String) {
        if (_uiState.value.isOffline) return
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
        if (_uiState.value.isOffline) return
        _uiState.value = _uiState.value.copy(
            filters = filters,
            isFilterSheetVisible = false
        )
        viewModelScope.launch {
            performSearch(_uiState.value.query)
        }
    }

    fun onClearFilters() {
        _uiState.value = _uiState.value.copy(filters = SearchFilters())
        if (_uiState.value.isOffline) return
        val query = _uiState.value.query
        if (query.isNotBlank()) {
            viewModelScope.launch {
                performSearch(query)
            }
        } else {
            _uiState.value = _uiState.value.copy(
                searchResults = emptyList(),
                hasSearched = false
            )
        }
    }

    class Factory(
        private val searchRepository: SearchRepository,
        private val gameRepository: GameRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(searchRepository, gameRepository, context) as T
        }
    }
}