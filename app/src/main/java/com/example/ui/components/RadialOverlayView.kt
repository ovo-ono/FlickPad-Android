package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.example.data.ZoneData

@Composable
fun RadialOverlayView(
    activeZoneIndex: Int,
    armedSectorIndex: Int?,
    lastFlickedChar: String?,
    zones: List<ZoneData>,
    themeName: String,
    scale: Float = 1.0f,
    opacity: Float = 0.9f,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale.coerceIn(0.6f, 1.5f))
            .alpha(opacity.coerceIn(0.2f, 1.0f))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        FlickBoardGridView(
            zones = zones,
            activeZoneIndex = activeZoneIndex,
            armedSectorIndex = armedSectorIndex,
            themeName = themeName,
            isEditorMode = false
        )
    }
}
