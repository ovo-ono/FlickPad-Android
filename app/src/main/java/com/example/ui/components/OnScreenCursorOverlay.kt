package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FlickCyan
import com.example.ui.theme.FlickGreen
import com.example.ui.theme.FlickViolet
import kotlin.math.roundToInt

@Composable
fun OnScreenCursorOverlay(
    cursorX: Float,
    cursorY: Float,
    isLeftMouseDown: Boolean,
    isRightMouseDown: Boolean,
    isDragLocked: Boolean,
    modifier: Modifier = Modifier
) {
    val clickPulse = remember { Animatable(0f) }

    LaunchedEffect(isLeftMouseDown, isRightMouseDown) {
        if (isLeftMouseDown || isRightMouseDown) {
            clickPulse.snapTo(1f)
            clickPulse.animateTo(0f, animationSpec = tween(220))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("on_screen_real_cursor_overlay")
    ) {
        // Floating Cursor Pointer
        Box(
            modifier = Modifier
                .offset { IntOffset(cursorX.roundToInt(), cursorY.roundToInt()) }
                .size(44.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Click ripple
                if (clickPulse.value > 0f) {
                    val pulseRadius = 14f + (1f - clickPulse.value) * 20f
                    drawCircle(
                        color = (if (isLeftMouseDown) FlickCyan else FlickViolet).copy(alpha = clickPulse.value * 0.7f),
                        radius = pulseRadius,
                        center = Offset(2f, 2f),
                        style = Stroke(width = 3f)
                    )
                }

                // Precision Mouse Arrow Pointer
                val arrowPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(0f, 28f)
                    lineTo(8f, 21f)
                    lineTo(15f, 32f)
                    lineTo(20f, 29f)
                    lineTo(13f, 17f)
                    lineTo(22f, 16f)
                    close()
                }

                val fill = when {
                    isLeftMouseDown -> FlickCyan
                    isRightMouseDown -> FlickViolet
                    isDragLocked -> FlickGreen
                    else -> Color.White
                }

                // Black outer outline for contrast on any background
                drawPath(arrowPath, color = Color.Black, style = Stroke(width = 3.5f))
                // Vibrant fill
                drawPath(arrowPath, color = fill)
            }

            // Drag lock indicator badge
            if (isDragLocked) {
                Box(
                    modifier = Modifier
                        .offset(x = 18.dp, y = 18.dp)
                        .background(FlickGreen, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text("LOCK", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}
