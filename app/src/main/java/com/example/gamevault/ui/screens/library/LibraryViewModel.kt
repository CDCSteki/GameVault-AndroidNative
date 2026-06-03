package com.example.gamevault.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gamevault.data.local.entity.GameEntity
import com.example.gamevault.data.local.entity.PlayStatus
import com.example.gamevault.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class LibraryTab { COLLECTION, WISHLIST }
enum class CollectionFilter { ALL, PLAYING, PLAYED, NOT_PLAYED }

data class LibraryUiState(
    val activeTab: LibraryTab = LibraryTab.COLLECTION,
    val collectionFilter: CollectionFilter = CollectionFilter.ALL,
    val collection: List<GameEntity> = emptyList(),
    val wishlist: List<GameEntity> = emptyList(),
    val isLoading: Boolean = false
)

class LibraryViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        observeCollection()
        observeWishlist()
    }

    private fun observeCollection() {
        viewModelScope.launch {
            gameRepository.getCollection().collect { games ->
                _uiState.value = _uiState.value.copy(collection = games)
            }
        }
    }

    private fun observeWishlist() {
        viewModelScope.launch {
            gameRepository.getWishlist().collect { games ->
                _uiState.value = _uiState.value.copy(wishlist = games)
            }
        }
    }

    fun onTabChange(tab: LibraryTab) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }

    fun onCollectionFilterChange(filter: CollectionFilter) {
        _uiState.value = _uiState.value.copy(collectionFilter = filter)
    }

    fun onPlayStatusChange(gameId: Int, status: PlayStatus) {
        viewModelScope.launch {
            gameRepository.updatePlayStatus(gameId, status)
        }
    }

    fun onRemoveFromCollection(gameId: Int) {
        viewModelScope.launch {
            gameRepository.removeFromCollection(gameId)
        }
    }

    fun onRemoveFromWishlist(gameId: Int) {
        viewModelScope.launch {
            gameRepository.removeFromWishlist(gameId)
        }
    }

    fun onMoveToCollection(game: GameEntity) {
        viewModelScope.launch {
            gameRepository.addToCollection(game)
        }
    }

    fun getFilteredCollection(): List<GameEntity> {
        val state = _uiState.value
        return when (state.collectionFilter) {
            CollectionFilter.ALL -> state.collection
            CollectionFilter.PLAYING -> state.collection.filter {
                it.playStatus == PlayStatus.PLAYING.name
            }
            CollectionFilter.PLAYED -> state.collection.filter {
                it.playStatus == PlayStatus.PLAYED.name
            }
            CollectionFilter.NOT_PLAYED -> state.collection.filter {
                it.playStatus == PlayStatus.NOT_PLAYED.name
            }
        }
    }

    class Factory(private val gameRepository: GameRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return LibraryViewModel(gameRepository) as T
        }
    }
}