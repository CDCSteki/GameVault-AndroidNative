package com.example.gamevault.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gamevault.data.local.entity.GameEntity
import com.example.gamevault.data.remote.dto.GameDetailDto
import com.example.gamevault.data.remote.dto.GameMovieDto
import com.example.gamevault.data.remote.dto.GameScreenshotDto
import com.example.gamevault.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GameDetailUiState(
    val gameDetail: GameDetailDto? = null,
    val localGame: GameEntity? = null,
    val screenshots: List<GameScreenshotDto> = emptyList(),
    val trailers: List<GameMovieDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isInCollection: Boolean = false,
    val isInWishlist: Boolean = false,
    val isPlayed: Boolean = false,
    val userRating: Float = 0f,
    val userNotes: String = "",
    val showNotesDialog: Boolean = false
)

class GameDetailViewModel(
    private val gameRepository: GameRepository,
    private val gameId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameDetailUiState())
    val uiState: StateFlow<GameDetailUiState> = _uiState.asStateFlow()

    init {
        loadGameDetail()
        observeLocalGame()
    }

    private fun loadGameDetail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Load detail, screenshots, trailers in parallel
            val detailResult = gameRepository.getGameDetails(gameId)
            val screenshotsResult = gameRepository.getGameScreenshots(gameId)
            val moviesResult = gameRepository.getGameMovies(gameId)

            _uiState.value = _uiState.value.copy(
                gameDetail = detailResult.getOrNull(),
                screenshots = screenshotsResult.getOrNull()?.results ?: emptyList(),
                trailers = moviesResult.getOrNull()?.results ?: emptyList(),
                isLoading = false,
                errorMessage = if (detailResult.isFailure) "Failed to load game details." else null
            )
        }
    }

    private fun observeLocalGame() {
        viewModelScope.launch {
            gameRepository.observeGameById(gameId).collect { localGame ->
                _uiState.value = _uiState.value.copy(
                    localGame = localGame,
                    isInCollection = localGame?.isInCollection ?: false,
                    isInWishlist = localGame?.isInWishlist ?: false,
                    isPlayed = localGame?.isPlayed ?: false,
                    userRating = localGame?.userRating ?: 0f,
                    userNotes = localGame?.userNotes ?: ""
                )
            }
        }
    }

    fun onAddToCollection() {
        viewModelScope.launch {
            val detail = _uiState.value.gameDetail ?: return@launch
            val entity = with(gameRepository) { detail.toEntity() }
            gameRepository.addToCollection(entity)
        }
    }

    fun onRemoveFromCollection() {
        viewModelScope.launch {
            gameRepository.removeFromCollection(gameId)
        }
    }

    fun onAddToWishlist() {
        viewModelScope.launch {
            val detail = _uiState.value.gameDetail ?: return@launch
            val entity = with(gameRepository) { detail.toEntity() }
            gameRepository.addToWishlist(entity)
        }
    }

    fun onRemoveFromWishlist() {
        viewModelScope.launch {
            gameRepository.removeFromWishlist(gameId)
        }
    }

    fun onTogglePlayedStatus() {
        viewModelScope.launch {
            val newStatus = !_uiState.value.isPlayed
            gameRepository.updatePlayedStatus(gameId, newStatus)
        }
    }

    fun onRatingChange(rating: Float) {
        _uiState.value = _uiState.value.copy(userRating = rating)
        viewModelScope.launch {
            // Daca jocul nu e salvat local, il salvam intai
            if (_uiState.value.localGame == null) {
                val detail = _uiState.value.gameDetail ?: return@launch
                val entity = with(gameRepository) { detail.toEntity() }
                gameRepository.addToCollection(entity)
            }
            gameRepository.updateUserRating(gameId, rating)
        }
    }

    fun onNotesChange(notes: String) {
        _uiState.value = _uiState.value.copy(userNotes = notes)
    }

    fun onSaveNotes() {
        viewModelScope.launch {
            gameRepository.updateUserNotes(gameId, _uiState.value.userNotes)
            _uiState.value = _uiState.value.copy(showNotesDialog = false)
        }
    }

    fun onToggleNotesDialog() {
        _uiState.value = _uiState.value.copy(
            showNotesDialog = !_uiState.value.showNotesDialog
        )
    }

    fun retry() {
        loadGameDetail()
    }

    class Factory(
        private val gameRepository: GameRepository,
        private val gameId: Int
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return GameDetailViewModel(gameRepository, gameId) as T
        }
    }
}