package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GamepadAction
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalGreen
import kotlin.math.sqrt

/**
 * Controller pad component:
 * - Removed Virtual Controller pad text in its entirety
 * - Thumbsticks and face buttons evenly sized and spaced
 * - Small R1, R2 and L1, L2 triggers/bumpers above them
 * - Select and Start buttons added
 * - Pure black background with thin amber outline
 * - Monospace fonts throughout
 */
@Composable
fun VirtualStickPad(
    onLeftStickMove: (Float, Float) -> Unit,
    onRightStickMove: (Float, Float) -> Unit,
    onActionClick: (GamepadAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = Color.Black,
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Row: Shoulder Buttons (L2, L1 | Select, Start | R1, R2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Shoulders
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShoulderButton(label = "L2", onClick = { onActionClick(GamepadAction.TAB) }, tag = "button_l2")
                    ShoulderButton(label = "L1", onClick = { onActionClick(GamepadAction.KEY_ESC) }, tag = "button_l1")
                }

                // Center System Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SystemButton(label = "SELECT", onClick = { onActionClick(GamepadAction.CYCLE_MODE) }, tag = "button_select")
                    SystemButton(label = "START", onClick = { onActionClick(GamepadAction.TOGGLE_OVERLAY) }, tag = "button_start")
                }

                // Right Shoulders
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShoulderButton(label = "R1", onClick = { onActionClick(GamepadAction.CYCLE_LAYOUTS) }, tag = "button_r1")
                    ShoulderButton(label = "R2", onClick = { onActionClick(GamepadAction.ENTER) }, tag = "button_r2")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Row: Left Stick | Face Buttons | Right Stick
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Thumbstick
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "L-STICK",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerminalAmber
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    AnalogThumbstick(
                        label = "L",
                        accentColor = TerminalAmber,
                        onPositionChanged = onLeftStickMove,
                        modifier = Modifier.testTag("virtual_left_stick")
                    )
                }

                // Center Action Buttons (Y top, X left, B right, A bottom)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Top: Y (Enter)
                    ActionButton(
                        label = "▲ Y",
                        subLabel = "Enter",
                        onClick = { onActionClick(GamepadAction.ENTER) },
                        tag = "button_y"
                    )

                    // Middle Row: X (Shift) and B (Back)
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        ActionButton(
                            label = "■ X",
                            subLabel = "Shift",
                            onClick = { onActionClick(GamepadAction.SHIFT_TOGGLE) },
                            tag = "button_x"
                        )
                        ActionButton(
                            label = "● B",
                            subLabel = "Back",
                            onClick = { onActionClick(GamepadAction.BACKSPACE) },
                            tag = "button_b"
                        )
                    }

                    // Bottom: A (Space)
                    ActionButton(
                        label = "✖ A",
                        subLabel = "Space",
                        onClick = { onActionClick(GamepadAction.SPACE) },
                        tag = "button_a"
                    )
                }

                // Right Thumbstick
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "R-STICK",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerminalAmber
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    AnalogThumbstick(
                        label = "R",
                        accentColor = TerminalAmber,
                        onPositionChanged = onRightStickMove,
                        modifier = Modifier.testTag("virtual_right_stick")
                    )
                }
            }
        }
    }
}

@Composable
fun ShoulderButton(
    label: String,
    onClick: () -> Unit,
    tag: String
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .size(width = 44.dp, height = 30.dp)
            .testTag(tag),
        shape = RoundedCornerShape(4.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Black,
            contentColor = TerminalAmber
        )
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = TerminalAmber
        )
    }
}

@Composable
fun SystemButton(
    label: String,
    onClick: () -> Unit,
    tag: String
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .size(width = 56.dp, height = 28.dp)
            .testTag(tag),
        shape = RoundedCornerShape(4.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Black,
            contentColor = TerminalAmber
        )
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = TerminalAmber
        )
    }
}

@Composable
fun ActionButton(
    label: String,
    subLabel: String,
    onClick: () -> Unit,
    tag: String
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .testTag(tag),
        shape = CircleShape,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = TerminalAmber
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCardBorder)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = TerminalAmber
            )
            Text(
                text = subLabel,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                color = TerminalAmber.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun AnalogThumbstick(
    label: String,
    accentColor: Color,
    onPositionChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var thumbOffset by remember { mutableStateOf(Offset.Zero) }
    val baseRadius = 52f
    val stickRadius = 24f

    Box(
        modifier = modifier
            .size(108.dp)
            .clip(CircleShape)
            .background(Color.Black)
            .border(1.dp, TerminalCardBorder, CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                        val maxDist = baseRadius - stickRadius
                        val clampedDist = dist.coerceAtMost(maxDist)
                        val factor = if (dist > 0f) clampedDist / dist else 0f
                        thumbOffset = Offset(dx * factor, dy * factor)
                        val normX = (thumbOffset.x / maxDist).coerceIn(-1f, 1f)
                        val normY = (thumbOffset.y / maxDist).coerceIn(-1f, 1f)
                        onPositionChanged(normX, normY)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newX = thumbOffset.x + dragAmount.x
                        val newY = thumbOffset.y + dragAmount.y
                        val dist = sqrt((newX * newX + newY * newY).toDouble()).toFloat()
                        val maxDist = baseRadius - stickRadius
                        val clampedDist = dist.coerceAtMost(maxDist)
                        val factor = if (dist > 0f) clampedDist / dist else 0f
                        thumbOffset = Offset(newX * factor, newY * factor)
                        val normX = (thumbOffset.x / maxDist).coerceIn(-1f, 1f)
                        val normY = (thumbOffset.y / maxDist).coerceIn(-1f, 1f)
                        onPositionChanged(normX, normY)
                    },
                    onDragEnd = {
                        thumbOffset = Offset.Zero
                        onPositionChanged(0f, 0f)
                    },
                    onDragCancel = {
                        thumbOffset = Offset.Zero
                        onPositionChanged(0f, 0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(108.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // Outer ring
            drawCircle(
                color = accentColor.copy(alpha = 0.4f),
                radius = baseRadius,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // Center deadzone ring
            drawCircle(
                color = accentColor.copy(alpha = 0.2f),
                radius = baseRadius * 0.22f,
                center = center,
                style = Stroke(width = 1f)
            )

            // Crosshair
            drawLine(
                color = accentColor.copy(alpha = 0.25f),
                start = Offset(center.x - baseRadius, center.y),
                end = Offset(center.x + baseRadius, center.y),
                strokeWidth = 1f
            )
            drawLine(
                color = accentColor.copy(alpha = 0.25f),
                start = Offset(center.x, center.y - baseRadius),
                end = Offset(center.x, center.y + baseRadius),
                strokeWidth = 1f
            )

            // Thumb cap
            val thumbCenter = Offset(center.x + thumbOffset.x, center.y + thumbOffset.y)
            drawCircle(
                color = Color.Black,
                radius = stickRadius,
                center = thumbCenter
            )
            drawCircle(
                color = if (thumbOffset != Offset.Zero) TerminalGreen else accentColor,
                radius = stickRadius,
                center = thumbCenter,
                style = Stroke(width = 2f)
            )
        }
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = if (thumbOffset != Offset.Zero) TerminalGreen else accentColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp
        )
    }
}
