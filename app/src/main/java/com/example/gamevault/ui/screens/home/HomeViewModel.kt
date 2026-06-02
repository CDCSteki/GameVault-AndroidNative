package com.example.gamevault.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gamevault.data.remote.dto.GameDto
import com.example.gamevault.data.repository.GameRepository
import com.example.gamevault.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val username: String = "Hunter",
    val popularThisYear: List<GameDto> = emptyList(),
    val allTimeLegends: List<GameDto> = emptyList(),
    val indieGems: List<GameDto> = emptyList(),
    val competitive: List<GameDto> = emptyList(),
    val coOp: List<GameDto> = emptyList(),
    val retro: List<GameDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUsername()
        loadHomeData()
    }

    private fun loadUsername() {
        viewModelScope.launch {
            authRepository.loggedInUserId.collect { userId ->
                if (userId != -1) {
                    authRepository.getUserById(userId).collect { user ->
                        _uiState.value = _uiState.value.copy(
                            username = user?.username ?: "Hunter"
                        )
                    }
                }
            }
        }
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val dates = "$currentYear-01-01,$currentYear-12-31"

            val thisYearResult = gameRepository.getGamesThisYear(dates)
            val allTimeResult = gameRepository.getAllTimeTopGames()
            val indieResult = gameRepository.getGamesByGenre("indie")
            val competitiveResult = gameRepository.getGamesByGenre("shooter")
            val coOpResult = gameRepository.getGamesByGenre("action")
            val retroResult = gameRepository.getGamesByGenre("arcade")

            _uiState.value = _uiState.value.copy(
                popularThisYear = thisYearResult.getOrElse { emptyList() },
                allTimeLegends = allTimeResult.getOrElse { emptyList() },
                indieGems = indieResult.getOrElse { emptyList() },
                competitive = competitiveResult.getOrElse { emptyList() },
                coOp = coOpResult.getOrElse { emptyList() },
                retro = retroResult.getOrElse { emptyList() },
                isLoading = false,
                errorMessage = if (thisYearResult.isFailure && allTimeResult.isFailure) {
                    "Failed to load games. Check your connection."
                } else null
            )
        }
    }

    class Factory(
        private val gameRepository: GameRepository,
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(gameRepository, authRepository) as T
        }
    }
}