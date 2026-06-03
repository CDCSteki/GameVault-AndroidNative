package com.example.gamevault.ui.screens.gamelist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.gamevault.data.remote.dto.GameDto
import com.example.gamevault.data.repository.GameRepository
import com.example.gamevault.ui.theme.*
import java.util.Locale

@Composable
fun GameListScreen(
    listType: String,
    gameRepository: GameRepository,
    onGameClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val viewModel: GameListViewModel = viewModel(
        factory = GameListViewModel.Factory(gameRepository, listType)
    )
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GVTheme.colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            GameListTopBar(
                title = uiState.title,
                onBackClick = onBackClick
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GVTheme.colors.accent)
                    }
                }

                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = StatusRed,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = uiState.errorMessage!!,
                                color = GVTheme.colors.textSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = viewModel::retry,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GVTheme.colors.accent
                                )
                            ) {
                                Text("Retry", color = GVTheme.colors.textPrimary)
                            }
                        }
                    }
                }

                uiState.games.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No games found",
                            color = GVTheme.colors.textMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header cu count + page size selector
                        item {
                            PageSizeSelector(
                                currentSize = uiState.pageSize,
                                totalShown = uiState.games.size,
                                onSizeChange = viewModel::onPageSizeChange
                            )
                        }

                        items(uiState.games) { game ->
                            GameListCard(
                                game = game,
                                onClick = { onGameClick(game.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PageSizeSelector(
    currentSize: Int,
    totalShown: Int,
    onSizeChange: (Int) -> Unit
) {
    val options = listOf(10, 20, 40)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Showing $totalShown titles",
                style = MaterialTheme.typography.labelSmall,
                color = GVTheme.colors.accent,
                letterSpacing = 1.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Show:",
                    style = MaterialTheme.typography.labelSmall,
                    color = GVTheme.colors.textMuted
                )
                options.forEach { size ->
                    val isSelected = currentSize == size
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isSelected) GVTheme.colors.accent
                                else GVTheme.colors.card
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) GVTheme.colors.accent else GVTheme.colors.border,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { if (!isSelected) onSizeChange(size) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "$size",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) GVTheme.colors.textPrimary else GVTheme.colors.textMuted,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = GVTheme.colors.border.copy(alpha = 0.3f))
    }
}

@Composable
private fun GameListTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    val colors = GVTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(colors.accent.copy(alpha = 0.25f), GVTheme.colors.background)
                )
            )
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = GVTheme.colors.card,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = GVTheme.colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(GVTheme.colors.accent, GVTheme.colors.accentSecondary)
                    )
                ),
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun GameListCard(
    game: GameDto,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = GVTheme.colors.border, shape = RoundedCornerShape(12.dp))
            .background(GVTheme.colors.card)
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {

            AsyncImage(
                model = game.backgroundImage,
                contentDescription = game.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        game.genres?.firstOrNull()?.let { genre ->
                            BadgeChip(text = genre.name.uppercase())
                        }
                        game.platforms?.firstOrNull()?.platform?.let { platform ->
                            BadgeChip(
                                text = platform.name,
                                color = GVTheme.colors.accentSecondary.copy(alpha = 0.15f),
                                textColor = GVTheme.colors.accentSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = game.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = GVTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = StatusYellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f", game.rating),
                            style = MaterialTheme.typography.labelMedium,
                            color = GVTheme.colors.textSecondary
                        )
                    }

                    game.playtime?.let { playtime ->
                        if (playtime > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = GVTheme.colors.textMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${playtime}h",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GVTheme.colors.textMuted
                                )
                            }
                        }
                    }

                    game.released?.take(4)?.let { year ->
                        Text(
                            text = year,
                            style = MaterialTheme.typography.labelSmall,
                            color = GVTheme.colors.textMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeChip(
    text: String,
    color: Color = GVTheme.colors.accent.copy(alpha = 0.15f),
    textColor: Color = GVTheme.colors.accent
) {
    Box(
        modifier = Modifier
            .background(color = color, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}