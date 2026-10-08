package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FlickPadViewModel
import com.example.ui.components.InteractiveDeadzoneDepiction
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalCardBorderBright
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalOrange
import com.example.ui.theme.TerminalRed
import com.example.ui.theme.TerminalSurfaceDark
import com.example.ui.theme.TerminalSurfaceElevated
import com.example.ui.theme.FlickCardBorder
import com.example.ui.theme.FlickCyan
import com.example.ui.theme.FlickGreen
import com.example.ui.theme.FlickRed
import com.example.ui.theme.FlickSurfaceElevated
import com.example.ui.theme.FlickViolet

val THEME_OPTIONS = listOf(
    "CYBERPUNK" to "Cyberpunk Cyan & Violet",
    "DEEP_OLED" to "Deep OLED Black & Blue",
    "MATRIX_GREEN" to "Emerald Matrix Green",
    "SOLAR_AMBER" to "Solar Amber & Orange",
    "FROST_BLUE" to "Frost Blue & Indigo"
)

@Composable
fun SettingsScreen(
    viewModel: FlickPadViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentProfile by viewModel.currentProfile.collectAsState()
    val allProfiles by viewModel.allProfiles.collectAsState()

    val connectedControllers by viewModel.gamepadManager.controllers.collectAsState()
    val selectedControllerId by viewModel.gamepadManager.selectedControllerId.collectAsState()

    val recordingTarget by viewModel.recordingShortcutTarget.collectAsState()
    val recordedKeys by viewModel.recordedKeys.collectAsState()

    val leftStickX by viewModel.leftStickX.collectAsState()
    val leftStickY by viewModel.leftStickY.collectAsState()
    val rightStickX by viewModel.rightStickX.collectAsState()
    val rightStickY by viewModel.rightStickY.collectAsState()

    var leftDeadzone by remember(currentProfile) { mutableFloatStateOf(currentProfile?.leftDeadzone ?: 0.22f) }
    var leftSens by remember(currentProfile) { mutableFloatStateOf(currentProfile?.leftSensitivity ?: 1.0f) }
    var rightDeadzone by remember(currentProfile) { mutableFloatStateOf(currentProfile?.rightDeadzone ?: 0.22f) }
    var rightSens by remember(currentProfile) { mutableFloatStateOf(currentProfile?.rightSensitivity ?: 1.0f) }

    // Setting 4: Overlay sliders & inputs
    var overlayScale by remember(currentProfile) { mutableFloatStateOf(currentProfile?.overlayScale ?: 1.0f) }
    var overlayOpacity by remember(currentProfile) { mutableFloatStateOf(currentProfile?.overlayOpacity ?: 0.90f) }
    var autoHideText by remember(currentProfile) {
        mutableStateOf((currentProfile?.overlayAutoHideSeconds ?: 0f).toString())
    }
    var overlayTheme by remember(currentProfile) { mutableStateOf(currentProfile?.overlayTheme ?: "CYBERPUNK") }
    var themeDropdownExpanded by remember { mutableStateOf(false) }

    // Setting 3: Profiles expandable section (collapsed by default)
    var profilesExpanded by remember { mutableStateOf(false) }

    // Dialogs
    var showCreateProfileDialog by remember { mutableStateOf(false) }
    var newProfileName by remember { mutableStateOf("") }
    var newProfileDesc by remember { mutableStateOf("") }

    var showExportDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }

    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "SETTINGS",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = FlickCyan
        )
        Text(
            text = "Device connections, shortcut recording, profiles management, and overlay configuration.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.65f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // =================================================================
        // SETTING 1: CONNECTION MENU (DEVICE SELECTION + COLORED STATUS DOT)
        // =================================================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = FlickSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, FlickCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val isAnyConnected = connectedControllers.isNotEmpty()
                val selectedController = connectedControllers.firstOrNull { it.id == selectedControllerId }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Simple colored dot showing whether it is currently connected successfully
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (isAnyConnected) FlickGreen else FlickRed)
                                .testTag("status_dot")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CONNECTED INPUT DEVICE",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    IconButton(onClick = { viewModel.gamepadManager.refreshControllers() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Rescan", tint = FlickCyan)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isAnyConnected) {
                    Text(
                        text = "Select which connected gamepad will control FlickPad:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        connectedControllers.forEach { controller ->
                            val isSelected = controller.id == selectedControllerId
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.gamepadManager.selectController(controller.id) }
                                    .testTag("device_item_${controller.id}"),
                                color = if (isSelected) FlickCyan.copy(alpha = 0.15f) else Color(0xFF090D17),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) FlickCyan else Color.White.copy(alpha = 0.08f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(FlickGreen)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = controller.name,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) FlickCyan else Color.White,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = "ID: ${controller.id} | Vendor: 0x${Integer.toHexString(controller.vendorId)}",
                                                fontSize = 10.sp,
                                                color = Color.White.copy(alpha = 0.5f)
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .background(FlickCyan.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("ACTIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = FlickCyan)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0C1322))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(FlickRed)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "No hardware gamepad connected. Virtual on-screen touch sticks and pass-through are active.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // =================================================================
        // SETTING 2: SHORTCUT / HOTKEY RECORDING MENU (1 to 6 KEYS)
        // =================================================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = FlickSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, FlickCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Keyboard, contentDescription = null, tint = FlickCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MODE TOGGLE SHORTCUTS (KEYBINDS)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = "Enter recording mode to record 1 to 6 keys on your controller to switch modes or toggle the overlay.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                val shortcutsList = listOf(
                    Triple("TEXT_INPUT", "Text Input Mode", currentProfile?.shortcutTextInput ?: "L3 + R3"),
                    Triple("OVERLAY", "On-Screen Overlay HUD", currentProfile?.shortcutOverlay ?: "BACK + START"),
                    Triple("CURSOR", "Cursor Control Mode", currentProfile?.shortcutCursor ?: "L1 + R1")
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    shortcutsList.forEach { (targetId, label, currentCombo) ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp)),
                            color = Color(0xFF090D17),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(label, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                    Text(
                                        text = "Keys: $currentCombo",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = FlickCyan
                                    )
                                }

                                Button(
                                    onClick = { viewModel.startRecordingShortcut(targetId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = FlickViolet.copy(alpha = 0.25f)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, FlickViolet),
                                    modifier = Modifier.testTag("btn_record_$targetId")
                                ) {
                                    Text("Record", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // =================================================================
        // SETTING 3: PROFILES SELECTION (EXPANDABLE MENU - COLLAPSED BY DEFAULT)
        // =================================================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = FlickSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, FlickCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { profilesExpanded = !profilesExpanded }
                        .testTag("expandable_profiles_header"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = FlickViolet)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "PROFILES MANAGEMENT (${allProfiles.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (profilesExpanded) "Tap to collapse" else "Tap to expand and manage profiles",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Icon(
                        if (profilesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = FlickViolet
                    )
                }

                // Collapsed by default, animated expand
                AnimatedVisibility(visible = profilesExpanded) {
                    Column {
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    newProfileName = ""
                                    newProfileDesc = ""
                                    showCreateProfileDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorderBright),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("button_create_profile")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, Modifier.size(13.dp), tint = TerminalAmber)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Save New", color = TerminalAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }

                            Button(
                                onClick = {
                                    exportedJsonText = viewModel.exportCurrentProfileJson()
                                    showExportDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("button_export_profile")
                            ) {
                                Icon(Icons.Default.FileUpload, contentDescription = null, Modifier.size(13.dp), tint = TerminalAmber)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export", color = TerminalAmber, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }

                            Button(
                                onClick = {
                                    importJsonText = ""
                                    showImportDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                                border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("button_import_profile")
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, Modifier.size(13.dp), tint = TerminalAmber)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Import", color = TerminalAmber, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            allProfiles.forEach { profile ->
                                val isActive = profile.id == currentProfile?.id
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.loadProfile(profile) }
                                        .testTag("profile_item_${profile.id}"),
                                    color = if (isActive) Color(0xFF0F2238) else Color(0xFF090D17),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isActive) FlickCyan else Color.White.copy(alpha = 0.08f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = profile.name,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isActive) FlickCyan else Color.White,
                                                    fontSize = 13.sp
                                                )
                                                if (profile.isDefault) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(12.dp))
                                                }
                                            }
                                            Text(profile.description, fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                                        }

                                        Row {
                                            IconButton(
                                                onClick = { viewModel.setAsDefaultProfile(profile) },
                                                enabled = !profile.isDefault
                                            ) {
                                                Icon(
                                                    Icons.Default.Star,
                                                    contentDescription = "Default",
                                                    tint = if (profile.isDefault) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.3f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            if (allProfiles.size > 1) {
                                                IconButton(onClick = { viewModel.deleteProfile(profile) }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = FlickRed.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
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

        Spacer(modifier = Modifier.height(16.dp))

        // =================================================================
        // SETTING 4: ON-SCREEN KEYBOARD OVERLAY SETTINGS
        // =================================================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color.Black,
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ON-SCREEN OVERLAY SETTINGS",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TerminalAmber,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Configure persistent on-screen HUD size, opacity, and auto-hide timeout.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TerminalAmber.copy(alpha = 0.65f),
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Simple Size Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Overlay Size / Scale", fontSize = 13.sp, color = TerminalAmber, fontFamily = FontFamily.Monospace)
                    Text("${(overlayScale * 100).toInt()}%", fontSize = 13.sp, color = TerminalAmber, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Slider(
                    value = overlayScale,
                    onValueChange = {
                        overlayScale = it
                        val autoHide = autoHideText.toFloatOrNull() ?: 0f
                        viewModel.updateOverlaySettings(overlayScale, overlayOpacity, autoHide, overlayTheme)
                    },
                    valueRange = 0.6f..1.6f,
                    colors = SliderDefaults.colors(thumbColor = TerminalAmber, activeTrackColor = TerminalAmber),
                    modifier = Modifier.testTag("slider_overlay_size")
                )

                // Simple Opacity Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Overlay Opacity", fontSize = 13.sp, color = TerminalAmber, fontFamily = FontFamily.Monospace)
                    Text("${(overlayOpacity * 100).toInt()}%", fontSize = 13.sp, color = TerminalAmber, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Slider(
                    value = overlayOpacity,
                    onValueChange = {
                        overlayOpacity = it
                        val autoHide = autoHideText.toFloatOrNull() ?: 0f
                        viewModel.updateOverlaySettings(overlayScale, overlayOpacity, autoHide, overlayTheme)
                    },
                    valueRange = 0.2f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = TerminalAmber, activeTrackColor = TerminalAmber),
                    modifier = Modifier.testTag("slider_overlay_opacity")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Text Input Box for Auto-Hide Seconds (0 = always on, 0.1 min, 999 max)
                Text(
                    text = "Auto-Hide Timeout (Seconds):",
                    fontSize = 13.sp,
                    color = TerminalAmber,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "0 = always on, 0.1 minimum, 999 maximum seconds",
                    fontSize = 11.sp,
                    color = TerminalAmber.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = autoHideText,
                    onValueChange = { newValue ->
                        autoHideText = newValue
                        val parsed = newValue.toFloatOrNull()
                        if (parsed != null) {
                            val clamped = if (parsed == 0f) 0f else parsed.coerceIn(0.1f, 999f)
                            viewModel.updateOverlaySettings(overlayScale, overlayOpacity, clamped, overlayTheme)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_auto_hide_seconds")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // =================================================================
        // SETTING 5: STICK SENSITIVITY & DEADZONES (MOVED FROM SENSITIVITY MENU)
        // =================================================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = TerminalSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorderBright)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "STICK DEADZONES & SENSITIVITY",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TerminalOrange,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Configure left and right analog stick deadzones (5% to 95%) and response multipliers.",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.65f),
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Two Interactive Depictions - ONLY Left Stick and Right Stick titles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InteractiveDeadzoneDepiction(
                        title = "Left Stick",
                        deadzone = leftDeadzone,
                        stickX = leftStickX,
                        stickY = leftStickY,
                        accentColor = TerminalOrange,
                        onTestDrag = { x, y -> viewModel.onLeftStickMove(x, y) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("depiction_left_stick")
                    )

                    InteractiveDeadzoneDepiction(
                        title = "Right Stick",
                        deadzone = rightDeadzone,
                        stickX = rightStickX,
                        stickY = rightStickY,
                        accentColor = TerminalGreen,
                        onTestDrag = { x, y -> viewModel.onRightStickMove(x, y) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("depiction_right_stick")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Deadzone Sliders: 5% to 95%
                Text(
                    text = "DEADZONE (5% – 95%)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TerminalOrange,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Left Stick Deadzone
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Left Stick Deadzone", fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f), fontFamily = FontFamily.Monospace)
                    Text("${(leftDeadzone * 100).toInt()}%", fontSize = 13.sp, color = TerminalOrange, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Slider(
                    value = leftDeadzone,
                    onValueChange = {
                        leftDeadzone = it
                        viewModel.updateDeadzoneAndSens(leftDeadzone, leftSens, rightDeadzone, rightSens)
                    },
                    valueRange = 0.05f..0.95f,
                    colors = SliderDefaults.colors(thumbColor = TerminalOrange, activeTrackColor = TerminalOrange),
                    modifier = Modifier.testTag("slider_left_deadzone")
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Right Stick Deadzone
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Right Stick Deadzone", fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f), fontFamily = FontFamily.Monospace)
                    Text("${(rightDeadzone * 100).toInt()}%", fontSize = 13.sp, color = TerminalGreen, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Slider(
                    value = rightDeadzone,
                    onValueChange = {
                        rightDeadzone = it
                        viewModel.updateDeadzoneAndSens(leftDeadzone, leftSens, rightDeadzone, rightSens)
                    },
                    valueRange = 0.05f..0.95f,
                    colors = SliderDefaults.colors(thumbColor = TerminalGreen, activeTrackColor = TerminalGreen),
                    modifier = Modifier.testTag("slider_right_deadzone")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Stick Sensitivity Multipliers
                Text(
                    text = "STICK SENSITIVITY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TerminalOrange,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Left Stick Sensitivity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Left Stick Sensitivity", fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f), fontFamily = FontFamily.Monospace)
                    Text(String.format("%.2fx", leftSens), fontSize = 13.sp, color = TerminalOrange, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Slider(
                    value = leftSens,
                    onValueChange = {
                        leftSens = it
                        viewModel.updateDeadzoneAndSens(leftDeadzone, leftSens, rightDeadzone, rightSens)
                    },
                    valueRange = 0.5f..2.5f,
                    colors = SliderDefaults.colors(thumbColor = TerminalOrange, activeTrackColor = TerminalOrange),
                    modifier = Modifier.testTag("slider_left_sensitivity")
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Right Stick Sensitivity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Right Stick Sensitivity", fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f), fontFamily = FontFamily.Monospace)
                    Text(String.format("%.2fx", rightSens), fontSize = 13.sp, color = TerminalGreen, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Slider(
                    value = rightSens,
                    onValueChange = {
                        rightSens = it
                        viewModel.updateDeadzoneAndSens(leftDeadzone, leftSens, rightDeadzone, rightSens)
                    },
                    valueRange = 0.5f..2.5f,
                    colors = SliderDefaults.colors(thumbColor = TerminalGreen, activeTrackColor = TerminalGreen),
                    modifier = Modifier.testTag("slider_right_sensitivity")
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Recording Shortcut Dialog
    if (recordingTarget != null) {
        val targetName = when (recordingTarget) {
            "TEXT_INPUT" -> "Text Input Mode"
            "OVERLAY" -> "On-Screen Overlay HUD"
            "CURSOR" -> "Cursor Control Mode"
            else -> "Shortcut"
        }

        AlertDialog(
            onDismissRequest = { viewModel.cancelRecordingShortcut() },
            title = {
                Text("Recording Shortcut: $targetName")
            },
            text = {
                Column {
                    Text(
                        "Press 1 to 6 keys/buttons on your controller (or tap below).",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF090D17))
                            .border(1.dp, FlickCyan, RoundedCornerShape(8.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (recordedKeys.isEmpty()) {
                            Text(
                                "Waiting for buttons...",
                                color = Color.White.copy(alpha = 0.4f),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        } else {
                            Text(
                                text = recordedKeys.joinToString(" + "),
                                color = FlickCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Quick Test Buttons:", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("L3", "R3", "L1", "R1", "START", "SELECT").forEach { sampleKey ->
                            Button(
                                onClick = { viewModel.recordKey(sampleKey) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF131D2E)),
                                modifier = Modifier.weight(1f),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp)
                            ) {
                                Text(sampleKey, fontSize = 9.sp, color = Color.White)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.saveRecordedShortcut() },
                    enabled = recordedKeys.isNotEmpty(),
                    modifier = Modifier.testTag("btn_save_recorded_shortcut")
                ) {
                    Text("Save Shortcut")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelRecordingShortcut() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Create Profile Dialog
    if (showCreateProfileDialog) {
        AlertDialog(
            onDismissRequest = { showCreateProfileDialog = false },
            title = { Text("Create New Profile") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { newProfileName = it },
                        label = { Text("Profile Name") },
                        placeholder = { Text("e.g. Apex Legends Chat") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newProfileDesc,
                        onValueChange = { newProfileDesc = it },
                        label = { Text("Description") },
                        placeholder = { Text("e.g. Squad tactical callouts") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProfileName.isNotBlank()) {
                            viewModel.createProfile(newProfileName.trim(), newProfileDesc.trim())
                            showCreateProfileDialog = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Export Profile Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Profile JSON") },
            text = {
                Column {
                    Text(
                        "Shareable JSON format for Android, Windows and Linux:",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF090D14))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = exportedJsonText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = FlickCyan
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clip = ClipData.newPlainText("FlickPad Profile JSON", exportedJsonText)
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(clip)
                        showExportDialog = false
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Import Profile Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Profile JSON") },
            text = {
                Column {
                    Text(
                        "Paste a valid FlickPad configuration JSON below:",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = { Text("{\n  \"format\": \"flickpad_profile\",\n  ...\n}") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonText.isNotBlank()) {
                            val success = viewModel.importProfileFromJson(importJsonText.trim())
                            if (success) {
                                showImportDialog = false
                            }
                        }
                    }
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
