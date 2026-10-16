package com.example

import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Mouse
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.provider.Settings
import com.example.service.FlickPadOverlayService
import com.example.ui.FlickPadViewModel
import com.example.ui.screens.CursorScreen
import com.example.ui.screens.PlayTestScreen
import com.example.ui.screens.RadialLayoutScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.FlickPadTheme
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalCardBorderBright
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalOrange
import com.example.ui.theme.TerminalSurfaceDark
import com.example.ui.theme.TerminalSurfaceElevated

class MainActivity : ComponentActivity() {

    private val viewModel: FlickPadViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Settings.canDrawOverlays(this)) {
            FlickPadOverlayService.start(this)
        }
        setContent {
            FlickPadTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (viewModel.onGenericMotionEventExternal(event)) {
            return true
        }
        return super.onGenericMotionEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.isGamepadButton() || keyCode in 96..110 || keyCode in 19..22) {
            if (viewModel.onGamepadKeyDown(keyCode)) {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (event.isGamepadButton() || keyCode in 96..110 || keyCode in 19..22) {
            if (viewModel.onGamepadKeyUp(keyCode)) {
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun KeyEvent.isGamepadButton(): Boolean {
        val src = source
        return (src and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
               (src and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK ||
               KeyEvent.isGamepadButton(keyCode)
    }
}

@Composable
fun MainAppScreen(viewModel: FlickPadViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val statusMessage by viewModel.statusMessage.collectAsState()
    val activeMode by viewModel.activeMode.collectAsState()
    val cursorX by viewModel.cursorX.collectAsState()
    val cursorY by viewModel.cursorY.collectAsState()
    val isLeftMouseDown by viewModel.isLeftMouseDown.collectAsState()
    val isRightMouseDown by viewModel.isRightMouseDown.collectAsState()
    val isDragLocked by viewModel.isDragLocked.collectAsState()
    val modeToast by viewModel.modeToast.collectAsState()

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            // Settings and action changes update cleanly without intrusive bottom toast popups
            viewModel.clearStatusMessage()
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!isLandscape) {
                NavigationBar(
                    containerColor = TerminalSurfaceElevated,
                    contentColor = TerminalAmber
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                if (selectedTab == 0) Icons.Filled.PlayArrow else Icons.Outlined.PlayArrow,
                                contentDescription = "Preview"
                            )
                        },
                        label = { Text("Preview", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TerminalGreen,
                            selectedTextColor = TerminalGreen,
                            indicatorColor = TerminalGreen.copy(alpha = 0.20f),
                            unselectedIconColor = TerminalAmber,
                            unselectedTextColor = TerminalAmber
                        ),
                        modifier = Modifier.testTag("tab_preview")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Icon(
                                if (selectedTab == 1) Icons.Filled.Tune else Icons.Outlined.Tune,
                                contentDescription = "Layout"
                            )
                        },
                        label = { Text("Layout", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TerminalGreen,
                            selectedTextColor = TerminalGreen,
                            indicatorColor = TerminalGreen.copy(alpha = 0.20f),
                            unselectedIconColor = TerminalAmber,
                            unselectedTextColor = TerminalAmber
                        ),
                        modifier = Modifier.testTag("tab_layout")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = {
                            Icon(
                                if (selectedTab == 2) Icons.Filled.Mouse else Icons.Outlined.Mouse,
                                contentDescription = "Cursor"
                            )
                        },
                        label = { Text("Cursor", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TerminalGreen,
                            selectedTextColor = TerminalGreen,
                            indicatorColor = TerminalGreen.copy(alpha = 0.20f),
                            unselectedIconColor = TerminalAmber,
                            unselectedTextColor = TerminalAmber
                        ),
                        modifier = Modifier.testTag("tab_cursor")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = {
                            Icon(
                                if (selectedTab == 3) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("Settings", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TerminalGreen,
                            selectedTextColor = TerminalGreen,
                            indicatorColor = TerminalGreen.copy(alpha = 0.20f),
                            unselectedIconColor = TerminalAmber,
                            unselectedTextColor = TerminalAmber
                        ),
                        modifier = Modifier.testTag("tab_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val displayMetrics = context.resources.displayMetrics
            val totalWidthPx = displayMetrics.widthPixels.toFloat()
            val totalHeightPx = displayMetrics.heightPixels.toFloat()
            LaunchedEffect(totalWidthPx, totalHeightPx) {
                viewModel.updateScreenDimensions(totalWidthPx, totalHeightPx)
            }

            if (isLandscape) {
                Row(modifier = Modifier.fillMaxSize()) {
                    NavigationRail(
                        containerColor = TerminalSurfaceElevated,
                        contentColor = TerminalAmber,
                        modifier = Modifier.fillMaxHeight().testTag("nav_rail")
                    ) {
                        NavigationRailItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = {
                                Icon(
                                    if (selectedTab == 0) Icons.Filled.PlayArrow else Icons.Outlined.PlayArrow,
                                    contentDescription = "Preview"
                                )
                            },
                            label = { Text("Preview", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = TerminalGreen,
                                selectedTextColor = TerminalGreen,
                                indicatorColor = TerminalGreen.copy(alpha = 0.20f),
                                unselectedIconColor = TerminalAmber,
                                unselectedTextColor = TerminalAmber
                            ),
                            modifier = Modifier.testTag("rail_preview")
                        )

                        NavigationRailItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = {
                                Icon(
                                    if (selectedTab == 1) Icons.Filled.Tune else Icons.Outlined.Tune,
                                    contentDescription = "Layout"
                                )
                            },
                            label = { Text("Layout", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = TerminalGreen,
                                selectedTextColor = TerminalGreen,
                                indicatorColor = TerminalGreen.copy(alpha = 0.20f),
                                unselectedIconColor = TerminalAmber,
                                unselectedTextColor = TerminalAmber
                            ),
                            modifier = Modifier.testTag("rail_layout")
                        )

                        NavigationRailItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = {
                                Icon(
                                    if (selectedTab == 2) Icons.Filled.Mouse else Icons.Outlined.Mouse,
                                    contentDescription = "Cursor"
                                )
                            },
                            label = { Text("Cursor", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = TerminalGreen,
                                selectedTextColor = TerminalGreen,
                                indicatorColor = TerminalGreen.copy(alpha = 0.20f),
                                unselectedIconColor = TerminalAmber,
                                unselectedTextColor = TerminalAmber
                            ),
                            modifier = Modifier.testTag("rail_cursor")
                        )

                        NavigationRailItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            icon = {
                                Icon(
                                    if (selectedTab == 3) Icons.Filled.Settings else Icons.Outlined.Settings,
                                    contentDescription = "Settings"
                                )
                            },
                            label = { Text("Settings", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = TerminalGreen,
                                selectedTextColor = TerminalGreen,
                                indicatorColor = TerminalGreen.copy(alpha = 0.20f),
                                unselectedIconColor = TerminalAmber,
                                unselectedTextColor = TerminalAmber
                            ),
                            modifier = Modifier.testTag("rail_settings")
                        )
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        when (selectedTab) {
                            0 -> PlayTestScreen(viewModel = viewModel)
                            1 -> RadialLayoutScreen(viewModel = viewModel)
                            2 -> CursorScreen(viewModel = viewModel)
                            3 -> SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (selectedTab) {
                        0 -> PlayTestScreen(viewModel = viewModel)
                        1 -> RadialLayoutScreen(viewModel = viewModel)
                        2 -> CursorScreen(viewModel = viewModel)
                        3 -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }

        }
    }

    // Real on-screen pointer cursor overlay when Cursor Mode is active - spans full screen including status bar
        if (activeMode == "CURSOR_CONTROL") {
            com.example.ui.components.OnScreenCursorOverlay(
                cursorX = cursorX,
                cursorY = cursorY,
                isLeftMouseDown = isLeftMouseDown,
                isRightMouseDown = isRightMouseDown,
                isDragLocked = isDragLocked,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Very brief mode switch toast popup stylized as [off], [text_input], [cursor]
        AnimatedVisibility(
            visible = modeToast != null,
            enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.85f),
            exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.85f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = if (isLandscape) 16.dp else 52.dp)
                .testTag("mode_change_toast")
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black,
                border = BorderStroke(1.5.dp, TerminalGreen),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = modeToast ?: "",
                        color = TerminalGreen,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
