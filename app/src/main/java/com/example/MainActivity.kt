package com.example

import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.example.ui.FlickPadViewModel
import com.example.ui.screens.CursorScreen
import com.example.ui.screens.PlayTestScreen
import com.example.ui.screens.RadialLayoutScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.FlickPadTheme
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
        setContent {
            FlickPadTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        val sources = event.source
        if ((sources and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK ||
            (sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
        ) {
            val device = event.device
            // Read Left stick with device axis range normalization
            var lx = event.getAxisValue(MotionEvent.AXIS_X)
            var ly = event.getAxisValue(MotionEvent.AXIS_Y)
            if (device != null) {
                val xRange = device.getMotionRange(MotionEvent.AXIS_X, event.source)
                if (xRange != null && xRange.flat > 0f && Math.abs(lx) < xRange.flat) {
                    lx = 0f
                }
                val yRange = device.getMotionRange(MotionEvent.AXIS_Y, event.source)
                if (yRange != null && yRange.flat > 0f && Math.abs(ly) < yRange.flat) {
                    ly = 0f
                }
            }
            viewModel.onLeftStickMove(lx, ly)

            // Read Right stick: Standard Android gamepad uses AXIS_Z and AXIS_RZ.
            // Many USB/Bluetooth gamepads (Switch Pro, DualShock/DualSense, generic DirectInput/XInput adapters)
            // report AXIS_RX and AXIS_RY instead.
            var rx = event.getAxisValue(MotionEvent.AXIS_Z)
            var ry = event.getAxisValue(MotionEvent.AXIS_RZ)
            if (rx == 0f && ry == 0f) {
                rx = event.getAxisValue(MotionEvent.AXIS_RX)
                ry = event.getAxisValue(MotionEvent.AXIS_RY)
            }
            if (device != null) {
                val zRange = device.getMotionRange(MotionEvent.AXIS_Z, event.source)
                    ?: device.getMotionRange(MotionEvent.AXIS_RX, event.source)
                if (zRange != null && zRange.flat > 0f && Math.abs(rx) < zRange.flat) {
                    rx = 0f
                }
                val rzRange = device.getMotionRange(MotionEvent.AXIS_RZ, event.source)
                    ?: device.getMotionRange(MotionEvent.AXIS_RY, event.source)
                if (rzRange != null && rzRange.flat > 0f && Math.abs(ry) < rzRange.flat) {
                    ry = 0f
                }
            }
            viewModel.onRightStickMove(rx, ry)
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

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
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
                        indicatorColor = TerminalGreen.copy(alpha = 0.25f),
                        unselectedIconColor = TerminalAmber.copy(alpha = 0.5f),
                        unselectedTextColor = TerminalAmber.copy(alpha = 0.5f)
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
                        indicatorColor = TerminalGreen.copy(alpha = 0.25f),
                        unselectedIconColor = TerminalAmber.copy(alpha = 0.5f),
                        unselectedTextColor = TerminalAmber.copy(alpha = 0.5f)
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
                        indicatorColor = TerminalGreen.copy(alpha = 0.25f),
                        unselectedIconColor = TerminalAmber.copy(alpha = 0.5f),
                        unselectedTextColor = TerminalAmber.copy(alpha = 0.5f)
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
                        indicatorColor = TerminalGreen.copy(alpha = 0.25f),
                        unselectedIconColor = TerminalAmber.copy(alpha = 0.5f),
                        unselectedTextColor = TerminalAmber.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.padding(innerPadding)) {
                when (selectedTab) {
                    0 -> PlayTestScreen(viewModel = viewModel)
                    1 -> RadialLayoutScreen(viewModel = viewModel)
                    2 -> CursorScreen(viewModel = viewModel)
                    3 -> SettingsScreen(viewModel = viewModel)
                }
            }

            // Real on-screen pointer cursor overlay when Cursor Mode is active
            if (activeMode == "CURSOR_CONTROL") {
                com.example.ui.components.OnScreenCursorOverlay(
                    cursorX = cursorX,
                    cursorY = cursorY,
                    isLeftMouseDown = isLeftMouseDown,
                    isRightMouseDown = isRightMouseDown,
                    isDragLocked = isDragLocked
                )
            }
        }
    }
}
