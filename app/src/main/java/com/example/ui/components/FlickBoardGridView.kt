package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ZoneData
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalGreen

data class ZoneGridCell(
    val row: Int,
    val col: Int,
    val zoneIndex: Int
)

val NINE_ZONE_GRID = listOf(
    // Row 0: Up-Left, Up, Up-Right
    ZoneGridCell(0, 0, 8),
    ZoneGridCell(0, 1, 1),
    ZoneGridCell(0, 2, 2),
    // Row 1: Left, Neutral, Right
    ZoneGridCell(1, 0, 7),
    ZoneGridCell(1, 1, 0),
    ZoneGridCell(1, 2, 3),
    // Row 2: Down-Left, Down, Down-Right
    ZoneGridCell(2, 0, 6),
    ZoneGridCell(2, 1, 5),
    ZoneGridCell(2, 2, 4)
)

/**
 * 9-Zone FlickBoard Grid View:
 * - All squares of symbols are equal size and evenly spaced
 * - Pure black background with thin amber outline
 * - Selected or active turns green
 * - Clean monospace typography
 */
@Composable
fun FlickBoardGridView(
    zones: List<ZoneData>,
    activeZoneIndex: Int,
    armedSectorIndex: Int?,
    themeName: String,
    modifier: Modifier = Modifier,
    isEditorMode: Boolean = false,
    enlargedZoneIndex: Int? = null,
    selectedKeyIndex: Int? = null,
    onZoneClick: ((Int) -> Unit)? = null,
    onKeyClick: ((zoneIndex: Int, keyIndex: Int) -> Unit)? = null,
    onBackFromEnlarged: (() -> Unit)? = null
) {
    AnimatedContent(
        targetState = enlargedZoneIndex,
        transitionSpec = {
            androidx.compose.animation.fadeIn(animationSpec = tween(150)) togetherWith
            androidx.compose.animation.fadeOut(animationSpec = tween(150))
        },
        label = "zone_zoom_transition"
    ) { targetEnlarged ->
        if (targetEnlarged == null) {
            // Standard 3x3 Grid of 9 Zones
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (row in 0..2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (col in 0..2) {
                            val cell = NINE_ZONE_GRID.first { it.row == row && it.col == col }
                            val zone = zones.firstOrNull { it.index == cell.zoneIndex }
                            val isArmedZone = (!isEditorMode && activeZoneIndex == cell.zoneIndex)

                            MiniZoneCard(
                                zone = zone,
                                zoneIndex = cell.zoneIndex,
                                isArmed = isArmedZone,
                                armedSectorIndex = if (isArmedZone) armedSectorIndex else null,
                                isEditorMode = isEditorMode,
                                onClick = { onZoneClick?.invoke(cell.zoneIndex) },
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.0f)
                                    .testTag("zone_card_${cell.zoneIndex}")
                            )
                        }
                    }
                }
            }
        } else {
            // Enlarged view of the selected zone
            val zone = zones.firstOrNull { it.index == targetEnlarged }
            EnlargedZoneView(
                zone = zone,
                zoneIndex = targetEnlarged,
                selectedKeyIndex = selectedKeyIndex,
                onKeyClick = { kIdx -> onKeyClick?.invoke(targetEnlarged, kIdx) },
                onBack = { onBackFromEnlarged?.invoke() },
                modifier = modifier
                    .fillMaxWidth()
                    .background(Color.Black)
            )
        }
    }
}

@Composable
fun MiniZoneCard(
    zone: ZoneData?,
    zoneIndex: Int,
    isArmed: Boolean,
    armedSectorIndex: Int?,
    isEditorMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            isArmed -> TerminalGreen
            isEditorMode -> TerminalCardBorder
            else -> TerminalCardBorder
        },
        label = "mini_zone_border"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        color = Color.Black,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(if (isArmed) 1.5.dp else 1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            // 3x3 layout of 8 keys inside the zone
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Top row (Up-Left=7, Up=0, Up-Right=1)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MiniKeySquare(symbol = zone?.symbols?.getOrNull(7) ?: "", isArmed = armedSectorIndex == 7)
                    MiniKeySquare(symbol = zone?.symbols?.getOrNull(0) ?: "", isArmed = armedSectorIndex == 0)
                    MiniKeySquare(symbol = zone?.symbols?.getOrNull(1) ?: "", isArmed = armedSectorIndex == 1)
                }
                // Middle row (Left=6, Center Dot, Right=2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MiniKeySquare(symbol = zone?.symbols?.getOrNull(6) ?: "", isArmed = armedSectorIndex == 6)
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isArmed) TerminalGreen else TerminalAmber.copy(alpha = 0.4f))
                    )
                    MiniKeySquare(symbol = zone?.symbols?.getOrNull(2) ?: "", isArmed = armedSectorIndex == 2)
                }
                // Bottom row (Down-Left=5, Down=4, Down-Right=3)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MiniKeySquare(symbol = zone?.symbols?.getOrNull(5) ?: "", isArmed = armedSectorIndex == 5)
                    MiniKeySquare(symbol = zone?.symbols?.getOrNull(4) ?: "", isArmed = armedSectorIndex == 4)
                    MiniKeySquare(symbol = zone?.symbols?.getOrNull(3) ?: "", isArmed = armedSectorIndex == 3)
                }
            }
        }
    }
}

