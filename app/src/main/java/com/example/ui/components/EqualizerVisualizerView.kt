package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun EqualizerVisualizerView(
    bands: List<Float>,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barWidth: Dp = 4.dp,
    maxHeight: Dp = 28.dp,
    gradientColors: List<Color> = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary
    )
) {
    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        bands.forEach { bandValue ->
            val targetHeight = if (isPlaying) bandValue.coerceIn(0.12f, 1.0f) else 0.12f
            val animatedHeightFraction by animateFloatAsState(
                targetValue = targetHeight,
                animationSpec = tween(durationMillis = 65),
                label = "bar_height"
            )

            Box(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight(animatedHeightFraction)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(
                        Brush.verticalGradient(
                            colors = if (isPlaying) gradientColors else listOf(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                            )
                        )
                    )
            )
        }
    }
}
