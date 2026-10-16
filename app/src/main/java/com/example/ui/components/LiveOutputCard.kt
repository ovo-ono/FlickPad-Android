package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalCardBorder
import com.example.ui.theme.TerminalCardBorderBright
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalOrange
import com.example.ui.theme.TerminalRed
import com.example.ui.theme.TerminalSurfaceElevated
import com.example.ui.theme.TerminalTextPrimary

/**
 * Clean outlined test input field with an X button to clear all.
 * Styled in retro terminal aesthetic.
 */
@Composable
fun LiveOutputCard(
    text: String,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
            .border(1.dp, TerminalCardBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = "terminal@flickpad:~$ _",
                        color = TerminalAmber.copy(alpha = 0.5f),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                } else {
                    Text(
                        text = text,
                        color = TerminalGreen,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (text.isNotEmpty()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.testTag("button_clear")
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear All",
                        tint = TerminalRed
                    )
                }
            }
        }
    }
}