@Composable
fun MiniKeySquare(
    symbol: String,
    isArmed: Boolean
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(if (isArmed) TerminalGreen.copy(alpha = 0.25f) else Color.Transparent)
            .border(0.5.dp, if (isArmed) TerminalGreen else TerminalCardBorder.copy(alpha = 0.35f), RoundedCornerShape(3.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = if (isArmed) TerminalGreen else TerminalAmber,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
    }
}

@Composable
fun EnlargedZoneView(
    zone: ZoneData?,
    zoneIndex: Int,
    selectedKeyIndex: Int?,
    onKeyClick: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp)),
        color = Color.Black,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, TerminalGreen)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("button_back_zone")) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TerminalAmber
                    )
                }
                Text(
                    text = "EDIT ZONE $zoneIndex (TAP KEY TO EDIT)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TerminalGreen,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(36.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3x3 Enlarged Key Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Row 1: Up-Left (7), Up (0), Up-Right (1)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EnlargedKeyButton(keyIndex = 7, symbol = zone?.symbols?.getOrNull(7) ?: "", isSelected = selectedKeyIndex == 7, onClick = { onKeyClick(7) }, modifier = Modifier.weight(1f))
                    EnlargedKeyButton(keyIndex = 0, symbol = zone?.symbols?.getOrNull(0) ?: "", isSelected = selectedKeyIndex == 0, onClick = { onKeyClick(0) }, modifier = Modifier.weight(1f))
                    EnlargedKeyButton(keyIndex = 1, symbol = zone?.symbols?.getOrNull(1) ?: "", isSelected = selectedKeyIndex == 1, onClick = { onKeyClick(1) }, modifier = Modifier.weight(1f))
                }

                // Row 2: Left (6), Center Guide, Right (2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EnlargedKeyButton(keyIndex = 6, symbol = zone?.symbols?.getOrNull(6) ?: "", isSelected = selectedKeyIndex == 6, onClick = { onKeyClick(6) }, modifier = Modifier.weight(1f))

                    // Center Neutral Indicator
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.2f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black)
                            .border(1.dp, TerminalCardBorder.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("•", color = TerminalAmber, fontSize = 24.sp, fontFamily = FontFamily.Monospace)
                    }

                    EnlargedKeyButton(keyIndex = 2, symbol = zone?.symbols?.getOrNull(2) ?: "", isSelected = selectedKeyIndex == 2, onClick = { onKeyClick(2) }, modifier = Modifier.weight(1f))
                }

                // Row 3: Down-Left (5), Down (4), Down-Right (3)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EnlargedKeyButton(keyIndex = 5, symbol = zone?.symbols?.getOrNull(5) ?: "", isSelected = selectedKeyIndex == 5, onClick = { onKeyClick(5) }, modifier = Modifier.weight(1f))
                    EnlargedKeyButton(keyIndex = 4, symbol = zone?.symbols?.getOrNull(4) ?: "", isSelected = selectedKeyIndex == 4, onClick = { onKeyClick(4) }, modifier = Modifier.weight(1f))
                    EnlargedKeyButton(keyIndex = 3, symbol = zone?.symbols?.getOrNull(3) ?: "", isSelected = selectedKeyIndex == 3, onClick = { onKeyClick(3) }, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun EnlargedKeyButton(
    keyIndex: Int,
    symbol: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .aspectRatio(1.2f)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .testTag("enlarged_key_$keyIndex"),
        color = Color.Black,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) TerminalGreen else TerminalCardBorder)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (symbol.isEmpty()) "—" else symbol,
                color = if (isSelected) TerminalGreen else TerminalAmber,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
