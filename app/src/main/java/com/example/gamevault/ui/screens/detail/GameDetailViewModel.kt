package com.example.gamevault.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gamevault.data.local.entity.GameEntity
import com.example.gamevault.data.local.entity.PlayStatus
import com.example.gamevault.data.remote.dto.GameDetailDto
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
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
    val isInCollection: Boolean = false,
    val isInWishlist: Boolean = false,
    val playStatus: PlayStatus = PlayStatus.NOT_PLAYED,
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

            val detailResult = gameRepository.getGameDetails(gameId)
            val screenshotsResult = gameRepository.getGameScreenshots(gameId)

            _uiState.value = _uiState.value.copy(
                gameDetail = detailResult.getOrNull(),
                screenshots = screenshotsResult.getOrNull()?.results ?: emptyList(),
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
                    playStatus = try {
                        PlayStatus.valueOf(localGame?.playStatus ?: PlayStatus.NOT_PLAYED.name)
                    } catch (e: Exception) {
                        PlayStatus.NOT_PLAYED
                    },
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
            val result = gameRepository.addToCollection(entity)
            when (result) {
                is GameRepository.AddToCollectionResult.Success ->
                    _uiState.value = _uiState.value.copy(
                        snackbarMessage = "Added to collection!"
                    )
                is GameRepository.AddToCollectionResult.AlreadyInCollection ->
                    _uiState.value = _uiState.value.copy(
                        snackbarMessage = "Already in your collection!"
                    )
            }
        }
    }

    fun onRemoveFromCollection() {
        viewModelScope.launch {
            gameRepository.removeFromCollection(gameId)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Removed from collection"
            )
        }
    }

    fun onAddToWishlist() {
        viewModelScope.launch {
            val detail = _uiState.value.gameDetail ?: return@launch
            val entity = with(gameRepository) { detail.toEntity() }
            val result = gameRepository.addToWishlist(entity)
            when (result) {
                is GameRepository.AddToWishlistResult.Success ->
                    _uiState.value = _uiState.value.copy(
                        snackbarMessage = "Added to wishlist!"
                    )
                is GameRepository.AddToWishlistResult.AlreadyInWishlist ->
                    _uiState.value = _uiState.value.copy(
                        snackbarMessage = "Already in your wishlist!"
                    )
                is GameRepository.AddToWishlistResult.AlreadyInCollection ->
                    _uiState.value = _uiState.value.copy(
                        snackbarMessage = "This game is already in your collection!"
                    )
            }
        }
    }

    fun onRemoveFromWishlist() {
        viewModelScope.launch {
            gameRepository.removeFromWishlist(gameId)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Removed from wishlist"
            )
        }
    }

    fun onPlayStatusChange(status: PlayStatus) {
        viewModelScope.launch {
            gameRepository.updatePlayStatus(gameId, status)
        }
    }

    fun onRatingChange(rating: Float) {
        _uiState.value = _uiState.value.copy(userRating = rating)
        viewModelScope.launch {
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

    fun onSnackbarDismissed() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
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