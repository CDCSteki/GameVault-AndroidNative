package com.example.gamevault.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.gamevault.R
import com.example.gamevault.data.local.entity.PlayStatus
import com.example.gamevault.data.remote.dto.GameDetailDto
import com.example.gamevault.data.remote.dto.GameScreenshotDto
import com.example.gamevault.data.repository.GameRepository
import com.example.gamevault.ui.util.formatReleaseDate
import com.example.gamevault.ui.util.toPlaytimeString
import com.example.gamevault.ui.theme.*

@Composable
fun GameDetailScreen(
    gameId: Int,
    gameRepository: GameRepository,
    onBackClick: () -> Unit
) {
    val viewModel: GameDetailViewModel = viewModel(
        factory = GameDetailViewModel.Factory(gameRepository, gameId)
    )
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage = uiState.snackbarMessageRes?.let { stringResource(it) }

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            snackbarHostState.showSnackbar(snackbarMessage)
            viewModel.onSnackbarDismissed()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = GVTheme.colors.card,
                    contentColor = GVTheme.colors.textPrimary,
                    actionColor = GVTheme.colors.accent
                )
            }
        },
        containerColor = GVTheme.colors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(GVTheme.colors.background)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        color = GVTheme.colors.accent,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.errorMessageRes != null && uiState.gameDetail == null -> {
                    ErrorSection(
                        message = stringResource(uiState.errorMessageRes!!),
                        onRetry = viewModel::retry,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.gameDetail != null -> {
                    GameDetailContent(
                        uiState = uiState,
                        onBackClick = onBackClick,
                        onAddToCollection = viewModel::onAddToCollection,
                        onRemoveFromCollection = viewModel::onRemoveFromCollection,
                        onAddToWishlist = viewModel::onAddToWishlist,
                        onRemoveFromWishlist = viewModel::onRemoveFromWishlist,
                        onPlayStatusChange = viewModel::onPlayStatusChange,
                        onRatingChange = viewModel::onRatingChange,
                        onToggleNotesDialog = viewModel::onToggleNotesDialog
                    )
                }
            }
        }
    }

    if (uiState.showNotesDialog) {
        NotesDialog(
            notes = uiState.userNotes,
            onNotesChange = viewModel::onNotesChange,
            onSave = viewModel::onSaveNotes,
            onDismiss = viewModel::onToggleNotesDialog
        )
    }
}

@Composable
private fun GameDetailContent(
    uiState: GameDetailUiState,
    onBackClick: () -> Unit,
    onAddToCollection: () -> Unit,
    onRemoveFromCollection: () -> Unit,
    onAddToWishlist: () -> Unit,
    onRemoveFromWishlist: () -> Unit,
    onPlayStatusChange: (PlayStatus) -> Unit,
    onRatingChange: (Float) -> Unit,
    onToggleNotesDialog: () -> Unit
) {
    val detail = uiState.gameDetail!!
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        HeroSection(detail = detail, onBackClick = onBackClick)
        TitleSection(detail = detail)
        ActionButtonsSection(
            isInCollection = uiState.isInCollection,
            isInWishlist = uiState.isInWishlist,
            onAddToCollection = onAddToCollection,
            onRemoveFromCollection = onRemoveFromCollection,
            onAddToWishlist = onAddToWishlist,
            onRemoveFromWishlist = onRemoveFromWishlist
        )
        if (uiState.isInCollection) {
            PlayStatusSection(
                currentStatus = uiState.playStatus,
                onStatusChange = onPlayStatusChange
            )
        }
        InfoGridSection(detail = detail)

        SystemRequirementsSection(detail = detail)

        PrivateNotesSection(
            notes = uiState.userNotes,
            userRating = uiState.userRating,
            onRatingChange = onRatingChange,
            onOpenNotes = onToggleNotesDialog
        )
        AboutSection(description = detail.descriptionRaw)
        if (uiState.screenshots.isNotEmpty()) {
            ScreenshotsSection(screenshots = uiState.screenshots)
        }
        UserSentimentSection(detail = detail)
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun HeroSection(
    detail: GameDetailDto,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        AsyncImage(
            model = detail.backgroundImage,
            contentDescription = detail.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            GVTheme.colors.background
                        )
                    )
                )
        )

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = GVTheme.colors.textPrimary
            )
        }
    }
}

