package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalGreen
import kotlin.math.sqrt

@Composable
fun InteractiveDeadzoneDepiction(
    title: String, // "Left Stick" or "Right Stick" only
    deadzone: Float,
    stickX: Float,
    stickY: Float,
    accentColor: Color = TerminalAmber,
    onTestDrag: ((Float, Float) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var touchX by remember { mutableFloatStateOf(0f) }
    var touchY by remember { mutableFloatStateOf(0f) }

    val currentX = if (touchX != 0f || touchY != 0f) touchX else stickX
    val currentY = if (touchX != 0f || touchY != 0f) touchY else stickY
    val magnitude = sqrt((currentX * currentX + currentY * currentY).toDouble()).toFloat()
    val isEngaged = magnitude >= deadzone

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
            .border(1.dp, if (isEngaged) TerminalGreen else TerminalCardBorder, RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ONLY the title: "Left Stick" or "Right Stick"
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (isEngaged) TerminalGreen else TerminalAmber,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.0f)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black)
                .border(1.dp, TerminalCardBorder.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                .pointerInput(deadzone) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val minDim = minOf(size.width, size.height).toFloat()
                            val maxR = (minDim / 2f) * 0.9f
                            val dx = (offset.x - center.x) / maxR
                            val dy = (offset.y - center.y) / maxR
                            touchX = dx.coerceIn(-1f, 1f)
                            touchY = dy.coerceIn(-1f, 1f)
                            onTestDrag?.invoke(touchX, touchY)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val minDim = minOf(size.width, size.height).toFloat()
                            val maxR = (minDim / 2f) * 0.9f
                            val dx = (change.position.x - center.x) / maxR
                            val dy = (change.position.y - center.y) / maxR
                            touchX = dx.coerceIn(-1f, 1f)
                            touchY = dy.coerceIn(-1f, 1f)
                            onTestDrag?.invoke(touchX, touchY)
                        },
                        onDragEnd = {
                            touchX = 0f
                            touchY = 0f
                            onTestDrag?.invoke(0f, 0f)
                        },
                        onDragCancel = {
                            touchX = 0f
                            touchY = 0f
                            onTestDrag?.invoke(0f, 0f)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val outerRadius = (size.minDimension / 2f) * 0.88f
                val deadzoneRadius = outerRadius * deadzone.coerceIn(0.05f, 0.95f)

                // High-clarity crosshair grid (phosphor style)
                drawLine(
                    color = TerminalAmber.copy(alpha = 0.25f),
                    start = Offset(center.x - outerRadius, center.y),
                    end = Offset(center.x + outerRadius, center.y),
                    strokeWidth = 1f
                )
                drawLine(
                    color = TerminalAmber.copy(alpha = 0.25f),
                    start = Offset(center.x, center.y - outerRadius),
                    end = Offset(center.x, center.y + outerRadius),
                    strokeWidth = 1f
                )

                // Outer boundary ring (full 100% range)
                drawCircle(
                    color = TerminalAmber.copy(alpha = 0.4f),
                    radius = outerRadius,
                    center = center,
                    style = Stroke(width = 1.5f)
                )

                // Shaded Deadzone Disc
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            TerminalAmber.copy(alpha = 0.25f),
                            TerminalAmber.copy(alpha = 0.05f)
                        ),
                        center = center,
                        radius = deadzoneRadius
                    ),
                    radius = deadzoneRadius,
                    center = center
                )
                drawCircle(
                    color = if (isEngaged) TerminalGreen else TerminalAmber,
                    radius = deadzoneRadius,
                    center = center,
                    style = Stroke(width = 2f)
                )

                // Live stick position dot
                val dotX = center.x + currentX.coerceIn(-1f, 1f) * outerRadius
                val dotY = center.y + currentY.coerceIn(-1f, 1f) * outerRadius
                val dotCenter = Offset(dotX, dotY)

                val dotColor = if (isEngaged) TerminalGreen else TerminalAmber
                drawCircle(
                    color = dotColor,
                    radius = 8f,
                    center = dotCenter
                )
                drawCircle(
                    color = Color.Black,
                    radius = 3.5f,
                    center = dotCenter
                )
                if (isEngaged) {
                    drawCircle(
                        color = TerminalGreen.copy(alpha = 0.6f),
                        radius = 14f,
                        center = dotCenter,
                        style = Stroke(width = 1.5f)
                    )
                }
            }
        }
    }
}
