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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    color = NeonPurple,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.errorMessage != null && uiState.gameDetail == null -> {
                ErrorSection(
                    message = uiState.errorMessage!!,
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
                    onTogglePlayed = viewModel::onTogglePlayedStatus,
                    onRatingChange = viewModel::onRatingChange,
                    onNotesChange = viewModel::onNotesChange,
                    onSaveNotes = viewModel::onSaveNotes,
                    onToggleNotesDialog = viewModel::onToggleNotesDialog
                )
            }
        }
    }

    // Notes Dialog
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
    onTogglePlayed: () -> Unit,
    onRatingChange: (Float) -> Unit,
    onNotesChange: (String) -> Unit,
    onSaveNotes: () -> Unit,
    onToggleNotesDialog: () -> Unit
) {
    val detail = uiState.gameDetail!!
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Hero Image
        HeroSection(
            detail = detail,
            onBackClick = onBackClick
        )

        // Title + Badges
        TitleSection(detail = detail)

        // Action Buttons
        ActionButtonsSection(
            isInCollection = uiState.isInCollection,
            isInWishlist = uiState.isInWishlist,
            onAddToCollection = onAddToCollection,
            onRemoveFromCollection = onRemoveFromCollection,
            onAddToWishlist = onAddToWishlist,
            onRemoveFromWishlist = onRemoveFromWishlist
        )

        // Info Grid
        InfoGridSection(detail = detail)

        // Private Notes
        PrivateNotesSection(
            notes = uiState.userNotes,
            userRating = uiState.userRating,
            onRatingChange = onRatingChange,
            onOpenNotes = onToggleNotesDialog
        )

        // About
        AboutSection(description = detail.descriptionRaw)

        // Screenshots
        if (uiState.screenshots.isNotEmpty()) {
            ScreenshotsSection(screenshots = uiState.screenshots)
        }

        // Trailers
        if (uiState.trailers.isNotEmpty()) {
            TrailersSection(
                trailersCount = uiState.trailers.size,
                previewUrl = uiState.trailers.firstOrNull()?.preview
            )
        }

        // User Sentiment
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

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            DarkNavy
                        )
                    )
                )
        )

        // Back button
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
                tint = TextPrimary
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
        // Genre badges
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            detail.genres?.take(2)?.forEach { genre ->
                Box(
                    modifier = Modifier
                        .background(
                            color = NeonPurple.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = NeonPurple.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = genre.name.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonPurple
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = detail.name,
            style = MaterialTheme.typography.displayMedium,
            color = TextPrimary,
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
        // Collection Button
        Button(
            onClick = if (isInCollection) onRemoveFromCollection else onAddToCollection,
            modifier = Modifier
                .weight(1f)
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
                        brush = if (isInCollection) {
                            Brush.linearGradient(listOf(StatusGreen, StatusGreen.copy(alpha = 0.7f)))
                        } else {
                            Brush.linearGradient(listOf(NeonPurple, Color(0xFF6A0DAD)))
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
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isInCollection) "IN COLLECTION" else "ADD TO COLLECTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Wishlist Button
        OutlinedButton(
            onClick = if (isInWishlist) onRemoveFromWishlist else onAddToWishlist,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (isInWishlist) StatusYellow else BorderCyan
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (isInWishlist) StatusYellow.copy(alpha = 0.1f) else Color.Transparent
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isInWishlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    tint = if (isInWishlist) StatusYellow else TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "WISHLIST",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isInWishlist) StatusYellow else TextSecondary,
                    fontWeight = FontWeight.Bold
                )
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
            .background(DarkCard)
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            InfoItem(
                label = "DEVELOPER",
                value = detail.developers?.firstOrNull()?.name ?: "N/A",
                modifier = Modifier.weight(1f)
            )
            InfoItem(
                label = "RELEASE",
                value = detail.released.formatReleaseDate(),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            InfoItem(
                label = "PLATFORM",
                value = detail.platforms
                    ?.take(3)
                    ?.joinToString(", ") { it.platform.name }
                    ?: "N/A",
                modifier = Modifier.weight(1f)
            )
            InfoItem(
                label = "PLAYTIME",
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
            color = TextMuted,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
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
                color = NeonPurple.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(DarkCard)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Private Notes",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            // Star Rating
            Row {
                repeat(5) { index ->
                    val starRating = (index + 1).toFloat()
                    Icon(
                        imageVector = if (userRating >= starRating) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = if (userRating >= starRating) StatusYellow else TextMuted,
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
                .background(DarkNavySecondary)
                .clickable(onClick = onOpenNotes)
                .padding(12.dp)
        ) {
            Text(
                text = if (notes.isBlank()) "Your private notes on this game..." else notes,
                style = MaterialTheme.typography.bodySmall,
                color = if (notes.isBlank()) TextMuted else TextSecondary,
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
                    .background(NeonPurple, RoundedCornerShape(2.dp))
            )
            Text(
                text = "About the Game",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
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
            text = "Screenshots",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
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
private fun TrailersSection(
    trailersCount: Int,
    previewUrl: String?
) {
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
                    .background(NeonCyan, RoundedCornerShape(2.dp))
            )
            Text(
                text = "Trailers",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkCard)
        ) {
            if (previewUrl != null) {
                AsyncImage(
                    model = previewUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    alpha = 0.6f
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = NeonCyan.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = NeonCyan,
                            shape = RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Trailer",
                        tint = NeonCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "WATCH OFFICIAL TRAILER",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$trailersCount Videos Available",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
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
            .background(DarkCard)
            .padding(16.dp)
    ) {
        Text(
            text = "USER SENTIMENT",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
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
                color = NeonCyan,
                fontWeight = FontWeight.ExtraBold
            )
            LinearProgressIndicator(
                progress = { detail.rating / 5f },
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = NeonCyan,
                trackColor = DarkNavySecondary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Rating breakdown
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
                    color = TextSecondary,
                    modifier = Modifier.width(100.dp)
                )
                LinearProgressIndicator(
                    progress = { rating.percent / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = NeonPurple,
                    trackColor = DarkNavySecondary
                )
                Text(
                    text = "${rating.percent.toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
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
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
        ) {
            Text("Retry", color = TextPrimary)
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
        containerColor = DarkCard,
        title = {
            Text(
                text = "Private Notes",
                color = TextPrimary,
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
                        text = "Write your notes here...",
                        color = TextMuted
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = BorderCyan,
                    focusedContainerColor = DarkNavySecondary,
                    unfocusedContainerColor = DarkNavySecondary,
                    cursorColor = NeonCyan
                ),
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
            ) {
                Text("Save", color = TextPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}