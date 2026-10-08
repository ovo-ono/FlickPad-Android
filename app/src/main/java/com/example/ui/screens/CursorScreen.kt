package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DefaultProfiles
import com.example.engine.CursorAccelerationCurve
import com.example.model.GamepadAction
import com.example.model.GamepadButtons
import com.example.ui.FlickPadViewModel
import com.example.ui.components.ButtonCombinationsCard
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalGreen

val RADIAL_OPTIONS = listOf(
    "Scrolling Up",
    "Scrolling Down",
    "Zooming In",
    "Zooming Out",
    "Scrolling Left",
    "Scrolling Right",
    "None"
)

@Composable
fun CursorScreen(
    viewModel: FlickPadViewModel,
    modifier: Modifier = Modifier
) {
    val currentProfile by viewModel.currentProfile.collectAsState()
    val cursorCombinations by viewModel.cursorCombinations.collectAsState()
    val parsedCursorMappings by viewModel.parsedCursorMappings.collectAsState()
    val leftRadialActions by viewModel.leftStickRadialActions.collectAsState()
    val rightRadialActions by viewModel.rightStickRadialActions.collectAsState()

    var mouseSens by remember(currentProfile) { mutableFloatStateOf(currentProfile?.mouseSensitivity ?: 1.5f) }
    var accelerationCurve by remember(currentProfile) { mutableStateOf(currentProfile?.mouseAccelerationCurve ?: "EXPONENTIAL") }
    var leftMoveCursor by remember(currentProfile) { mutableStateOf(currentProfile?.leftStickMoveCursor ?: true) }
    var rightMoveCursor by remember(currentProfile) { mutableStateOf(currentProfile?.rightStickMoveCursor ?: false) }

    var buttonToRemapInCursorMode by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "CURSOR & MOUSE CONTROL",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TerminalAmber,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "Configure analog mouse speed, 8-directional radial stick menus, and cursor button bindings.",
            style = MaterialTheme.typography.bodySmall,
            color = TerminalAmber.copy(alpha = 0.65f),
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mouse Sensitivity & Acceleration Curve Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color.Black,
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = TerminalAmber)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MOUSE SENSITIVITY & ACCELERATION",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TerminalAmber,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cursor Speed", fontSize = 13.sp, color = TerminalAmber, fontFamily = FontFamily.Monospace)
                    Text(String.format("%.2fx", mouseSens), fontSize = 13.sp, color = TerminalAmber, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Slider(
                    value = mouseSens,
                    onValueChange = {
                        mouseSens = it
                        viewModel.updateCursorSpeedAndCurve(mouseSens, accelerationCurve)
                    },
                    valueRange = 0.2f..4.0f,
                    colors = SliderDefaults.colors(thumbColor = TerminalAmber, activeTrackColor = TerminalAmber),
                    modifier = Modifier.testTag("slider_mouse_sensitivity")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Acceleration Response Curve:",
                    fontSize = 13.sp,
                    color = TerminalAmber,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CursorAccelerationCurve.entries.forEach { curve ->
                        val isSelected = accelerationCurve == curve.name
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                accelerationCurve = curve.name
                                viewModel.updateCursorSpeedAndCurve(mouseSens, accelerationCurve)
                            },
                            label = {
                                Text(
                                    curve.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.Black,
                                selectedLabelColor = TerminalGreen,
                                containerColor = Color.Black,
                                labelColor = TerminalAmber.copy(alpha = 0.6f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = TerminalCardBorder,
                                selectedBorderColor = TerminalGreen
                            ),
                            modifier = Modifier.weight(1f).testTag("chip_curve_${curve.name}")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dual Analog Sticks Assignment (Move Cursor vs 8-Directional Radial Menu)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color.Black,
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TouchApp, contentDescription = null, tint = TerminalAmber)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ANALOG STICK ASSIGNMENT",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TerminalAmber,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Left Stick Configuration
                StickRadialConfigSection(
                    stickTitle = "Left Analog Stick",
                    isMoveCursorEnabled = leftMoveCursor,
                    onToggleMoveCursor = {
                        leftMoveCursor = it
                        viewModel.setStickMoveCursor(isLeft = true, moveCursor = it)
                    },
                    actions = leftRadialActions,
                    onSelectAction = { dirIdx, act ->
                        viewModel.updateStickRadialAction(isLeft = true, directionIndex = dirIdx, action = act)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Right Stick Configuration
                StickRadialConfigSection(
                    stickTitle = "Right Analog Stick",
                    isMoveCursorEnabled = rightMoveCursor,
                    onToggleMoveCursor = {
                        rightMoveCursor = it
                        viewModel.setStickMoveCursor(isLeft = false, moveCursor = it)
                    },
                    actions = rightRadialActions,
                    onSelectAction = { dirIdx, act ->
                        viewModel.updateStickRadialAction(isLeft = false, directionIndex = dirIdx, action = act)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cursor Mode Face Buttons Remapping Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color.Black,
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CURSOR MODE BUTTON ACTIONS",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TerminalAmber,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Buttons active when Cursor Control mode is selected (Left/Right clicks, scrolling, drag-lock).",
                    style = MaterialTheme.typography.bodySmall,
                    color = TerminalAmber.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GamepadButtons.ALL_BUTTONS.forEach { btn ->
                        val actionCode = parsedCursorMappings[btn.keyCodeName] ?: "MOUSE_LEFT_CLICK"
                        val action = GamepadAction.fromCode(actionCode)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { buttonToRemapInCursorMode = btn.keyCodeName }
                                .testTag("cursor_button_row_${btn.keyCodeName}"),
                            color = Color.Black,
                            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = btn.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TerminalAmber,
                                    fontFamily = FontFamily.Monospace
                                )

                                Box(
                                    modifier = Modifier
                                        .background(Color.Black, RoundedCornerShape(4.dp))
                                        .border(1.dp, TerminalGreen, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = action.title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TerminalGreen,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CURSOR MODE BUTTON COMBINATIONS
        ButtonCombinationsCard(
            title = "Cursor Button Combinations",
            subtitle = "Chain 2 to 4 buttons together to execute actions active only in cursor mode.",
            combinations = cursorCombinations,
            availableActions = GamepadAction.entries.toList(),
            onAddCombination = { viewModel.addCombination(isCursor = true) },
            onDeleteCombination = { viewModel.deleteCombination(it, isCursor = true) },
            onUpdateCombination = { viewModel.updateButtonCombination(it, isCursor = true) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Remap Cursor Button Action Dialog
    if (buttonToRemapInCursorMode != null) {
        val btnCode = buttonToRemapInCursorMode!!
        val btnInfo = GamepadButtons.ALL_BUTTONS.firstOrNull { it.keyCodeName == btnCode }
        val cursorActions = listOf(
            GamepadAction.MOUSE_LEFT_CLICK,
            GamepadAction.MOUSE_RIGHT_CLICK,
            GamepadAction.MOUSE_MIDDLE_CLICK,
            GamepadAction.MOUSE_DRAG_LOCK,
            GamepadAction.SCROLL_UP_ACTION,
            GamepadAction.SCROLL_DOWN_ACTION,
            GamepadAction.ZOOM_IN_ACTION,
            GamepadAction.ZOOM_OUT_ACTION,
            GamepadAction.KEY_ESC,
            GamepadAction.NONE
        )

        AlertDialog(
            onDismissRequest = { buttonToRemapInCursorMode = null },
            containerColor = Color.Black,
            title = {
                Text(
                    "Cursor Action: ${btnInfo?.displayName ?: btnCode}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TerminalAmber
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    cursorActions.forEach { act ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, TerminalCardBorder, RoundedCornerShape(6.dp))
                                .clickable {
                                    viewModel.updateCursorButtonMapping(btnCode, act.code)
                                    buttonToRemapInCursorMode = null
                                }
                                .testTag("cursor_act_${act.code}"),
                            color = Color.Black
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(act.title, fontWeight = FontWeight.Bold, color = TerminalAmber, fontFamily = FontFamily.Monospace)
                                Text(act.category, fontSize = 11.sp, color = TerminalGreen, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { buttonToRemapInCursorMode = null }) {
                    Text("Close", color = TerminalAmber, fontFamily = FontFamily.Monospace)
                }
            }
        )
    }
}

@Composable
fun StickRadialConfigSection(
    stickTitle: String,
    isMoveCursorEnabled: Boolean,
    onToggleMoveCursor: (Boolean) -> Unit,
    actions: List<String>,
    onSelectAction: (dirIndex: Int, action: String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        color = Color.Black,
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header with "Move Cursor" switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stickTitle,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isMoveCursorEnabled) TerminalGreen else TerminalAmber,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (isMoveCursorEnabled) "Controls pointer motion directly" else "8-directional radial menu enabled",
                        style = MaterialTheme.typography.labelSmall,
                        color = TerminalAmber.copy(alpha = 0.6f),
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Move Cursor",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMoveCursorEnabled) TerminalGreen else TerminalAmber.copy(alpha = 0.5f),
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = isMoveCursorEnabled,
                        onCheckedChange = onToggleMoveCursor,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TerminalGreen,
                            checkedTrackColor = Color.Black,
                            checkedBorderColor = TerminalGreen,
                            uncheckedThumbColor = TerminalAmber.copy(alpha = 0.5f),
                            uncheckedTrackColor = Color.Black,
                            uncheckedBorderColor = TerminalCardBorder
                        ),
                        modifier = Modifier.testTag("switch_move_cursor_${stickTitle.take(4)}")
                    )
                }
            }

            // 8-Directional Radial Menu (enabled only when Move Cursor is disabled)
            if (!isMoveCursorEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "8-DIRECTION RADIAL MAPPINGS (TAP TO CHANGE):",
                    style = MaterialTheme.typography.labelSmall,
                    color = TerminalAmber.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DefaultProfiles.DIRECTION_LABELS.forEachIndexed { dirIdx, dirLabel ->
                        val currentAction = actions.getOrNull(dirIdx) ?: "None"
                        var menuExpanded by remember { mutableStateOf(false) }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, TerminalCardBorder, RoundedCornerShape(4.dp))
                                .clickable { menuExpanded = true }
                                .testTag("radial_dir_${stickTitle.take(4)}_$dirIdx"),
                            color = Color.Black
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = dirLabel,
                                    fontSize = 12.sp,
                                    color = TerminalAmber,
                                    fontFamily = FontFamily.Monospace
                                )

                                Box {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = currentAction,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (currentAction != "None") TerminalGreen else TerminalAmber.copy(alpha = 0.6f),
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = TerminalAmber,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false },
                                        modifier = Modifier
                                            .background(Color.Black)
                                            .border(1.dp, TerminalCardBorder)
                                    ) {
                                        RADIAL_OPTIONS.forEach { opt ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        opt,
                                                        color = if (opt == currentAction) TerminalGreen else TerminalAmber,
                                                        fontWeight = if (opt == currentAction) FontWeight.Bold else FontWeight.Normal,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                },
                                                onClick = {
                                                    onSelectAction(dirIdx, opt)
                                                    menuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
