package com.example.gamevault.ui.screens.home

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gamevault.R
import com.example.gamevault.data.remote.dto.GameDto
import com.example.gamevault.data.repository.GameRepository
import com.example.gamevault.data.repository.AuthRepository
import com.example.gamevault.ui.util.NetworkConnectivityObserver
import com.example.gamevault.ui.util.NetworkStatus
import kotlinx.coroutines.async
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
    val coop: List<GameDto> = emptyList(),
    val retro: List<GameDto> = emptyList(),
    val isLoading: Boolean = false,
    @param:StringRes val errorMessageRes: Int? = null
)

class HomeViewModel(
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository,
    applicationContext: Context
) : ViewModel() {

    private val appContext = applicationContext.applicationContext
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUsername()
        observeNetworkAndLoad()
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

    private fun observeNetworkAndLoad() {
        viewModelScope.launch {
            NetworkConnectivityObserver.observe(appContext).collect { status ->
                when (status) {
                    NetworkStatus.Available -> {
                        val state = _uiState.value
                        if (state.popularThisYear.isEmpty() || state.errorMessageRes != null) {
                            loadHomeData()
                        }
                    }
                    NetworkStatus.Unavailable -> {
                        _uiState.value = _uiState.value.copy(
                            popularThisYear = emptyList(),
                            allTimeLegends = emptyList(),
                            indieGems = emptyList(),
                            competitive = emptyList(),
                            coop = emptyList(),
                            retro = emptyList(),
                            isLoading = false,
                            errorMessageRes = R.string.fail_to_load_games
                        )
                    }
                }
            }
        }
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessageRes = null)

            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val dates = "$currentYear-01-01,$currentYear-12-31"

            val thisYearDef = async { gameRepository.getGamesThisYear(dates) }
            val allTimeDef = async { gameRepository.getAllTimeTopGames() }
            val indieDef = async { gameRepository.getGamesWithFilters(genres = "indie", pageSize = 10) }
            val competitiveDef = async { gameRepository.getGamesWithFilters(tags = "multiplayer,competitive", pageSize = 10) }
            val coopDef = async { gameRepository.getGamesWithFilters(tags = "co-op", pageSize = 10) }
            val retroDef = async { gameRepository.getGamesWithFilters(dates = "1980-01-01,2005-12-31", pageSize = 10) }

            val thisYearResult = thisYearDef.await()
            val allTimeResult = allTimeDef.await()
            val indieResult = indieDef.await()
            val competitiveResult = competitiveDef.await()
            val coopResult = coopDef.await()
            val retroResult = retroDef.await()

            _uiState.value = _uiState.value.copy(
                popularThisYear = thisYearResult.getOrElse { emptyList() },
                allTimeLegends = allTimeResult.getOrElse { emptyList() },
                indieGems = indieResult.getOrElse { emptyList() },
                competitive = competitiveResult.getOrElse { emptyList() },
                coop = coopResult.getOrElse { emptyList() },
                retro = retroResult.getOrElse { emptyList() },
                isLoading = false,
                errorMessageRes = if (thisYearResult.isFailure && allTimeResult.isFailure) {
                    R.string.fail_to_load_games
                } else null
            )
        }
    }

    class Factory(
        private val gameRepository: GameRepository,
        private val authRepository: AuthRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(gameRepository, authRepository, context) as T
        }
    }
}