package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SongCoverArt(
    drawableRes: Int?,
    gradientColors: List<Long>,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    cornerSize: Dp = 12.dp,
    isCircular: Boolean = false,
    contentDescription: String = "Portada del álbum"
) {
    val shape = if (isCircular) CircleShape else RoundedCornerShape(cornerSize)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape),
        contentAlignment = Alignment.Center
    ) {
        if (drawableRes != null) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val colors = if (gradientColors.size >= 2) {
                listOf(Color(gradientColors[0]), Color(gradientColors[1]))
            } else {
                listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4))
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(colors = colors)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (size > 100.dp) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(size * 0.45f)
                )
            }
        }
    }
}
