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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.GamepadAction
import com.example.model.GamepadButtons
import com.example.ui.FlickPadViewModel
import com.example.ui.components.ButtonCombinationsCard
import com.example.ui.components.FlickBoardGridView
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalRed

@Composable
fun RadialLayoutScreen(
    viewModel: FlickPadViewModel,
    modifier: Modifier = Modifier
) {
    val parsedZones by viewModel.parsedZones.collectAsState()
    val parsedMappings by viewModel.parsedMappings.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()
    val allLayouts by viewModel.allLayouts.collectAsState()
    val activeLayoutId by viewModel.activeLayoutId.collectAsState()
    val activeModifierLayer by viewModel.activeModifierLayer.collectAsState()
    val buttonCombinations by viewModel.buttonCombinations.collectAsState()

    val theme = currentProfile?.overlayTheme ?: "CYBERPUNK"

    // Zoomed zone state
    var enlargedZoneIndex by remember { mutableStateOf<Int?>(null) }
    var selectedKeyIndex by remember { mutableStateOf<Int?>(null) }

    // Dialog state for editing a key
    var showKeyEditorDialog by remember { mutableStateOf(false) }
    var editingKeyText by remember { mutableStateOf("") }

    // Layout Dropdown & Dialogs
    var layoutDropdownExpanded by remember { mutableStateOf(false) }
    var showAddLayoutDialog by remember { mutableStateOf(false) }
    var newLayoutName by remember { mutableStateOf("") }

    var showRenameLayoutDialog by remember { mutableStateOf(false) }
    var renameLayoutName by remember { mutableStateOf("") }

    var showExportLayoutsDialog by remember { mutableStateOf(false) }
    var exportedLayoutsJson by remember { mutableStateOf("") }

    var showImportLayoutsDialog by remember { mutableStateOf(false) }
    var importLayoutsJsonText by remember { mutableStateOf("") }

    // Button remapping dialog state
    var buttonToRemap by remember { mutableStateOf<String?>(null) }
    var actionCategoryTab by remember { mutableIntStateOf(0) }

    val activeLayout = allLayouts.firstOrNull { it.id == activeLayoutId } ?: allLayouts.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "LAYOUT & BUTTON ACTIONS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TerminalAmber,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Custom keyboard layouts (K1–K16), modifier layers, and button mappings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TerminalAmber.copy(alpha = 0.65f),
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // =================================================================
        // LAYOUT DROPDOWN SELECTOR & ACTIONS
        // =================================================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color.Black,
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Keyboard Layout:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TerminalAmber,
                        fontFamily = FontFamily.Monospace
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                exportedLayoutsJson = viewModel.exportLayoutsJson()
                                showExportLayoutsDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.testTag("button_export_layouts")
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, Modifier.size(13.dp), tint = TerminalAmber)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Export", color = TerminalAmber, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = {
                                importLayoutsJsonText = ""
                                showImportLayoutsDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.testTag("button_import_layouts")
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, Modifier.size(13.dp), tint = TerminalAmber)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Import", color = TerminalAmber, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Layout Dropdown Button
                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { layoutDropdownExpanded = true }
                            .testTag("layout_dropdown_trigger"),
                        color = Color.Black,
                        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.Black, RoundedCornerShape(4.dp))
                                        .border(1.dp, TerminalGreen, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = activeLayout?.id ?: "K1",
                                        fontWeight = FontWeight.Bold,
                                        color = TerminalGreen,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = activeLayout?.name ?: "MessagEase Core",
                                    fontWeight = FontWeight.Bold,
                                    color = TerminalAmber,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TerminalAmber)
                        }
                    }

                    DropdownMenu(
                        expanded = layoutDropdownExpanded,
                        onDismissRequest = { layoutDropdownExpanded = false },
                        modifier = Modifier
                            .background(Color.Black)
                            .border(1.dp, TerminalCardBorder)
                            .height(340.dp)
                    ) {
                        allLayouts.forEach { layout ->
                            val isCurrent = layout.id == activeLayoutId
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "${layout.id}: ${layout.name}",
                                            color = if (isCurrent) TerminalGreen else TerminalAmber,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        if (isCurrent) {
                                            Text("(Active)", color = TerminalGreen, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.selectLayout(layout.id)
                                    layoutDropdownExpanded = false
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = TerminalGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add New Layout (K1–K16)", color = TerminalGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            },
                            onClick = {
                                layoutDropdownExpanded = false
                                newLayoutName = ""
                                showAddLayoutDialog = true
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null, tint = TerminalAmber, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Rename Current Layout", color = TerminalAmber, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                }
                            },
                            onClick = {
                                layoutDropdownExpanded = false
                                renameLayoutName = activeLayout?.name ?: ""
                                showRenameLayoutDialog = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // =================================================================
                // MODIFIER LAYER TOGGLE BUTTONS (Shift, Alt, Fn, Ctrl)
                // =================================================================
                Text(
                    text = "Modifier Layer (shows & edits modified symbols):",
                    fontSize = 11.5.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val modifiers = listOf("Shift", "Alt", "Fn", "Ctrl")
                    modifiers.forEach { mod ->
                        val isSelected = activeModifierLayer == mod
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setModifierLayer(mod) },
                            label = {
                                Text(
                                    text = mod,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace
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
                            modifier = Modifier.weight(1f).testTag("modifier_chip_$mod")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // =================================================================
        // 9-ZONE KEYBOARD VISUALIZATION & IN-PLACE ZOOM EDITOR
        // =================================================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color.Black,
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                FlickBoardGridView(
                    zones = parsedZones,
                    activeZoneIndex = enlargedZoneIndex ?: 0,
                    armedSectorIndex = null,
                    themeName = theme,
                    isEditorMode = true,
                    enlargedZoneIndex = enlargedZoneIndex,
                    selectedKeyIndex = selectedKeyIndex,
                    onZoneClick = { clickedZone ->
                        enlargedZoneIndex = clickedZone
                        selectedKeyIndex = null
                    },
                    onKeyClick = { zoneIdx, keyIdx ->
                        selectedKeyIndex = keyIdx
                        val zone = parsedZones.firstOrNull { it.index == zoneIdx }
                        editingKeyText = zone?.symbols?.getOrNull(keyIdx) ?: ""
                        showKeyEditorDialog = true
                    },
                    onBackFromEnlarged = {
                        enlargedZoneIndex = null
                        selectedKeyIndex = null
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // =================================================================
        // BUTTON ACTIONS MENU
        // =================================================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color.Black,
            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Button Actions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TerminalAmber,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Map any controller button to keyboard keys, layout switching, media, text tools, or screen controls.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TerminalAmber.copy(alpha = 0.65f),
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GamepadButtons.ALL_BUTTONS.forEach { btn ->
                        val actionCode = parsedMappings[btn.keyCodeName] ?: "NONE"
                        val action = GamepadAction.fromCode(actionCode)
                        val isAssigned = action != GamepadAction.NONE

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { buttonToRemap = btn.keyCodeName }
                                .testTag("button_action_row_${btn.keyCodeName}"),
                            color = Color.Black,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                TerminalCardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Clean button name only
                                Text(
                                    text = btn.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TerminalAmber,
                                    fontFamily = FontFamily.Monospace
                                )

                                // Action badge pill: only assigned action highlights in green
                                Box(
                                    modifier = Modifier
                                        .background(Color.Black, RoundedCornerShape(4.dp))
                                        .border(1.dp, if (isAssigned) TerminalGreen else TerminalCardBorder, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = action.title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAssigned) TerminalGreen else TerminalAmber.copy(alpha = 0.6f),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // =================================================================
        // BUTTON COMBINATIONS (2 to 4 buttons chained together)
        // =================================================================
        ButtonCombinationsCard(
            title = "Button Combinations",
            subtitle = "Chain 2 to 4 buttons together to execute actions like volume or brightness.",
            combinations = buttonCombinations,
            availableActions = GamepadAction.entries.toList(),
            onAddCombination = { viewModel.addCombination(isCursor = false) },
            onDeleteCombination = { viewModel.deleteCombination(it, isCursor = false) },
            onUpdateCombination = { viewModel.updateButtonCombination(it, isCursor = false) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    // =================================================================
    // DIALOGS
    // =================================================================

    // Add New Layout Dialog
    if (showAddLayoutDialog) {
        AlertDialog(
            onDismissRequest = { showAddLayoutDialog = false },
            title = { Text("Add New Layout") },
            text = {
                Column {
                    Text("Enter name for the new keyboard layout slot:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newLayoutName,
                        onValueChange = { newLayoutName = it },
                        singleLine = true,
                        label = { Text("Layout Name") },
                        modifier = Modifier.fillMaxWidth().testTag("input_new_layout_name")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addNewLayout(newLayoutName)
                        showAddLayoutDialog = false
                    },
                    modifier = Modifier.testTag("button_confirm_add_layout")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddLayoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename Layout Dialog
    if (showRenameLayoutDialog) {
        AlertDialog(
            onDismissRequest = { showRenameLayoutDialog = false },
            title = { Text("Rename Layout") },
            text = {
                Column {
                    Text("Enter a new name for layout $activeLayoutId:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = renameLayoutName,
                        onValueChange = { renameLayoutName = it },
                        singleLine = true,
                        label = { Text("New Name") },
                        modifier = Modifier.fillMaxWidth().testTag("input_rename_layout")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameLayoutName.isNotBlank()) {
                            viewModel.renameLayout(activeLayoutId, renameLayoutName)
                        }
                        showRenameLayoutDialog = false
                    },
                    modifier = Modifier.testTag("button_confirm_rename_layout")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameLayoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Export Layouts Dialog
    if (showExportLayoutsDialog) {
        AlertDialog(
            onDismissRequest = { showExportLayoutsDialog = false },
            title = { Text("Export Layouts JSON") },
            text = {
                Column {
                    Text("Cross-platform layout JSON:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportedLayoutsJson,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showExportLayoutsDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Import Layouts Dialog
    if (showImportLayoutsDialog) {
        AlertDialog(
            onDismissRequest = { showImportLayoutsDialog = false },
            title = { Text("Import Layouts JSON") },
            text = {
                Column {
                    Text("Paste layouts JSON to import:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importLayoutsJsonText,
                        onValueChange = { importLayoutsJsonText = it },
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        placeholder = { Text("Paste JSON here...") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.importLayoutsJson(importLayoutsJsonText)
                        showImportLayoutsDialog = false
                    },
                    modifier = Modifier.testTag("button_confirm_import_layouts")
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportLayoutsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Key Edit Dialog
    if (showKeyEditorDialog && enlargedZoneIndex != null && selectedKeyIndex != null) {
        val zIdx = enlargedZoneIndex!!
        val kIdx = selectedKeyIndex!!
        AlertDialog(
            onDismissRequest = { showKeyEditorDialog = false },
            containerColor = Color.Black,
            title = {
                Text(
                    "Edit Key (${com.example.data.DefaultProfiles.DIRECTION_LABELS.getOrNull(kIdx) ?: "Key $kIdx"})",
                    color = TerminalAmber,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Layer: $activeModifierLayer | Layout: $activeLayoutId",
                        fontSize = 12.sp,
                        color = TerminalGreen,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Enter a single character, digit, symbol, or macro word.",
                        fontSize = 12.sp,
                        color = TerminalAmber.copy(alpha = 0.7f),
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editingKeyText,
                        onValueChange = { editingKeyText = it },
                        singleLine = true,
                        label = { Text("Key Symbol", fontFamily = FontFamily.Monospace) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("key_edit_input_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateZoneSymbol(zIdx, kIdx, editingKeyText)
                        showKeyEditorDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalGreen),
                    modifier = Modifier.testTag("button_save_key")
                ) {
                    Text("Save", color = TerminalGreen, fontFamily = FontFamily.Monospace)
                }
            },
            dismissButton = {
                TextButton(onClick = { showKeyEditorDialog = false }) {
                    Text("Cancel", color = TerminalAmber, fontFamily = FontFamily.Monospace)
                }
            }
        )
    }

    // Button Remapping Dialog with Categorized Tabs
    if (buttonToRemap != null) {
        val btnCode = buttonToRemap!!
        val btnInfo = GamepadButtons.ALL_BUTTONS.firstOrNull { it.keyCodeName == btnCode }
        val categories = listOf("Keyboard", "Layout", "Text Tools", "Media Controls", "Screen Controls", "Navigation", "General")

        AlertDialog(
            onDismissRequest = { buttonToRemap = null },
            containerColor = Color.Black,
            title = {
                Text(
                    "Map ${btnInfo?.displayName ?: btnCode}",
                    color = TerminalAmber,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ScrollableTabRow(
                        selectedTabIndex = actionCategoryTab,
                        containerColor = Color.Black,
                        contentColor = TerminalAmber,
                        edgePadding = 0.dp,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[actionCategoryTab]),
                                color = TerminalGreen
                            )
                        }
                    ) {
                        categories.forEachIndexed { idx, cat ->
                            val isTabSel = actionCategoryTab == idx
                            Tab(
                                selected = isTabSel,
                                onClick = { actionCategoryTab = idx },
                                text = {
                                    Text(
                                        cat,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isTabSel) TerminalGreen else TerminalAmber.copy(alpha = 0.6f)
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val filteredActions = when (actionCategoryTab) {
                        0 -> GamepadAction.entries.filter { it.category == "Keyboard" || it.category == "Function Keys" }
                        1 -> GamepadAction.entries.filter { it.category == "Layout" }
                        2 -> GamepadAction.entries.filter { it.category == "Text Tools" }
                        3 -> GamepadAction.entries.filter { it.category == "Media Controls" }
                        4 -> GamepadAction.entries.filter { it.category == "Screen Controls" }
                        5 -> GamepadAction.entries.filter { it.category == "Navigation" }
                        else -> listOf(GamepadAction.NONE)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        filteredActions.forEach { act ->
                            val isCurrent = parsedMappings[btnCode] == act.code
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        viewModel.updateButtonMapping(btnCode, act.code)
                                        buttonToRemap = null
                                    }
                                    .testTag("action_pick_${act.code}"),
                                color = Color.Black,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isCurrent) TerminalGreen else TerminalCardBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = act.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCurrent) TerminalGreen else TerminalAmber,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (isCurrent) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = TerminalGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { buttonToRemap = null }) {
                    Text("Close", color = TerminalAmber, fontFamily = FontFamily.Monospace)
                }
            }
        )
    }
}
