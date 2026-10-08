package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.ButtonCombination
import com.example.model.GamepadAction
import com.example.model.GamepadButtons
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalRed

/**
 * Button Combinations Card (2 to 4 button chaining)
 * - Pure black background
 * - Thin amber border
 * - Monospace typography
 * - Evenly sized buttons
 */
@Composable
fun ButtonCombinationsCard(
    title: String = "Button Combinations",
    subtitle: String = "Chain 2 to 4 buttons together to execute actions like volume or brightness.",
    combinations: List<ButtonCombination>,
    availableActions: List<GamepadAction>,
    onAddCombination: () -> Unit,
    onDeleteCombination: (String) -> Unit,
    onUpdateCombination: (ButtonCombination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = Color.Black,
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TerminalAmber,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TerminalAmber.copy(alpha = 0.65f),
                        fontFamily = FontFamily.Monospace
                    )
                }

                Button(
                    onClick = onAddCombination,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TerminalGreen),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("button_add_combination")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, Modifier.size(13.dp), tint = TerminalGreen)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Add", color = TerminalGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (combinations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No combinations configured. Tap '+ Add' to create one.",
                        fontSize = 12.sp,
                        color = TerminalAmber.copy(alpha = 0.5f),
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    combinations.forEachIndexed { index, combo ->
                        CombinationRow(
                            comboIndex = index,
                            combo = combo,
                            availableActions = availableActions,
                            onDelete = { onDeleteCombination(combo.id) },
                            onUpdate = onUpdateCombination
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CombinationRow(
    comboIndex: Int,
    combo: ButtonCombination,
    availableActions: List<GamepadAction>,
    onDelete: () -> Unit,
    onUpdate: (ButtonCombination) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        color = Color.Black,
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header: Chain sequence indicator + Delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CHAIN #${comboIndex + 1} (${combo.buttons.size} BUTTONS)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TerminalAmber,
                    fontFamily = FontFamily.Monospace
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(26.dp)
                        .testTag("delete_combo_${combo.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TerminalRed, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Button selectors row (2 to 4 slots)
            Text(
                text = "Button Chain Sequence (Slot 1 to 4):",
                fontSize = 11.sp,
                color = TerminalAmber.copy(alpha = 0.7f),
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (slot in 0..3) {
                    val currentBtnCode = combo.buttons.getOrNull(slot) ?: "NONE"
                    val isOptional = slot >= 2

                    Box(modifier = Modifier.weight(1f)) {
                        ButtonPickerDropdown(
                            selectedCode = currentBtnCode,
                            slotIndex = slot,
                            isOptional = isOptional,
                            onSelect = { newCode ->
                                val currentList = combo.buttons.toMutableList()
                                if (newCode == "NONE") {
                                    if (slot < currentList.size) {
                                        currentList.removeAt(slot)
                                    }
                                } else {
                                    while (currentList.size <= slot) {
                                        currentList.add("KEYCODE_BUTTON_A")
                                    }
                                    currentList[slot] = newCode
                                }
                                val sanitized = currentList.filter { it != "NONE" }
                                if (sanitized.size >= 2) {
                                    onUpdate(combo.copy(buttons = sanitized))
                                } else {
                                    onUpdate(combo.copy(buttons = currentList))
                                }
                            }
                        )
                    }

                    if (slot < 3) {
                        Text(
                            "+",
                            color = TerminalAmber.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action selector row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Action To Execute:",
                    fontSize = 11.sp,
                    color = TerminalAmber.copy(alpha = 0.8f),
                    fontFamily = FontFamily.Monospace
                )

                ActionPickerDropdown(
                    currentActionCode = combo.actionCode,
                    availableActions = availableActions,
                    onSelect = { newActCode ->
                        onUpdate(combo.copy(actionCode = newActCode))
                    }
                )
            }
        }
    }
}

@Composable
fun ButtonPickerDropdown(
    selectedCode: String,
    slotIndex: Int,
    isOptional: Boolean,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayName = if (selectedCode == "NONE") {
        if (isOptional) "(None)" else "Btn ${slotIndex + 1}"
    } else {
        GamepadButtons.ALL_BUTTONS.firstOrNull { it.keyCodeName == selectedCode }?.displayName ?: selectedCode.replace("KEYCODE_BUTTON_", "")
    }

    val isSet = selectedCode != "NONE"

    Box {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .clickable { expanded = true }
                .border(1.dp, if (isSet) TerminalGreen else TerminalCardBorder.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .testTag("btn_picker_slot_$slotIndex"),
            color = Color.Black
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = displayName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSet) TerminalGreen else TerminalAmber.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color.Black)
                .border(1.dp, TerminalCardBorder)
                .height(280.dp)
        ) {
            if (isOptional) {
                DropdownMenuItem(
                    text = { Text("None (Empty Slot)", color = TerminalAmber.copy(alpha = 0.6f), fontFamily = FontFamily.Monospace) },
                    onClick = {
                        onSelect("NONE")
                        expanded = false
                    }
                )
            }

            GamepadButtons.ALL_BUTTONS.forEach { btn ->
                DropdownMenuItem(
                    text = {
                        Text(
                            btn.displayName,
                            color = if (btn.keyCodeName == selectedCode) TerminalGreen else TerminalAmber,
                            fontWeight = if (btn.keyCodeName == selectedCode) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    onClick = {
                        onSelect(btn.keyCodeName)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ActionPickerDropdown(
    currentActionCode: String,
    availableActions: List<GamepadAction>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentAction = GamepadAction.fromCode(currentActionCode)

    Box {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable { expanded = true }
                .border(1.dp, TerminalGreen, RoundedCornerShape(4.dp))
                .testTag("action_picker_trigger"),
            color = Color.Black
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = currentAction.title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TerminalGreen,
                    fontFamily = FontFamily.Monospace
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TerminalGreen, modifier = Modifier.size(16.dp))
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color.Black)
                .border(1.dp, TerminalCardBorder)
                .height(300.dp)
        ) {
            availableActions.forEach { act ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                act.title,
                                color = if (act.code == currentActionCode) TerminalGreen else TerminalAmber,
                                fontWeight = if (act.code == currentActionCode) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(act.category, fontSize = 10.sp, color = TerminalAmber.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace)
                        }
                    },
                    onClick = {
                        onSelect(act.code)
                        expanded = false
                    }
                )
            }
        }
    }
}
