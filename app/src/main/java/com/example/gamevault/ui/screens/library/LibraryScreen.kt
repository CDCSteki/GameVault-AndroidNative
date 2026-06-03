package com.example.gamevault.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.gamevault.data.local.entity.GameEntity
import com.example.gamevault.data.local.entity.PlayStatus
import com.example.gamevault.data.repository.GameRepository
import com.example.gamevault.ui.components.GameVaultTopBar
import com.example.gamevault.ui.theme.*
import com.example.gamevault.ui.util.firstGenre
import com.example.gamevault.ui.util.firstPlatform

@Composable
fun LibraryScreen(
    gameRepository: GameRepository,
    onGameClick: (Int) -> Unit
) {
    val viewModel: LibraryViewModel = viewModel(
        factory = LibraryViewModel.Factory(gameRepository)
    )
    val uiState by viewModel.uiState.collectAsState()
    val filteredCollection = viewModel.getFilteredCollection()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GVTheme.colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            GameVaultTopBar()

            LibraryTabs(
                activeTab = uiState.activeTab,
                onTabChange = viewModel::onTabChange
            )

            if (uiState.activeTab == LibraryTab.COLLECTION) {
                CollectionFilterRow(
                    activeFilter = uiState.collectionFilter,
                    onFilterChange = viewModel::onCollectionFilterChange
                )
            }

            if (uiState.activeTab == LibraryTab.COLLECTION) {
                if (filteredCollection.isEmpty()) {
                    EmptyLibraryMessage(
                        message = when (uiState.collectionFilter) {
                            CollectionFilter.PLAYING -> "No games currently being played"
                            CollectionFilter.PLAYED -> "No played games yet"
                            CollectionFilter.NOT_PLAYED -> "No unplayed games"
                            CollectionFilter.ALL -> "Your collection is empty.\nSearch for games to add!"
                        }
                    )
                } else {
                    CollectionList(
                        games = filteredCollection,
                        onGameClick = onGameClick,
                        onPlayStatusChange = { game, status ->
                            viewModel.onPlayStatusChange(game.rawgId, status)
                        },
                        onRemove = { viewModel.onRemoveFromCollection(it.rawgId) }
                    )
                }
            } else {
                if (uiState.wishlist.isEmpty()) {
                    EmptyLibraryMessage(
                        message = "Your wishlist is empty.\nAdd games you want to play!"
                    )
                } else {
                    WishlistList(
                        games = uiState.wishlist,
                        onGameClick = onGameClick,
                        onMoveToCollection = { viewModel.onMoveToCollection(it) },
                        onRemove = { viewModel.onRemoveFromWishlist(it.rawgId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryTabs(
    activeTab: LibraryTab,
    onTabChange: (LibraryTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LibraryTab.entries.forEach { tab ->
            val isSelected = activeTab == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) GVTheme.colors.accent else GVTheme.colors.card)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) GVTheme.colors.accent else GVTheme.colors.border,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onTabChange(tab) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (tab) {
                        LibraryTab.COLLECTION -> "MY COLLECTION"
                        LibraryTab.WISHLIST -> "WISHLIST"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) GVTheme.colors.textPrimary else GVTheme.colors.textMuted,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CollectionFilterRow(
    activeFilter: CollectionFilter,
    onFilterChange: (CollectionFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CollectionFilter.entries.forEach { filter ->
            val isSelected = activeFilter == filter
            val filterColor = when (filter) {
                CollectionFilter.ALL -> GVTheme.colors.accent
                CollectionFilter.PLAYING -> GVTheme.colors.accentSecondary
                CollectionFilter.PLAYED -> StatusGreen
                CollectionFilter.NOT_PLAYED -> StatusOrange
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) filterColor.copy(alpha = 0.15f)
                        else Color.Transparent
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) filterColor else GVTheme.colors.border,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onFilterChange(filter) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(
                    text = when (filter) {
                        CollectionFilter.ALL -> "All"
                        CollectionFilter.PLAYING -> "Playing"
                        CollectionFilter.PLAYED -> "Played"
                        CollectionFilter.NOT_PLAYED -> "Not Played"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) filterColor else GVTheme.colors.textMuted,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun CollectionList(
    games: List<GameEntity>,
    onGameClick: (Int) -> Unit,
    onPlayStatusChange: (GameEntity, PlayStatus) -> Unit,
    onRemove: (GameEntity) -> Unit
) {
    Text(
        text = "${games.size} Games in library",
        style = MaterialTheme.typography.bodySmall,
        color = GVTheme.colors.textMuted,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(games, key = { it.rawgId }) { game ->
            CollectionGameCard(
                game = game,
                onClick = { onGameClick(game.rawgId) },
                onPlayStatusChange = { status -> onPlayStatusChange(game, status) },
                onRemove = { onRemove(game) }
            )
        }
    }
}

@Composable
private fun CollectionGameCard(
    game: GameEntity,
    onClick: () -> Unit,
    onPlayStatusChange: (PlayStatus) -> Unit,
    onRemove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val currentStatus = try {
        PlayStatus.valueOf(game.playStatus)
    } catch (_: Exception) {
        PlayStatus.NOT_PLAYED
    }

    val statusColor = when (currentStatus) {
        PlayStatus.NOT_PLAYED -> StatusOrange
        PlayStatus.PLAYING -> GVTheme.colors.accentSecondary
        PlayStatus.PLAYED -> StatusGreen
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = GVTheme.colors.border, shape = RoundedCornerShape(12.dp))
            .background(GVTheme.colors.card)
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {

            // Cover image
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .fillMaxHeight()
            ) {
                AsyncImage(
                    model = game.coverImageUrl,
                    contentDescription = game.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                game.platforms.firstPlatform().takeIf { it.isNotEmpty() }?.let { platform ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(4.dp)
                            .background(
                                color = GVTheme.colors.background.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = platform.take(8),
                            style = MaterialTheme.typography.labelSmall,
                            color = GVTheme.colors.textSecondary,
                            fontSize = 8.sp
                        )
                    }
                }
            }

            // Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = game.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = GVTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = game.genres.firstGenre().uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = GVTheme.colors.textMuted
                    )
                }

                // Play status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(
                            width = 1.dp,
                            color = statusColor,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when (currentStatus) {
                                PlayStatus.NOT_PLAYED -> Icons.Default.RadioButtonUnchecked
                                PlayStatus.PLAYING -> Icons.Default.PlayCircle
                                PlayStatus.PLAYED -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = when (currentStatus) {
                                PlayStatus.NOT_PLAYED -> "Not Played"
                                PlayStatus.PLAYING -> "Playing"
                                PlayStatus.PLAYED -> "Played"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Dropdown Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = GVTheme.colors.textMuted
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(GVTheme.colors.card)
                ) {
                    PlayStatus.entries.forEach { status ->
                        if (status != currentStatus) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = when (status) {
                                            PlayStatus.NOT_PLAYED -> "Mark as Not Played"
                                            PlayStatus.PLAYING -> "Mark as Playing"
                                            PlayStatus.PLAYED -> "Mark as Played"
                                        },
                                        color = GVTheme.colors.textPrimary
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onPlayStatusChange(status)
                                }
                            )
                        }
                    }
                    HorizontalDivider(color = GVTheme.colors.border.copy(alpha = 0.3f))
                    DropdownMenuItem(
                        text = { Text("Remove", color = StatusRed) },
                        onClick = {
                            showMenu = false
                            onRemove()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WishlistList(
    games: List<GameEntity>,
    onGameClick: (Int) -> Unit,
    onMoveToCollection: (GameEntity) -> Unit,
    onRemove: (GameEntity) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(games, key = { it.rawgId }) { game ->
            WishlistGameCard(
                game = game,
                onClick = { onGameClick(game.rawgId) },
                onMoveToCollection = { onMoveToCollection(game) },
                onRemove = { onRemove(game) }
            )
        }
    }
}

@Composable
private fun WishlistGameCard(
    game: GameEntity,
    onClick: () -> Unit,
    onMoveToCollection: () -> Unit,
    onRemove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = GVTheme.colors.border, shape = RoundedCornerShape(12.dp))
            .background(GVTheme.colors.card)
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = game.coverImageUrl,
                contentDescription = game.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(90.dp)
                    .fillMaxHeight()
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = game.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = GVTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = game.genres.firstGenre().uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = GVTheme.colors.textMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GVTheme.colors.accent.copy(alpha = 0.15f))
                        .border(
                            width = 1.dp,
                            color = GVTheme.colors.accent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable(onClick = onMoveToCollection)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "+ Add to Collection",
                        style = MaterialTheme.typography.labelSmall,
                        color = GVTheme.colors.accent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = GVTheme.colors.textMuted
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(GVTheme.colors.card)
                ) {
                    DropdownMenuItem(
                        text = { Text("Add to Collection", color = GVTheme.colors.textPrimary) },
                        onClick = {
                            showMenu = false
                            onMoveToCollection()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Remove", color = StatusRed) },
                        onClick = {
                            showMenu = false
                            onRemove()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyLibraryMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.VideoLibrary,
                contentDescription = null,
                tint = GVTheme.colors.textMuted,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = GVTheme.colors.textMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}