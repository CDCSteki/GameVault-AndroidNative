package com.example.gamevault.ui.screens.gamelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gamevault.data.remote.dto.GameDto
import com.example.gamevault.data.repository.GameRepository
import com.example.gamevault.ui.navigation.NavRoutes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class GameListUiState(
    val title: String = "",
    val games: List<GameDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val pageSize: Int = 10
)

class GameListViewModel(
    private val gameRepository: GameRepository,
    private val listType: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameListUiState())
    val uiState: StateFlow<GameListUiState> = _uiState.asStateFlow()

    init {
        loadGames()
    }

    fun loadGames(pageSize: Int = _uiState.value.pageSize) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                pageSize = pageSize
            )

            val title = when {
                listType == NavRoutes.GameList.TYPE_THIS_YEAR -> "Popular This Year"
                listType == NavRoutes.GameList.TYPE_ALL_TIME -> "All-Time Legends"
                listType.startsWith("genre_") -> listType
                    .removePrefix("genre_")
                    .replaceFirstChar { it.uppercase() }
                else -> "Games"
            }

            val result = when {
                listType == NavRoutes.GameList.TYPE_THIS_YEAR -> {
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    gameRepository.getGamesThisYear(
                        dates = "$currentYear-01-01,$currentYear-12-31",
                        pageSize = pageSize
                    )
                }
                listType == NavRoutes.GameList.TYPE_ALL_TIME -> {
                    gameRepository.getAllTimeTopGames(pageSize = pageSize)
                }
                listType.startsWith("genre_") -> {
                    val genre = listType.removePrefix("genre_")
                    gameRepository.getGamesByGenre(
                        genre = genre,
                        pageSize = pageSize
                    )
                }
                else -> gameRepository.getPopularGames(pageSize = pageSize)
            }

            _uiState.value = _uiState.value.copy(
                title = title,
                games = result.getOrElse { emptyList() },
                isLoading = false,
                errorMessage = if (result.isFailure) "Failed to load games." else null
            )
        }
    }

    fun onPageSizeChange(newSize: Int) {
        loadGames(pageSize = newSize)
    }

    fun retry() = loadGames()

    class Factory(
        private val gameRepository: GameRepository,
        private val listType: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return GameListViewModel(gameRepository, listType) as T
        }
    }
}