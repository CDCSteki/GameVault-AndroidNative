package com.example.gamevault.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.gamevault.data.remote.dto.GameDto
import com.example.gamevault.data.repository.AuthRepository
import com.example.gamevault.data.repository.GameRepository
import com.example.gamevault.ui.navigation.NavRoutes
import com.example.gamevault.ui.theme.*

@Composable
fun HomeScreen(
    gameRepository: GameRepository,
    authRepository: AuthRepository,
    onGameClick: (Int) -> Unit,
    onViewAllClick: (String) -> Unit
) {
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(gameRepository, authRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GVTheme.colors.background)
    ) {
        if (uiState.isLoading && uiState.popularThisYear.isEmpty()) {
            CircularProgressIndicator(
                color = GVTheme.colors.accent,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Header
                item {
                    HomeHeader(username = uiState.username)
                }

                // Popular This Year
                if (uiState.popularThisYear.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Popular This Year",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            onViewAll = {
                                onViewAllClick(NavRoutes.GameList.TYPE_THIS_YEAR)
                            }
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.popularThisYear) { game ->
                                GameCardMedium(
                                    game = game,
                                    badge = if (game == uiState.popularThisYear.first()) "ONLINE" else if (game == uiState.popularThisYear[1]) "TRENDING" else null,
                                    onClick = { onGameClick(game.id) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // All Time Legends
                if (uiState.allTimeLegends.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "All-Time Legends",
                            icon = Icons.Default.EmojiEvents,
                            onViewAll = {
                                onViewAllClick(NavRoutes.GameList.TYPE_ALL_TIME)
                            }
                        )
                    }
                    item {
                        AllTimeLegendCard(
                            game = uiState.allTimeLegends.first(),
                            onClick = { onGameClick(uiState.allTimeLegends.first().id) }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Discover Section
                if (uiState.errorMessage == null && (uiState.indieGems.isNotEmpty() || uiState.competitive.isNotEmpty() || uiState.coop.isNotEmpty() || uiState.retro.isNotEmpty())) {
                    item {
                        SectionHeader(
                            title = "Discover",
                            icon = Icons.Default.Star,
                            onViewAll = null
                        )
                    }
                    item {
                        DiscoverGrid(
                            indieGames = uiState.indieGems,
                            competitiveGames = uiState.competitive,
                            coOpGames = uiState.coop,
                            retroGames = uiState.retro,
                            onViewAllGenre = { route -> onViewAllClick(route) }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Error
                if (uiState.errorMessage != null) {
                    item {
                        Text(
                            text = uiState.errorMessage!!,
                            color = StatusRed,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(username: String) {
    val colors = GVTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colors.accent.copy(alpha = 0.25f),
                        GVTheme.colors.background
                    )
                )
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = GVTheme.colors.accent,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "GAMEVAULT",
                    style = MaterialTheme.typography.titleMedium.copy(
                        brush = Brush.linearGradient(
                            colors = listOf(GVTheme.colors.accent, GVTheme.colors.accentSecondary)
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome,",
                style = MaterialTheme.typography.headlineSmall,
                color = GVTheme.colors.textSecondary
            )
            Text(
                text = username,
                style = MaterialTheme.typography.headlineLarge.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(GVTheme.colors.accent, GVTheme.colors.accentSecondary)
                    ),
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Your next legendary adventure is just a click away.",
                style = MaterialTheme.typography.bodySmall,
                color = GVTheme.colors.textMuted
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    onViewAll: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GVTheme.colors.accent,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = GVTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        if (onViewAll != null) {
            TextButton(onClick = onViewAll) {
                Text(
                    text = "VIEW ALL",
                    style = MaterialTheme.typography.labelSmall,
                    color = GVTheme.colors.accent,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun GameCardMedium(
    game: GameDto,
    badge: String? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(150.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = game.backgroundImage,
            contentDescription = game.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        // Badge
        if (badge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        color = if (badge == "ONLINE") GVTheme.colors.accentSecondary else GVTheme.colors.accent,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = GVTheme.colors.background,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Game info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {
            Text(
                text = game.name,
                style = MaterialTheme.typography.labelLarge,
                color = GVTheme.colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = game.genres?.firstOrNull()?.name?.uppercase() ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = GVTheme.colors.textMuted
            )
        }
    }
}

@Composable
private fun AllTimeLegendCard(
    game: GameDto,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = game.backgroundImage,
            contentDescription = game.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.85f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Hall of Fame badge
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = StatusYellow.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "HALL OF FAME",
                    style = MaterialTheme.typography.labelSmall,
                    color = GVTheme.colors.background,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = game.name,
                style = MaterialTheme.typography.headlineSmall,
                color = GVTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = game.genres?.firstOrNull()?.name ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = GVTheme.colors.textSecondary
            )
        }
    }
}

@Composable
private fun DiscoverGrid(
    indieGames: List<GameDto>,
    competitiveGames: List<GameDto>,
    coOpGames: List<GameDto>,
    retroGames: List<GameDto>,
    onViewAllGenre: (String) -> Unit
) {
    val categories = listOf(
        DiscoverCategory("Indie Gems", GVTheme.colors.accent, indieGames, "discover_indie"),
        DiscoverCategory("Competitive", GVTheme.colors.accentSecondary, competitiveGames, "discover_competitive"),
        DiscoverCategory("Co-op", StatusGreen, coOpGames, "discover_coop"),
        DiscoverCategory("Retro", StatusOrange, retroGames, "discover_retro")
    )

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        categories.chunked(2).forEach { rowCategories ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowCategories.forEach { category ->
                    DiscoverCategoryCard(
                        category = category,
                        onViewAll = { onViewAllGenre(category.genre) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

data class DiscoverCategory(
    val name: String,
    val color: Color,
    val games: List<GameDto>,
    val genre: String
)

@Composable
private fun DiscoverCategoryCard(
    category: DiscoverCategory,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = category.color.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(GVTheme.colors.card)
            .clickable(onClick = onViewAll)
    ) {
        if (category.games.isNotEmpty()) {
            AsyncImage(
                model = category.games.firstOrNull()?.backgroundImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.3f
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, GVTheme.colors.card.copy(alpha = 0.7f))
                    )
                )
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = category.color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = category.color.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (category.name) {
                        "Indie Gems" -> "◈"
                        "Competitive" -> "⚡"
                        "Co-Op" -> "◎"
                        else -> "◀"
                    },
                    color = category.color,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelMedium,
                color = GVTheme.colors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}