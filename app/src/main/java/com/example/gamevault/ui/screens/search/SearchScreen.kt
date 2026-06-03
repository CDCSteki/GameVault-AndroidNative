package com.example.gamevault.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.gamevault.data.remote.dto.GameDto
import com.example.gamevault.data.repository.GameRepository
import com.example.gamevault.data.repository.SearchRepository
import com.example.gamevault.ui.components.GameVaultTopBar
import com.example.gamevault.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchRepository: SearchRepository,
    gameRepository: GameRepository,
    onGameClick: (Int) -> Unit
) {
    val viewModel: SearchViewModel = viewModel(
        factory = SearchViewModel.Factory(searchRepository, gameRepository)
    )
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Bar
            GameVaultTopBar()

            // Search Field
            SearchField(
                query = uiState.query,
                onQueryChange = viewModel::onQueryChange,
                onSearchSubmit = {
                    focusManager.clearFocus()
                    viewModel.onSearchSubmit()
                }
            )

            // Filter Button
            FilterButton(
                hasActiveFilters = uiState.filters != SearchFilters(),
                onClick = viewModel::onToggleFilterSheet
            )

            // Content
            if (!uiState.hasSearched && uiState.query.isEmpty()) {
                SearchHistorySection(
                    history = uiState.searchHistory,
                    onHistoryClick = { query ->
                        viewModel.onHistoryItemClick(query)
                    },
                    onDeleteItem = viewModel::onDeleteHistoryItem,
                    onClearAll = viewModel::onClearHistory
                )
            } else if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NeonPurple)
                }
            } else {
                SearchResultsSection(
                    results = uiState.searchResults,
                    onGameClick = onGameClick
                )
            }
        }

        // Filter Bottom Sheet
        if (uiState.isFilterSheetVisible) {
            FilterBottomSheet(
                currentFilters = uiState.filters,
                onApply = viewModel::onApplyFilters,
                onDismiss = viewModel::onToggleFilterSheet,
                onClear = viewModel::onClearFilters
            )
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = {
            Text(
                text = "Search titles, genres...",
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = BorderCyan,
            focusedContainerColor = DarkCard,
            unfocusedContainerColor = DarkCard,
            cursorColor = NeonCyan
        )
    )
}

@Composable
private fun FilterButton(
    hasActiveFilters: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(NeonPurple, Color(0xFF6A0DAD))
                    ),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (hasActiveFilters) "FILTER (ACTIVE)" else "FILTER",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun SearchHistorySection(
    history: List<com.example.gamevault.data.local.entity.SearchHistoryEntity>,
    onHistoryClick: (String) -> Unit,
    onDeleteItem: (Int) -> Unit,
    onClearAll: () -> Unit
) {
    if (history.isEmpty()) return

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Searches",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onClearAll) {
                Text(
                    text = "CLEAR ALL",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonPurple
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        history.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHistoryClick(item.query) }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = item.query,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                IconButton(
                    onClick = { onDeleteItem(item.id) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            HorizontalDivider(color = BorderCyan.copy(alpha = 0.3f))
        }
    }
}

@Composable
private fun SearchResultsSection(
    results: List<GameDto>,
    onGameClick: (Int) -> Unit
) {
    if (results.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.SearchOff,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No results found",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextMuted
                )
            }
        }
        return
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "Search Results",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${results.size} TITLES FOUND",
            style = MaterialTheme.typography.labelSmall,
            color = NeonPurple,
            letterSpacing = 1.sp
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(results) { game ->
            SearchResultCard(
                game = game,
                onClick = { onGameClick(game.id) }
            )
        }
    }
}

@Composable
private fun SearchResultCard(
    game: GameDto,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = BorderCyan,
                shape = RoundedCornerShape(12.dp)
            )
            .background(DarkCard)
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Cover image
            AsyncImage(
                model = game.backgroundImage,
                contentDescription = game.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
            )

            // Info
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Genre + Platform badges
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        game.genres?.firstOrNull()?.let { genre ->
                            BadgeChip(text = genre.name.uppercase())
                        }
                        game.platforms?.firstOrNull()?.platform?.let { platform ->
                            BadgeChip(
                                text = platform.name,
                                color = NeonCyan.copy(alpha = 0.15f),
                                textColor = NeonCyan
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = game.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                            color = TextSecondary
                        )
                    }
                    game.playtime?.let { playtime ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${playtime}h Playtime",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeChip(
    text: String,
    color: Color = NeonPurple.copy(alpha = 0.15f),
    textColor: Color = NeonPurple
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    currentFilters: SearchFilters,
    onApply: (SearchFilters) -> Unit,
    onDismiss: () -> Unit,
    onClear: () -> Unit
) {
    var selectedGenre by remember { mutableStateOf(currentFilters.genre) }
    var selectedPlatform by remember { mutableStateOf(currentFilters.platform) }
    var selectedYear by remember { mutableStateOf(currentFilters.year ?: "") }
    var selectedOrdering by remember { mutableStateOf(currentFilters.ordering) }

    val genres = listOf("action", "rpg", "shooter", "strategy", "indie", "adventure", "puzzle")
    val platforms = listOf("4" to "PC", "187" to "PS5", "18" to "PS4", "1" to "Xbox", "7" to "Nintendo")
    val orderings = listOf("-rating" to "Best Rated", "-released" to "Newest", "-added" to "Most Popular")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkNavySecondary,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        color = BorderCyan,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp)
        ) {
            Text(
                text = "Filter Games",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Genre filter
            FilterSectionTitle("Genre")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(genres) { genre ->
                    FilterChipItem(
                        text = genre.replaceFirstChar { it.uppercase() },
                        isSelected = selectedGenre == genre,
                        onClick = {
                            selectedGenre = if (selectedGenre == genre) null else genre
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Platform filter
            FilterSectionTitle("Platform")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(platforms) { (id, name) ->
                    FilterChipItem(
                        text = name,
                        isSelected = selectedPlatform == id,
                        onClick = {
                            selectedPlatform = if (selectedPlatform == id) null else id
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sort order
            FilterSectionTitle("Sort By")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(orderings) { (value, label) ->
                    FilterChipItem(
                        text = label,
                        isSelected = selectedOrdering == value,
                        onClick = {
                            selectedOrdering = if (selectedOrdering == value) null else value
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedGenre = null
                        selectedPlatform = null
                        selectedYear = ""
                        selectedOrdering = null
                        onClear()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderCyan)
                ) {
                    Text("CLEAR")
                }

                Button(
                    onClick = {
                        onApply(
                            SearchFilters(
                                genre = selectedGenre,
                                platform = selectedPlatform,
                                year = selectedYear.ifBlank { null },
                                ordering = selectedOrdering
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonPurple
                    )
                ) {
                    Text("APPLY", color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun FilterSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = TextSecondary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun FilterChipItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                color = if (isSelected) NeonPurple else DarkCard
            )
            .border(
                width = 1.dp,
                color = if (isSelected) NeonPurple else BorderCyan,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) TextPrimary else TextSecondary
        )
    }
}