@Composable
private fun TitleSection(detail: GameDetailDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .offset(y = (-16).dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            detail.genres?.take(2)?.forEach { genre ->
                Box(
                    modifier = Modifier
                        .background(
                            color = GVTheme.colors.accent.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = GVTheme.colors.accent.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = genre.name.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = GVTheme.colors.accent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = detail.name,
            style = MaterialTheme.typography.displayMedium,
            color = GVTheme.colors.textPrimary,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun ActionButtonsSection(
    isInCollection: Boolean,
    isInWishlist: Boolean,
    onAddToCollection: () -> Unit,
    onRemoveFromCollection: () -> Unit,
    onAddToWishlist: () -> Unit,
    onRemoveFromWishlist: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = if (isInCollection) onRemoveFromCollection else onAddToCollection,
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = if (isInCollection) {
                            Brush.linearGradient(listOf(StatusGreen, StatusGreen.copy(alpha = 0.7f)))
                        } else {
                            Brush.linearGradient(listOf(GVTheme.colors.accent, GVTheme.colors.accent.copy(alpha = 0.7f)))
                        },
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isInCollection) Icons.Default.CheckCircle else Icons.Default.Add,
                        contentDescription = null,
                        tint = GVTheme.colors.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isInCollection) stringResource(R.string.detail_in_collection) else stringResource(R.string.detail_add_collection),
                        style = MaterialTheme.typography.labelSmall,
                        color = GVTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        OutlinedButton(
            onClick = when {
                isInCollection -> { {} }
                isInWishlist -> onRemoveFromWishlist
                else -> onAddToWishlist
            },
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = when {
                    isInCollection -> GVTheme.colors.textMuted.copy(alpha = 0.3f)
                    isInWishlist -> StatusYellow
                    else -> GVTheme.colors.border
                }
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = when {
                    isInCollection -> Color.Transparent
                    isInWishlist -> StatusYellow.copy(alpha = 0.1f)
                    else -> Color.Transparent
                }
            ),
            enabled = !isInCollection
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = when {
                        isInCollection -> Icons.Default.Block
                        isInWishlist -> Icons.Default.Bookmark
                        else -> Icons.Default.BookmarkBorder
                    },
                    contentDescription = null,
                    tint = when {
                        isInCollection -> GVTheme.colors.textMuted.copy(alpha = 0.3f)
                        isInWishlist -> StatusYellow
                        else -> GVTheme.colors.textSecondary
                    },
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (isInCollection) stringResource(R.string.detail_in_collection) else stringResource(R.string.detail_wishlist),
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        isInCollection -> GVTheme.colors.textMuted.copy(alpha = 0.3f)
                        isInWishlist -> StatusYellow
                        else -> GVTheme.colors.textSecondary
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PlayStatusSection(
    currentStatus: PlayStatus,
    onStatusChange: (PlayStatus) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(
            text = stringResource(R.string.detail_play_status),
            style = MaterialTheme.typography.labelSmall,
            color = GVTheme.colors.textMuted,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlayStatus.entries.forEach { status ->
                val isSelected = currentStatus == status
                val statusColor = when (status) {
                    PlayStatus.NOT_PLAYED -> GVTheme.colors.textMuted
                    PlayStatus.PLAYING -> GVTheme.colors.accentSecondary
                    PlayStatus.PLAYED -> StatusGreen
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) statusColor.copy(alpha = 0.15f)
                            else GVTheme.colors.card
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) statusColor else GVTheme.colors.border,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onStatusChange(status) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when (status) {
                                PlayStatus.NOT_PLAYED -> Icons.Default.RadioButtonUnchecked
                                PlayStatus.PLAYING -> Icons.Default.PlayCircle
                                PlayStatus.PLAYED -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = if (isSelected) statusColor else GVTheme.colors.textMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = when (status) {
                                PlayStatus.NOT_PLAYED -> stringResource(R.string.status_not_played)
                                PlayStatus.PLAYING -> stringResource(R.string.status_playing)
                                PlayStatus.PLAYED -> stringResource(R.string.status_played)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) statusColor else GVTheme.colors.textMuted,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun InfoGridSection(detail: GameDetailDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GVTheme.colors.card)
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            InfoItem(
                label = stringResource(R.string.detail_developer),
                value = detail.developers?.firstOrNull()?.name ?: stringResource(R.string.general_na),
                modifier = Modifier.weight(1f)
            )
            InfoItem(
                label = stringResource(R.string.detail_release),
                value = detail.released.formatReleaseDate(),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            InfoItem(
                label = stringResource(R.string.detail_platform),
                value = detail.platforms
                    ?.take(3)
                    ?.joinToString(", ") { it.platform.name }
                    ?: stringResource(R.string.general_na),
                modifier = Modifier.weight(1f)
            )
            InfoItem(
                label = stringResource(R.string.detail_playtime),
                value = detail.playtime.toPlaytimeString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = GVTheme.colors.textMuted,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = GVTheme.colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PrivateNotesSection(
    notes: String,
    userRating: Float,
    onRatingChange: (Float) -> Unit,
    onOpenNotes: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = GVTheme.colors.accent.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(GVTheme.colors.card)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.detail_private_notes),
                style = MaterialTheme.typography.titleMedium,
                color = GVTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Row {
                repeat(5) { index ->
                    val starRating = (index + 1).toFloat()
                    Icon(
                        imageVector = if (userRating >= starRating) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = if (userRating >= starRating) StatusYellow else GVTheme.colors.textMuted,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onRatingChange(starRating) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(GVTheme.colors.backgroundSecondary)
                .clickable(onClick = onOpenNotes)
                .padding(12.dp)
        ) {
            Text(
                text = notes.ifBlank { stringResource(R.string.detail_notes_placeholder) },
                style = MaterialTheme.typography.bodySmall,
                color = if (notes.isBlank()) GVTheme.colors.textMuted else GVTheme.colors.textSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AboutSection(description: String?) {
    if (description.isNullOrBlank()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(20.dp)
                    .background(GVTheme.colors.accent, RoundedCornerShape(2.dp))
            )
            Text(
                text = stringResource(R.string.detail_about),
                style = MaterialTheme.typography.titleMedium,
                color = GVTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = GVTheme.colors.textSecondary,
            maxLines = 6,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ScreenshotsSection(screenshots: List<GameScreenshotDto>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.detail_screenshots),
            style = MaterialTheme.typography.titleMedium,
            color = GVTheme.colors.textPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(screenshots.take(8)) { screenshot ->
                AsyncImage(
                    model = screenshot.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(200.dp)
                        .height(120.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }
        }
    }
}

@Composable
private fun SystemRequirementsSection(detail: GameDetailDto) {
    val pcPlatform = detail.platforms?.find { it.platform.name.contains("PC", ignoreCase = true) }
    val requirements = pcPlatform?.requirements ?: pcPlatform?.requirementsEn

    if (requirements?.minimum.isNullOrBlank() && requirements?.recommended.isNullOrBlank()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GVTheme.colors.card)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(20.dp)
                    .background(GVTheme.colors.accentSecondary, RoundedCornerShape(2.dp))
            )
            Text(
                text = stringResource(R.string.pc_system_requirements),
                style = MaterialTheme.typography.titleMedium,
                color = GVTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        requirements.minimum?.let { minReq ->
            Text(
                text = minReq.replace("\n", "\n• "),
                style = MaterialTheme.typography.bodySmall,
                color = GVTheme.colors.textSecondary,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        requirements.recommended?.let { recReq ->
            Text(
                text = recReq.replace("\n", "\n• "),
                style = MaterialTheme.typography.bodySmall,
                color = GVTheme.colors.textSecondary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun UserSentimentSection(detail: GameDetailDto) {
    val sentimentPercent = (detail.rating / 5f * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GVTheme.colors.card)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.detail_user_sentiment),
            style = MaterialTheme.typography.labelSmall,
            color = GVTheme.colors.textMuted,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "$sentimentPercent%",
                style = MaterialTheme.typography.headlineLarge,
                color = GVTheme.colors.accentSecondary,
                fontWeight = FontWeight.ExtraBold
            )
            LinearProgressIndicator(
                progress = { detail.rating / 5f },
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = GVTheme.colors.accentSecondary,
                trackColor = GVTheme.colors.backgroundSecondary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        detail.ratings?.take(3)?.forEach { rating ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = rating.title.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    color = GVTheme.colors.textSecondary,
                    modifier = Modifier.width(100.dp)
                )
                LinearProgressIndicator(
                    progress = { rating.percent / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = GVTheme.colors.accent,
                    trackColor = GVTheme.colors.backgroundSecondary
                )
                Text(
                    text = "${rating.percent.toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = GVTheme.colors.textMuted,
                    modifier = Modifier.width(40.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun ErrorSection(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = StatusRed,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = GVTheme.colors.textSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = GVTheme.colors.accent)
        ) {
            Text(stringResource(R.string.detail_retry), color = GVTheme.colors.textPrimary)
        }
    }
}

@Composable
private fun NotesDialog(
    notes: String,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GVTheme.colors.card,
        title = {
            Text(
                text = stringResource(R.string.detail_private_notes),
                color = GVTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                placeholder = {
                    Text(
                        text = stringResource(R.string.detail_notes_write),
                        color = GVTheme.colors.textMuted
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = GVTheme.colors.textPrimary,
                    unfocusedTextColor = GVTheme.colors.textPrimary,
                    focusedBorderColor = GVTheme.colors.accentSecondary,
                    unfocusedBorderColor = GVTheme.colors.border,
                    focusedContainerColor = GVTheme.colors.backgroundSecondary,
                    unfocusedContainerColor = GVTheme.colors.backgroundSecondary,
                    cursorColor = GVTheme.colors.accentSecondary
                ),
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = GVTheme.colors.accent)
            ) {
                Text(stringResource(R.string.detail_notes_save), color = GVTheme.colors.textPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.detail_notes_cancel), color = GVTheme.colors.textSecondary)
            }
        }
    )
}