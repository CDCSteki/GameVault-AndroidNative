package com.example.gamevault.ui.screens.gamelist

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gamevault.R
import com.example.gamevault.data.remote.dto.GameDto
import com.example.gamevault.data.repository.GameRepository
import com.example.gamevault.ui.navigation.NavRoutes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class GameListUiState(
    @param: StringRes val titleRes: Int = R.string.app_name,
    val games: List<GameDto> = emptyList(),
    val isLoading: Boolean = false,
    @param: StringRes val errorMessageRes: Int? = null,
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
                errorMessageRes = null,
                pageSize = pageSize
            )

            val cleanListType = listType.substringAfterLast("/")

            val titleRes = when (cleanListType) {
                NavRoutes.GameList.TYPE_THIS_YEAR -> R.string.home_popular_this_year
                NavRoutes.GameList.TYPE_ALL_TIME -> R.string.home_all_time_legends
                "discover_indie" -> R.string.discover_indie
                "discover_competitive" -> R.string.discover_competitive
                "discover_coop" -> R.string.discover_coop
                "discover_retro" -> R.string.discover_retro
                else -> R.string.app_name
            }

            val result = when (cleanListType) {
                NavRoutes.GameList.TYPE_THIS_YEAR -> {
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    gameRepository.getGamesThisYear(
                        dates = "$currentYear-01-01,$currentYear-12-31",
                        pageSize = pageSize
                    )
                }
                NavRoutes.GameList.TYPE_ALL_TIME -> {
                    gameRepository.getAllTimeTopGames(pageSize = pageSize)
                }
                "discover_indie" -> {
                    gameRepository.getGamesWithFilters(genres = "indie", pageSize = pageSize)
                }
                "discover_competitive" -> {
                    gameRepository.getGamesWithFilters(tags = "multiplayer,competitive", pageSize = pageSize)
                }
                "discover_coop" -> {
                    gameRepository.getGamesWithFilters(tags = "co-op", pageSize = pageSize)
                }
                "discover_retro" -> {
                    gameRepository.getGamesWithFilters(dates = "1980-01-01,2005-12-31", pageSize = pageSize)
                }
                else -> gameRepository.getPopularGames(pageSize = pageSize)
            }

            _uiState.value = _uiState.value.copy(
                titleRes = titleRes,
                games = result.getOrElse { emptyList() },
                isLoading = false,
                errorMessageRes = if (result.isFailure) R.string.fail_to_load_games else null
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