package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.example.model.RepeatMode
import com.example.model.Song

@Composable
fun PlayerFullScreen(
    isVisible: Boolean,
    song: Song?,
    isPlaying: Boolean,
    playbackPositionMs: Long,
    isShuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    onCollapse: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onOpenQueue: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = isVisible) {
        onCollapse()
    }

    AnimatedVisibility(
        visible = isVisible && song != null,
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(350, easing = FastOutSlowInEasing)),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300, easing = FastOutSlowInEasing)),
        modifier = modifier
    ) {
        if (song == null) return@AnimatedVisibility

        val durationMs = (song.durationSeconds * 1000L).coerceAtLeast(1000L)
        var isUserScrubbing by remember { mutableStateOf(false) }
        var scrubPosition by remember { mutableFloatStateOf(0f) }

        val currentSliderValue = if (isUserScrubbing) {
            scrubPosition
        } else {
            (playbackPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        }

        val elapsedMs = if (isUserScrubbing) {
            (scrubPosition * durationMs).toLong()
        } else {
            playbackPositionMs
        }
        val remainingMs = (durationMs - elapsedMs).coerceAtLeast(0L)

        // Subtle cover scale animation while playing
        val infiniteTransition = rememberInfiniteTransition(label = "player_breathing")
        val coverScale by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = if (isPlaying) 1.025f else 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, easing = FastOutSlowInEasing),
                repeatMode = AnimRepeatMode.Reverse
            ),
            label = "cover_scale"
        )

        // Background with subtle radial ambient glow matching song colors
        val dominantColor = if (song.gradientColors.isNotEmpty()) {
            Color(song.gradientColors.first()).copy(alpha = 0.22f)
        } else {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("player_full_screen"),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                dominantColor,
                                MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    val maxW = maxWidth
                    val maxH = maxHeight

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp)
                            .widthIn(max = 540.dp)
                            .align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Navigation Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = onCollapse,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("close_full_player_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Minimizar reproductor",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "REPRODUCIENDO",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = song.album,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = onOpenQueue,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("open_queue_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QueueMusic,
                                    contentDescription = "Cola de reproducción",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        // Centered Large Album Artwork
                        val coverDimension = min(maxW * 0.76f, min(maxH * 0.40f, 320.dp))
                        Box(
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .scale(if (isPlaying) coverScale else 1.0f),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(coverDimension)
                                    .shadow(elevation = 20.dp, shape = RoundedCornerShape(28.dp)),
                                shape = RoundedCornerShape(28.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                SongCoverArt(
                                    drawableRes = song.drawableRes,
                                    gradientColors = song.gradientColors,
                                    size = coverDimension,
                                    cornerSize = 28.dp,
                                    contentDescription = "Portada de ${song.title}"
                                )
                            }
                        }

                        // Track Title, Artist, and Favorite button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = onToggleFavorite,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("full_player_favorite_btn")
                            ) {
                                Icon(
                                    imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = if (song.isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                                    tint = if (song.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Progress Slider & Timestamps
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Slider(
                                value = currentSliderValue,
                                onValueChange = {
                                    isUserScrubbing = true
                                    scrubPosition = it
                                },
                                onValueChangeFinished = {
                                    onSeek((scrubPosition * durationMs).toLong())
                                    isUserScrubbing = false
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("playback_progress_slider")
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatDurationMs(elapsedMs),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "-${formatDurationMs(remainingMs)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Primary & Secondary Playback Controls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Shuffle
                            IconButton(
                                onClick = onToggleShuffle,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("full_player_shuffle_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = "Reproducción aleatoria",
                                    tint = if (isShuffleEnabled) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    },
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            // Previous
                            IconButton(
                                onClick = onPreviousClick,
                                modifier = Modifier
                                    .size(52.dp)
                                    .testTag("full_player_prev_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Canción anterior",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(34.dp)
                                )
                            }

                            // Play / Pause FAB
                            FloatingActionButton(
                                onClick = onPlayPauseClick,
                                modifier = Modifier
                                    .size(72.dp)
                                    .testTag("full_player_play_pause_btn"),
                                shape = CircleShape,
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                elevation = FloatingActionButtonDefaults.elevation(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            // Next
                            IconButton(
                                onClick = onNextClick,
                                modifier = Modifier
                                    .size(52.dp)
                                    .testTag("full_player_next_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Siguiente canción",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(34.dp)
                                )
                            }

                            // Repeat Mode
                            IconButton(
                                onClick = onCycleRepeat,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("full_player_repeat_btn")
                            ) {
                                Icon(
                                    imageVector = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                    contentDescription = when (repeatMode) {
                                        RepeatMode.OFF -> "Repetición desactivada"
                                        RepeatMode.ALL -> "Repetir todo"
                                        RepeatMode.ONE -> "Repetir una"
                                    },
                                    tint = if (repeatMode != RepeatMode.OFF) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    },
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        // Bottom Quality Badge
                        Surface(
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Alta fidelidad • 320 kbps FLAC",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDurationMs(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
