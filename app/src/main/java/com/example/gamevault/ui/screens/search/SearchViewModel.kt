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
    val defaultGames: List<GameDto> = emptyList(), // Nou: Jocurile afișate implicit
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
        loadDefaultGames() // Încărcăm jocurile de bază la deschiderea ecranului
    }

    private fun loadDefaultGames() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Aducem 20 de jocuri populare ca sugestii
            val result = gameRepository.getPopularGames(20)
            _uiState.value = _uiState.value.copy(
                defaultGames = result.getOrElse { emptyList() },
                isLoading = false
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
            val filters = _uiState.value.filters
            if (filters != SearchFilters()) {
                // Dacă ștergem textul, dar avem filtre active, facem search pe baza filtrelor
                searchJob = viewModelScope.launch {
                    delay(500)
                    performSearch("")
                }
            } else {
                // Fără text, fără filtre -> Afișăm înapoi starea implicită
                _uiState.value = _uiState.value.copy(
                    searchResults = emptyList(),
                    hasSearched = false
                )
            }
        }
    }

    fun onSearchSubmit() {
        val query = _uiState.value.query
        // Permitem submit și dacă e gol, cu condiția să existe filtre
        if (query.isBlank() && _uiState.value.filters == SearchFilters()) return

        searchJob?.cancel()
        viewModelScope.launch {
            performSearch(query)
        }
    }

    private suspend fun performSearch(query: String) {
        val filters = _uiState.value.filters
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        // Salvăm în istoric doar dacă s-a scris efectiv un text
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
        // Declanșează automat căutarea cu noile filtre, chiar și cu query gol
        viewModelScope.launch {
            performSearch(_uiState.value.query)
        }
    }

    fun onClearFilters() {
        _uiState.value = _uiState.value.copy(filters = SearchFilters())
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
        private val gameRepository: GameRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(searchRepository, gameRepository) as T
        }
    }
}