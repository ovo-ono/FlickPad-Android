package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FlickPadViewModel
import com.example.ui.components.LiveOutputCard
import com.example.ui.components.RadialOverlayView
import com.example.ui.components.VirtualStickPad
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalGreen

/**
 * Preview Screen
 * - Pure black background
 * - All components separated with thin amber outlines
 * - Selecting or enabling a component turns it green
 * - Monospace fonts throughout
 */
@Composable
fun PlayTestScreen(
    viewModel: FlickPadViewModel,
    modifier: Modifier = Modifier
) {
    val activeMode by viewModel.activeMode.collectAsState()
    val typedText by viewModel.typedText.collectAsState()
    val activeZoneIndex by viewModel.activeZoneIndex.collectAsState()
    val armedSectorIndex by viewModel.armedSectorIndex.collectAsState()
    val lastFlickedChar by viewModel.lastFlickedChar.collectAsState()
    val parsedZones by viewModel.parsedZones.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()
    val pressedButtons by viewModel.pressedButtons.collectAsState()
    val overlayVisible by viewModel.overlayVisible.collectAsState()

    val theme = currentProfile?.overlayTheme ?: "SOLAR_AMBER"
    val scale = currentProfile?.overlayScale ?: 1.0f
    val opacity = currentProfile?.overlayOpacity ?: 0.90f

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode Selector Pills (Turns green when enabled/selected)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isOff = activeMode == "OFF"
            FilterChip(
                selected = isOff,
                onClick = { viewModel.setMode("OFF") },
                label = { Text("[OFF]", fontFamily = FontFamily.Monospace, fontWeight = if (isOff) FontWeight.Bold else FontWeight.Normal) },
                leadingIcon = { Icon(Icons.Default.PowerSettingsNew, contentDescription = null, Modifier.width(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.Black,
                    selectedLabelColor = TerminalGreen,
                    selectedLeadingIconColor = TerminalGreen,
                    containerColor = Color.Black,
                    labelColor = TerminalAmber.copy(alpha = 0.6f),
                    iconColor = TerminalAmber.copy(alpha = 0.6f)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isOff,
                    borderColor = TerminalCardBorder,
                    selectedBorderColor = TerminalGreen
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            val isTextInput = activeMode == "TEXT_INPUT"
            FilterChip(
                selected = isTextInput,
                onClick = { viewModel.setMode("TEXT_INPUT") },
                label = { Text("[TEXT_IN]", fontFamily = FontFamily.Monospace, fontWeight = if (isTextInput) FontWeight.Bold else FontWeight.Normal) },
                leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null, Modifier.width(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.Black,
                    selectedLabelColor = TerminalGreen,
                    selectedLeadingIconColor = TerminalGreen,
                    containerColor = Color.Black,
                    labelColor = TerminalAmber.copy(alpha = 0.6f),
                    iconColor = TerminalAmber.copy(alpha = 0.6f)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isTextInput,
                    borderColor = TerminalCardBorder,
                    selectedBorderColor = TerminalGreen
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            val isCursor = activeMode == "CURSOR_CONTROL"
            FilterChip(
                selected = isCursor,
                onClick = { viewModel.setMode("CURSOR_CONTROL") },
                label = { Text("[CURSOR]", fontFamily = FontFamily.Monospace, fontWeight = if (isCursor) FontWeight.Bold else FontWeight.Normal) },
                leadingIcon = { Icon(Icons.Default.Mouse, contentDescription = null, Modifier.width(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.Black,
                    selectedLabelColor = TerminalGreen,
                    selectedLeadingIconColor = TerminalGreen,
                    containerColor = Color.Black,
                    labelColor = TerminalAmber.copy(alpha = 0.6f),
                    iconColor = TerminalAmber.copy(alpha = 0.6f)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isCursor,
                    borderColor = TerminalCardBorder,
                    selectedBorderColor = TerminalGreen
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Outlined test input field with X button to clear all
        LiveOutputCard(
            text = typedText,
            onClear = { viewModel.clearText() }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Radial Overlay Keyboard HUD
        AnimatedVisibility(visible = overlayVisible) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
                    .border(1.dp, TerminalCardBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "> RADIAL_KEYBOARD_HUD",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TerminalAmber,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "ZONE: $activeZoneIndex",
                            style = MaterialTheme.typography.labelSmall,
                            color = TerminalAmber.copy(alpha = 0.7f),
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    RadialOverlayView(
                        activeZoneIndex = activeZoneIndex,
                        armedSectorIndex = armedSectorIndex,
                        lastFlickedChar = lastFlickedChar,
                        zones = parsedZones,
                        themeName = theme,
                        scale = scale * 0.95f,
                        opacity = opacity
                    )

                    if (pressedButtons.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "BTN_INPUT: ${pressedButtons.joinToString(", ") { it.replace("KEYCODE_BUTTON_", "") }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TerminalGreen,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        if (!overlayVisible && activeMode == "TEXT_INPUT") {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                color = Color.Black,
                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "STATUS: OVERLAY_SLEEP",
                            fontWeight = FontWeight.Bold,
                            color = TerminalAmber,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            "Touch stick or tap to revive HUD",
                            fontSize = 11.sp,
                            color = TerminalAmber.copy(alpha = 0.6f),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    androidx.compose.material3.OutlinedButton(
                        onClick = { viewModel.toggleOverlay() },
                        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalGreen),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Black,
                            contentColor = TerminalGreen
                        )
                    ) {
                        Text(
                            "WAKE HUD",
                            color = TerminalGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Virtual On-Screen Controller Pad
        VirtualStickPad(
            onLeftStickMove = { x, y -> viewModel.onLeftStickMove(x, y) },
            onRightStickMove = { x, y -> viewModel.onRightStickMove(x, y) },
            onActionClick = { action -> viewModel.executeAction(action) }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}
