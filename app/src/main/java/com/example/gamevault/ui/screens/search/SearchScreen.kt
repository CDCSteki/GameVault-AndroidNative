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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.gamevault.R
import com.example.gamevault.data.remote.dto.GameDto
import com.example.gamevault.data.repository.GameRepository
import com.example.gamevault.data.repository.SearchRepository
import com.example.gamevault.ui.components.GameVaultTopBar
import com.example.gamevault.ui.theme.*
import androidx.compose.ui.platform.LocalLocale

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
            .background(GVTheme.colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            GameVaultTopBar()

            SearchField(
                query = uiState.query,
                onQueryChange = viewModel::onQueryChange,
                onSearchSubmit = {
                    focusManager.clearFocus()
                    viewModel.onSearchSubmit()
                }
            )

            FilterButton(
                hasActiveFilters = uiState.filters != SearchFilters(),
                onClick = viewModel::onToggleFilterSheet
            )

            if (uiState.errorMessageRes != null) {
                Text(
                    text = stringResource(uiState.errorMessageRes!!),
                    color = StatusRed,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            val isDefaultState = uiState.query.isEmpty() && uiState.filters == SearchFilters()
            val displayList = if (isDefaultState) uiState.defaultGames else uiState.searchResults
            val listTitle = if (isDefaultState) stringResource(R.string.search_trending_suggestions) else stringResource(R.string.search_results)

            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {

                    if (isDefaultState && uiState.searchHistory.isNotEmpty()) {
                        item {
                            SearchHistorySection(
                                history = uiState.searchHistory,
                                onHistoryClick = { query ->
                                    viewModel.onHistoryItemClick(query)
                                },
                                onDeleteItem = viewModel::onDeleteHistoryItem,
                                onClearAll = viewModel::onClearHistory
                            )
                        }
                    }

                    if (!isDefaultState && displayList.isEmpty() && !uiState.isLoading) {
                        item {
                            EmptySearchState()
                        }
                    } else if (displayList.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text(
                                    text = listTitle,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = GVTheme.colors.textPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${displayList.size} ${stringResource(R.string.search_titles_found)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GVTheme.colors.accent,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        items(displayList) { game ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                SearchResultCard(
                                    game = game,
                                    onClick = { onGameClick(game.id) }
                                )
                            }
                        }
                    }
                }

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(GVTheme.colors.background.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GVTheme.colors.accent)
                    }
                }
            }
        }

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
                text = stringResource(R.string.search_placeholder),
                color = GVTheme.colors.textMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = GVTheme.colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = GVTheme.colors.textMuted,
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
            focusedTextColor = GVTheme.colors.textPrimary,
            unfocusedTextColor = GVTheme.colors.textPrimary,
            focusedBorderColor = GVTheme.colors.accentSecondary,
            unfocusedBorderColor = GVTheme.colors.border,
            focusedContainerColor = GVTheme.colors.card,
            unfocusedContainerColor = GVTheme.colors.card,
            cursorColor = GVTheme.colors.accentSecondary
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
                        colors = listOf(GVTheme.colors.accent, GVTheme.colors.accentSecondary)
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
                    tint = GVTheme.colors.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (hasActiveFilters) stringResource(R.string.search_filter_active) else stringResource(R.string.search_filter),
                    style = MaterialTheme.typography.labelLarge,
                    color = GVTheme.colors.textPrimary,
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
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.search_recent),
                style = MaterialTheme.typography.titleMedium,
                color = GVTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onClearAll) {
                Text(
                    text = stringResource(R.string.search_clear_all),
                    style = MaterialTheme.typography.labelSmall,
                    color = GVTheme.colors.accent
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
                        tint = GVTheme.colors.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = item.query,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GVTheme.colors.textSecondary
                    )
                }
                IconButton(
                    onClick = { onDeleteItem(item.id) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = GVTheme.colors.textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            HorizontalDivider(color = GVTheme.colors.border.copy(alpha = 0.3f))
        }
    }
}

@Composable
private fun EmptySearchState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = GVTheme.colors.textMuted,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.search_no_results),
                style = MaterialTheme.typography.titleMedium,
                color = GVTheme.colors.textMuted
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
                color = GVTheme.colors.border,
                shape = RoundedCornerShape(12.dp)
            )
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
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        game.genres?.firstOrNull()?.let { genre ->
                            BadgeChip(text = genre.name.uppercase())
                        }
                        if (!game.platforms.isNullOrEmpty()) {
                            val platformCount = game.platforms.size
                            val platformText = if (platformCount == 1) {
                                game.platforms.first().platform.name
                            } else {
                                "${game.platforms.first().platform.name} +${platformCount - 1}"
                            }

                            BadgeChip(
                                text = platformText,
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
                            text = String.format(LocalLocale.current.platformLocale, stringResource(R.string.general_rating_format), game.rating),
                            style = MaterialTheme.typography.labelMedium,
                            color = GVTheme.colors.textSecondary
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
                                tint = GVTheme.colors.textMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = String.format(LocalLocale.current.platformLocale, stringResource(R.string.general_playtime_format), playtime),
                                style = MaterialTheme.typography.labelSmall,
                                color = GVTheme.colors.textMuted
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
    val orderings = listOf("-rating" to stringResource(R.string.search_best_rated), "-released" to stringResource(R.string.search_newest), "-added" to stringResource(R.string.search_most_popular))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = GVTheme.colors.backgroundSecondary,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        color = GVTheme.colors.border,
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
                text = stringResource(R.string.filter_title),
                style = MaterialTheme.typography.headlineSmall,
                color = GVTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            FilterSectionTitle(stringResource(R.string.filter_genre))
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

            FilterSectionTitle(stringResource(R.string.filter_platform))
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

            FilterSectionTitle(stringResource(R.string.filter_sort_by))
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
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GVTheme.colors.textSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GVTheme.colors.border)
                ) {
                    Text(stringResource(R.string.filter_clear))
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
                        containerColor = GVTheme.colors.accent
                    )
                ) {
                    Text(stringResource(R.string.filter_apply), color = GVTheme.colors.textPrimary)
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
        color = GVTheme.colors.textSecondary,
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
                color = if (isSelected) GVTheme.colors.accent else GVTheme.colors.card
            )
            .border(
                width = 1.dp,
                color = if (isSelected) GVTheme.colors.accent else GVTheme.colors.border,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) GVTheme.colors.textPrimary else GVTheme.colors.textSecondary
        )
    }
}