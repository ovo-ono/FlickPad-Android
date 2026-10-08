package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.KeyEvent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.FlickPadApplication
import com.example.data.ButtonCombination
import com.example.data.DefaultProfiles
import com.example.data.KeyboardLayout
import com.example.data.ProfileEntity
import com.example.data.ZoneData
import com.example.engine.CursorAccelerationCurve
import com.example.engine.CursorEngine
import com.example.engine.GamepadDeviceManager
import com.example.engine.RadialEngine
import com.example.model.GamepadAction
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FlickPadViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as FlickPadApplication).repository
    private val vibrator = application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    private val clipboardManager = application.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val gamepadManager = GamepadDeviceManager(application)

    // Profiles from Room DB
    val allProfiles: StateFlow<List<ProfileEntity>> = repository.allProfiles.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentProfile = MutableStateFlow<ProfileEntity?>(null)
    val currentProfile: StateFlow<ProfileEntity?> = _currentProfile.asStateFlow()

    // Mode: "OFF", "TEXT_INPUT", "CURSOR_CONTROL"
    private val _activeMode = MutableStateFlow("TEXT_INPUT")
    val activeMode: StateFlow<String> = _activeMode.asStateFlow()

    // Typed text buffer
    private val _typedText = MutableStateFlow("")
    val typedText: StateFlow<String> = _typedText.asStateFlow()

    // Modifier states
    private val _isShiftActive = MutableStateFlow(false)
    val isShiftActive: StateFlow<Boolean> = _isShiftActive.asStateFlow()

    private val _isCapsLocked = MutableStateFlow(false)
    val isCapsLocked: StateFlow<Boolean> = _isCapsLocked.asStateFlow()

    // Analog stick states (-1f to +1f)
    private val _leftStickX = MutableStateFlow(0f)
    val leftStickX: StateFlow<Float> = _leftStickX.asStateFlow()

    private val _leftStickY = MutableStateFlow(0f)
    val leftStickY: StateFlow<Float> = _leftStickY.asStateFlow()

    private val _rightStickX = MutableStateFlow(0f)
    val rightStickX: StateFlow<Float> = _rightStickX.asStateFlow()

    private val _rightStickY = MutableStateFlow(0f)
    val rightStickY: StateFlow<Float> = _rightStickY.asStateFlow()

    // Active zone (0 = NEUTRAL, 1..8 = directional)
    private val _activeZoneIndex = MutableStateFlow(0)
    val activeZoneIndex: StateFlow<Int> = _activeZoneIndex.asStateFlow()

    // Armed sector on right stick (0..7, or null if below deadzone)
    private val _armedSectorIndex = MutableStateFlow<Int?>(null)
    val armedSectorIndex: StateFlow<Int?> = _armedSectorIndex.asStateFlow()

    // Recent character flicked for animation
    private val _lastFlickedChar = MutableStateFlow<String?>(null)
    val lastFlickedChar: StateFlow<String?> = _lastFlickedChar.asStateFlow()

    // Gamepad active pressed buttons
    private val _pressedButtons = MutableStateFlow<Set<String>>(emptySet())
    val pressedButtons: StateFlow<Set<String>> = _pressedButtons.asStateFlow()

    // Cursor State
    private val _cursorX = MutableStateFlow(200f)
    val cursorX: StateFlow<Float> = _cursorX.asStateFlow()

    private val _cursorY = MutableStateFlow(250f)
    val cursorY: StateFlow<Float> = _cursorY.asStateFlow()

    private val _isLeftMouseDown = MutableStateFlow(false)
    val isLeftMouseDown: StateFlow<Boolean> = _isLeftMouseDown.asStateFlow()

    private val _isRightMouseDown = MutableStateFlow(false)
    val isRightMouseDown: StateFlow<Boolean> = _isRightMouseDown.asStateFlow()

    private val _isDragLocked = MutableStateFlow(false)
    val isDragLocked: StateFlow<Boolean> = _isDragLocked.asStateFlow()

    private val _scrollOffset = MutableStateFlow(0f)
    val scrollOffset: StateFlow<Float> = _scrollOffset.asStateFlow()

    private val _zoomLevel = MutableStateFlow(1.0f)
    val zoomLevel: StateFlow<Float> = _zoomLevel.asStateFlow()

    // Overlay visual visibility & auto-hide
    private val _overlayVisible = MutableStateFlow(true)
    val overlayVisible: StateFlow<Boolean> = _overlayVisible.asStateFlow()

    private var autoHideJob: Job? = null

    // Layouts K1..K16 & Modifier Layers ("Base", "Shift", "Alt", "Fn", "Ctrl")
    private val _allLayouts = MutableStateFlow<List<KeyboardLayout>>(KeyboardLayout.createDefaultLayouts())
    val allLayouts: StateFlow<List<KeyboardLayout>> = _allLayouts.asStateFlow()

    private val _activeLayoutId = MutableStateFlow("K1")
    val activeLayoutId: StateFlow<String> = _activeLayoutId.asStateFlow()

    private val _activeModifierLayer = MutableStateFlow("Base")
    val activeModifierLayer: StateFlow<String> = _activeModifierLayer.asStateFlow()

    // Cached parsed zones & mappings
    private val _parsedZones = MutableStateFlow<List<ZoneData>>(emptyList())
    val parsedZones: StateFlow<List<ZoneData>> = _parsedZones.asStateFlow()

    private val _parsedMappings = MutableStateFlow<Map<String, String>>(emptyMap())
    val parsedMappings: StateFlow<Map<String, String>> = _parsedMappings.asStateFlow()

    private val _parsedCursorMappings = MutableStateFlow<Map<String, String>>(emptyMap())
    val parsedCursorMappings: StateFlow<Map<String, String>> = _parsedCursorMappings.asStateFlow()

    // Button Combinations for Text Input Mode & Cursor Mode
    private val _buttonCombinations = MutableStateFlow<List<ButtonCombination>>(ButtonCombination.getDefaultCombinations())
    val buttonCombinations: StateFlow<List<ButtonCombination>> = _buttonCombinations.asStateFlow()

    private val _cursorCombinations = MutableStateFlow<List<ButtonCombination>>(ButtonCombination.getDefaultCombinations())
    val cursorCombinations: StateFlow<List<ButtonCombination>> = _cursorCombinations.asStateFlow()

    private val _leftStickRadialActions = MutableStateFlow<List<String>>(DefaultProfiles.getDefaultRadialStickActions())
    val leftStickRadialActions: StateFlow<List<String>> = _leftStickRadialActions.asStateFlow()

    private val _rightStickRadialActions = MutableStateFlow<List<String>>(DefaultProfiles.getDefaultRadialStickActions())
    val rightStickRadialActions: StateFlow<List<String>> = _rightStickRadialActions.asStateFlow()

    // Shortcut recording state (1 to 6 keys)
    private val _recordingShortcutTarget = MutableStateFlow<String?>(null)
    val recordingShortcutTarget: StateFlow<String?> = _recordingShortcutTarget.asStateFlow()

    private val _recordedKeys = MutableStateFlow<List<String>>(emptyList())
    val recordedKeys: StateFlow<List<String>> = _recordedKeys.asStateFlow()

    // Message snackbar/toast event
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        viewModelScope.launch {
            allProfiles.collect { profiles ->
                if (_currentProfile.value == null && profiles.isNotEmpty()) {
                    val defaultProf = profiles.firstOrNull { it.isDefault } ?: profiles.first()
                    loadProfile(defaultProf)
                }
            }
        }
    }

    fun loadProfile(profile: ProfileEntity) {
        _currentProfile.value = profile
        _activeMode.value = profile.mode

        // Parse layouts K1..K16
        val loadedLayouts = KeyboardLayout.jsonToLayouts(profile.layoutsJson)
        _allLayouts.value = loadedLayouts
        _activeLayoutId.value = profile.activeLayoutId

        val activeLayout = loadedLayouts.firstOrNull { it.id == profile.activeLayoutId } ?: loadedLayouts.firstOrNull()
        if (activeLayout != null) {
            _parsedZones.value = activeLayout.getZonesForModifier(_activeModifierLayer.value)
        } else {
            _parsedZones.value = DefaultProfiles.jsonToZones(profile.zonesJson)
        }

        // Parse Button Combinations
        _buttonCombinations.value = ButtonCombination.jsonToCombinations(profile.buttonCombinationsJson)
        _cursorCombinations.value = ButtonCombination.jsonToCombinations(profile.cursorCombinationsJson)

        _parsedMappings.value = DefaultProfiles.jsonToMappings(profile.mappingsJson)
        _parsedCursorMappings.value = DefaultProfiles.jsonToMappings(profile.cursorMappingsJson)
        _leftStickRadialActions.value = DefaultProfiles.jsonToRadialActions(profile.leftStickRadialActionsJson)
        _rightStickRadialActions.value = DefaultProfiles.jsonToRadialActions(profile.rightStickRadialActionsJson)

        if (_activeMode.value == "TEXT_INPUT") {
            _overlayVisible.value = true
        }
        resetAutoHideTimer()
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun showStatus(msg: String) {
        _statusMessage.value = msg
    }

    private fun triggerHaptic(durationMs: Long = 25) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    /**
     * Spawns / keeps overlay alive when Text Input mode is active,
     * and automatically counts down to hide according to overlayAutoHideSeconds.
     */
    private fun resetAutoHideTimer() {
        autoHideJob?.cancel()
        if (_activeMode.value == "TEXT_INPUT") {
            _overlayVisible.value = true
            val seconds = _currentProfile.value?.overlayAutoHideSeconds ?: 0f
            if (seconds > 0f) {
                autoHideJob = viewModelScope.launch {
                    delay((seconds * 1000).toLong())
                    _overlayVisible.value = false
                }
            }
        }
    }

    // ==========================================
    // STICK INPUT PROCESSING
    // ==========================================
    fun onLeftStickMove(x: Float, y: Float) {
        _leftStickX.value = x
        _leftStickY.value = y
        resetAutoHideTimer()

        val profile = _currentProfile.value ?: return
        val deadzone = profile.leftDeadzone

        if (_activeMode.value == "TEXT_INPUT") {
            val newZone = RadialEngine.getZoneFromStick(x, y, deadzone)
            if (newZone != _activeZoneIndex.value) {
                _activeZoneIndex.value = newZone
                triggerHaptic(15)
            }
        } else if (_activeMode.value == "CURSOR_CONTROL") {
            if (profile.leftStickMoveCursor) {
                val curve = try {
                    CursorAccelerationCurve.valueOf(profile.mouseAccelerationCurve)
                } catch (_: Exception) {
                    CursorAccelerationCurve.EXPONENTIAL
                }
                val (dx, dy) = CursorEngine.computeMovementDelta(x, y, deadzone, profile.mouseSensitivity, curve)
                _cursorX.value = (_cursorX.value + dx).coerceIn(10f, 950f)
                _cursorY.value = (_cursorY.value + dy).coerceIn(10f, 1800f)
            } else {
                // Radial 8-directional actions
                val sector = RadialEngine.getSectorFromStick(x, y, deadzone)
                if (sector != null) {
                    val actionName = _leftStickRadialActions.value.getOrNull(sector) ?: "None"
                    executeRadialDirectionAction(actionName)
                }
            }
        }
    }

    fun onRightStickMove(x: Float, y: Float) {
        _rightStickX.value = x
        _rightStickY.value = y
        resetAutoHideTimer()

        val profile = _currentProfile.value ?: return
        val deadzone = profile.rightDeadzone

        if (_activeMode.value == "TEXT_INPUT") {
            val sector = RadialEngine.getSectorFromStick(x, y, deadzone)
            if (sector != null) {
                if (_armedSectorIndex.value != sector) {
                    _armedSectorIndex.value = sector
                    triggerHaptic(20)
                }
            } else {
                // Flick released!
                val armed = _armedSectorIndex.value
                if (armed != null) {
                    commitFlick(armed)
                    _armedSectorIndex.value = null
                }
            }
        } else if (_activeMode.value == "CURSOR_CONTROL") {
            if (profile.rightStickMoveCursor) {
                val curve = try {
                    CursorAccelerationCurve.valueOf(profile.mouseAccelerationCurve)
                } catch (_: Exception) {
                    CursorAccelerationCurve.EXPONENTIAL
                }
                val (dx, dy) = CursorEngine.computeMovementDelta(x, y, deadzone, profile.mouseSensitivity, curve)
                _cursorX.value = (_cursorX.value + dx).coerceIn(10f, 950f)
                _cursorY.value = (_cursorY.value + dy).coerceIn(10f, 1800f)
            } else {
                // Radial 8-directional actions
                val sector = RadialEngine.getSectorFromStick(x, y, deadzone)
                if (sector != null) {
                    val actionName = _rightStickRadialActions.value.getOrNull(sector) ?: "None"
                    executeRadialDirectionAction(actionName)
                }
            }
        }
    }

    private fun executeRadialDirectionAction(action: String) {
        when (action) {
            "Scrolling Up" -> _scrollOffset.value = (_scrollOffset.value - 12f).coerceIn(-1000f, 1000f)
            "Scrolling Down" -> _scrollOffset.value = (_scrollOffset.value + 12f).coerceIn(-1000f, 1000f)
            "Scrolling Left" -> _scrollOffset.value = (_scrollOffset.value - 8f).coerceIn(-1000f, 1000f)
            "Scrolling Right" -> _scrollOffset.value = (_scrollOffset.value + 8f).coerceIn(-1000f, 1000f)
            "Zooming In" -> _zoomLevel.value = (_zoomLevel.value + 0.02f).coerceIn(0.5f, 3.0f)
            "Zooming Out" -> _zoomLevel.value = (_zoomLevel.value - 0.02f).coerceIn(0.5f, 3.0f)
        }
    }

    private fun commitFlick(sector: Int) {
        val zones = _parsedZones.value
        val zoneIdx = _activeZoneIndex.value
        val zone = zones.firstOrNull { it.index == zoneIdx } ?: zones.firstOrNull() ?: return
        if (sector in zone.symbols.indices) {
            var char = zone.symbols[sector]
            if (char.isNotBlank()) {
                if (_isShiftActive.value || _isCapsLocked.value) {
                    char = if (char.length == 1 && char[0].isLetter()) char.uppercase() else char
                    if (_isShiftActive.value && !_isCapsLocked.value) {
                        _isShiftActive.value = false
                    }
                }
                appendCharacter(char)
                _lastFlickedChar.value = char
                triggerHaptic(35)
            }
        }
    }

    // ==========================================
    // GAMEPAD BUTTON DISPATCHING & COMBINATIONS
    // ==========================================
    fun onGamepadKeyDown(keyCode: Int): Boolean {
        val keyName = KeyEvent.keyCodeToString(keyCode)
        val newPressed = _pressedButtons.value + keyName
        _pressedButtons.value = newPressed
        resetAutoHideTimer()

        // Handle recording mode
        val recordingTarget = _recordingShortcutTarget.value
        if (recordingTarget != null) {
            recordKey(keyName)
            return true
        }

        // Check configurable shortcuts (1 to 6 keys)
        if (checkConfigurableShortcuts()) {
            return true
        }

        // Check chained Button Combinations (2 to 4 buttons)
        val combinationsList = if (_activeMode.value == "CURSOR_CONTROL") {
            _cursorCombinations.value
        } else {
            _buttonCombinations.value
        }

        for (combo in combinationsList) {
            val activeKeys = combo.buttons.filter { it.isNotBlank() && it != "NONE" }
            if (activeKeys.size >= 2 && activeKeys.all { newPressed.contains(it) }) {
                val act = GamepadAction.fromCode(combo.actionCode)
                if (act != GamepadAction.NONE) {
                    executeAction(act, isDown = true)
                    triggerHaptic(40)
                    return true
                }
            }
        }

        // Normal single button dispatch
        val mappings = if (_activeMode.value == "CURSOR_CONTROL") {
            _parsedCursorMappings.value
        } else {
            _parsedMappings.value
        }

        val actionCode = mappings[keyName]
        if (actionCode != null) {
            val action = GamepadAction.fromCode(actionCode)
            executeAction(action, isDown = true)
            return true
        }
        return false
    }

    fun onGamepadKeyUp(keyCode: Int): Boolean {
        val keyName = KeyEvent.keyCodeToString(keyCode)
        _pressedButtons.value = _pressedButtons.value - keyName

        val mappings = if (_activeMode.value == "CURSOR_CONTROL") {
            _parsedCursorMappings.value
        } else {
            _parsedMappings.value
        }

        val actionCode = mappings[keyName]
        if (actionCode != null) {
            val action = GamepadAction.fromCode(actionCode)
            executeAction(action, isDown = false)
            return true
        }
        return false
    }

    // ==========================================
    // SHORTCUT / HOTKEY RECORDING (1 to 6 keys)
    // ==========================================
    fun startRecordingShortcut(target: String) {
        _recordingShortcutTarget.value = target
        _recordedKeys.value = emptyList()
        showStatus("Recording $target shortcut: press 1 to 6 keys...")
    }

    fun recordKey(keyName: String) {
        val current = _recordedKeys.value
        val simplifiedName = simplifyKeyName(keyName)
        if (!current.contains(simplifiedName) && current.size < 6) {
            _recordedKeys.value = current + simplifiedName
            triggerHaptic(30)
        }
    }

    fun saveRecordedShortcut() {
        val target = _recordingShortcutTarget.value ?: return
        val combo = _recordedKeys.value.joinToString(" + ")
        if (combo.isBlank()) {
            _recordingShortcutTarget.value = null
            return
        }

        val current = _currentProfile.value ?: return
        val updated = when (target) {
            "TEXT_INPUT" -> current.copy(shortcutTextInput = combo)
            "OVERLAY" -> current.copy(shortcutOverlay = combo)
            "CURSOR" -> current.copy(shortcutCursor = combo)
            else -> current
        }
        _currentProfile.value = updated
        _recordingShortcutTarget.value = null
        _recordedKeys.value = emptyList()

        viewModelScope.launch {
            repository.updateProfile(updated)
            showStatus("Saved $target shortcut: $combo")
        }
    }

    fun cancelRecordingShortcut() {
        _recordingShortcutTarget.value = null
        _recordedKeys.value = emptyList()
        showStatus("Recording cancelled")
    }

    private fun simplifyKeyName(rawName: String): String {
        return rawName.replace("KEYCODE_BUTTON_", "")
            .replace("KEYCODE_", "")
            .replace("THUMBL", "L3")
            .replace("THUMBR", "R3")
    }

    private fun checkConfigurableShortcuts(): Boolean {
        val pressedSimplified = _pressedButtons.value.map { simplifyKeyName(it) }.toSet()
        val profile = _currentProfile.value ?: return false

        fun matchCombo(comboStr: String): Boolean {
            if (comboStr.isBlank()) return false
            val required = comboStr.split("+").map { it.trim() }.filter { it.isNotEmpty() }
            return required.isNotEmpty() && required.all { pressedSimplified.contains(it) }
        }

        if (matchCombo(profile.shortcutTextInput)) {
            setMode("TEXT_INPUT")
            triggerHaptic(40)
            showStatus("Mode: Text Input")
            return true
        }
        if (matchCombo(profile.shortcutCursor)) {
            setMode("CURSOR_CONTROL")
            triggerHaptic(40)
            showStatus("Mode: Cursor Control")
            return true
        }
        if (matchCombo(profile.shortcutOverlay)) {
            toggleOverlay()
            triggerHaptic(40)
            return true
        }

        return false
    }

    // ==========================================
    // ACTION EXECUTION DISPATCHER
    // ==========================================
    fun executeAction(action: GamepadAction, isDown: Boolean = true) {
        if (!isDown) {
            if (action == GamepadAction.MOUSE_LEFT_CLICK && !_isDragLocked.value) {
                _isLeftMouseDown.value = false
            } else if (action == GamepadAction.MOUSE_RIGHT_CLICK) {
                _isRightMouseDown.value = false
            }
            return
        }

        triggerHaptic(20)
        when (action) {
            GamepadAction.NONE -> { /* No Action */ }

            // Layout Cycling & Selection
            GamepadAction.CYCLE_LAYOUTS -> cycleLayouts()
            GamepadAction.LAYOUT_K1 -> activateLayout("K1")
            GamepadAction.LAYOUT_K2 -> activateLayout("K2")
            GamepadAction.LAYOUT_K3 -> activateLayout("K3")
            GamepadAction.LAYOUT_K4 -> activateLayout("K4")
            GamepadAction.LAYOUT_K5 -> activateLayout("K5")
            GamepadAction.LAYOUT_K6 -> activateLayout("K6")
            GamepadAction.LAYOUT_K7 -> activateLayout("K7")
            GamepadAction.LAYOUT_K8 -> activateLayout("K8")
            GamepadAction.LAYOUT_K9 -> activateLayout("K9")
            GamepadAction.LAYOUT_K10 -> activateLayout("K10")
            GamepadAction.LAYOUT_K11 -> activateLayout("K11")
            GamepadAction.LAYOUT_K12 -> activateLayout("K12")
            GamepadAction.LAYOUT_K13 -> activateLayout("K13")
            GamepadAction.LAYOUT_K14 -> activateLayout("K14")
            GamepadAction.LAYOUT_K15 -> activateLayout("K15")
            GamepadAction.LAYOUT_K16 -> activateLayout("K16")

            // Standard Keyboard Function Keys
            GamepadAction.KEY_ESC -> showStatus("Esc")
            GamepadAction.KEY_TAB -> appendCharacter("    ")
            GamepadAction.KEY_CAPS_LOCK -> toggleCaps()
            GamepadAction.KEY_SHIFT -> toggleShift()
            GamepadAction.KEY_CTRL -> showStatus("Ctrl")
            GamepadAction.KEY_ALT -> showStatus("Alt")
            GamepadAction.KEY_WIN -> showStatus("Win / Meta")
            GamepadAction.KEY_SPACE -> appendCharacter(" ")
            GamepadAction.KEY_ENTER -> appendCharacter("\n")
            GamepadAction.KEY_BACKSPACE -> backspace()
            GamepadAction.KEY_DELETE -> backspace()
            GamepadAction.KEY_INSERT -> showStatus("Insert")
            GamepadAction.KEY_HOME -> showStatus("Home")
            GamepadAction.KEY_END -> showStatus("End")
            GamepadAction.KEY_PAGE_UP -> showStatus("Page Up")
            GamepadAction.KEY_PAGE_DOWN -> showStatus("Page Down")
            GamepadAction.KEY_PRINTSCREEN -> showStatus("Print Screen")
            GamepadAction.KEY_SCROLL_LOCK -> showStatus("Scroll Lock")
            GamepadAction.KEY_PAUSE -> showStatus("Pause / Break")

            // F1 - F12
            GamepadAction.KEY_F1, GamepadAction.KEY_F2, GamepadAction.KEY_F3, GamepadAction.KEY_F4,
            GamepadAction.KEY_F5, GamepadAction.KEY_F6, GamepadAction.KEY_F7, GamepadAction.KEY_F8,
            GamepadAction.KEY_F9, GamepadAction.KEY_F10, GamepadAction.KEY_F11, GamepadAction.KEY_F12 -> {
                showStatus(action.title)
            }

            // Text tools
            GamepadAction.SPACE -> appendCharacter(" ")
            GamepadAction.BACKSPACE -> backspace()
            GamepadAction.ENTER -> appendCharacter("\n")
            GamepadAction.TAB -> appendCharacter("    ")
            GamepadAction.SHIFT_TOGGLE -> toggleShift()
            GamepadAction.CAPS_LOCK -> toggleCaps()
            GamepadAction.CLEAR_ALL -> clearText()
            GamepadAction.COPY_CLIPBOARD -> copyToClipboard()
            GamepadAction.PASTE_CLIPBOARD -> pasteFromClipboard()
            GamepadAction.CUT_CLIPBOARD -> {
                copyToClipboard()
                clearText()
                showStatus("Cut to clipboard")
            }
            GamepadAction.UNDO -> showStatus("Undo action")
            GamepadAction.REDO -> showStatus("Redo action")
            GamepadAction.SELECT_ALL -> showStatus("Selected all text")

            // Media controls
            GamepadAction.MEDIA_PLAY_PAUSE -> showStatus("Media Play / Pause")
            GamepadAction.MEDIA_VOLUME_UP -> showStatus("Volume Up")
            GamepadAction.MEDIA_VOLUME_DOWN -> showStatus("Volume Down")
            GamepadAction.MEDIA_MUTE -> showStatus("Audio Muted")
            GamepadAction.MEDIA_NEXT -> showStatus("Next Track")
            GamepadAction.MEDIA_PREVIOUS -> showStatus("Previous Track")

            // Screen controls
            GamepadAction.SCREEN_CAPTURE -> showStatus("Screenshot captured")
            GamepadAction.TOGGLE_OVERLAY -> toggleOverlay()
            GamepadAction.CYCLE_MODE -> cycleMode()
            GamepadAction.SCREEN_OFF -> showStatus("Screen Off triggered")
            GamepadAction.BRIGHTNESS_UP -> showStatus("Brightness Up")
            GamepadAction.BRIGHTNESS_DOWN -> showStatus("Brightness Down")

            // Navigation controls
            GamepadAction.NAV_UP -> showStatus("Arrow Up")
            GamepadAction.NAV_DOWN -> showStatus("Arrow Down")
            GamepadAction.NAV_LEFT -> showStatus("Arrow Left")
            GamepadAction.NAV_RIGHT -> showStatus("Arrow Right")
            GamepadAction.NAV_ESCAPE -> showStatus("Escape")

            // Cursor & Mouse
            GamepadAction.MOUSE_LEFT_CLICK -> _isLeftMouseDown.value = true
            GamepadAction.MOUSE_RIGHT_CLICK -> _isRightMouseDown.value = true
            GamepadAction.MOUSE_MIDDLE_CLICK -> showStatus("Middle Click")
            GamepadAction.MOUSE_DOUBLE_CLICK -> showStatus("Double Click")
            GamepadAction.MOUSE_DRAG_LOCK -> {
                _isDragLocked.value = !_isDragLocked.value
                _isLeftMouseDown.value = _isDragLocked.value
                showStatus(if (_isDragLocked.value) "Drag Lock: ON" else "Drag Lock: OFF")
            }
            GamepadAction.MOUSE_SCROLL_CLICK -> showStatus("Scroll Click")
            GamepadAction.BROWSER_BACK -> showStatus("Browser Back")
            GamepadAction.BROWSER_FORWARD -> showStatus("Browser Forward")
            GamepadAction.SCROLL_UP_ACTION -> _scrollOffset.value = (_scrollOffset.value - 20f).coerceIn(-1000f, 1000f)
            GamepadAction.SCROLL_DOWN_ACTION -> _scrollOffset.value = (_scrollOffset.value + 20f).coerceIn(-1000f, 1000f)
            GamepadAction.ZOOM_IN_ACTION -> _zoomLevel.value = (_zoomLevel.value + 0.1f).coerceIn(0.5f, 3.0f)
            GamepadAction.ZOOM_OUT_ACTION -> _zoomLevel.value = (_zoomLevel.value - 0.1f).coerceIn(0.5f, 3.0f)
        }
    }

    // ==========================================
    // TEXT EDITING ACTIONS
    // ==========================================
    fun appendCharacter(char: String) {
        _typedText.value += char
    }

    fun backspace() {
        if (_typedText.value.isNotEmpty()) {
            _typedText.value = _typedText.value.dropLast(1)
            triggerHaptic(25)
        }
    }

    fun clearText() {
        _typedText.value = ""
        triggerHaptic(30)
        showStatus("Text Cleared")
    }

    fun toggleShift() {
        _isShiftActive.value = !_isShiftActive.value
        showStatus(if (_isShiftActive.value) "Shift: ON" else "Shift: OFF")
    }

    fun toggleCaps() {
        _isCapsLocked.value = !_isCapsLocked.value
        showStatus(if (_isCapsLocked.value) "Caps Lock: ON" else "Caps Lock: OFF")
    }

    fun copyToClipboard() {
        val text = _typedText.value
        if (text.isNotBlank()) {
            val clip = ClipData.newPlainText("FlickPad Text", text)
            clipboardManager?.setPrimaryClip(clip)
            showStatus("Copied to clipboard!")
        } else {
            showStatus("Text field is empty")
        }
    }

    fun pasteFromClipboard() {
        val clip = clipboardManager?.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val pasteText = clip.getItemAt(0).text?.toString() ?: ""
            if (pasteText.isNotBlank()) {
                appendCharacter(pasteText)
                showStatus("Pasted text!")
            }
        }
    }

    // ==========================================
    // MODE & OVERLAY (AUTOMATIC SPAWN & TIMEOUT)
    // ==========================================
    fun setMode(mode: String) {
        _activeMode.value = mode
        if (mode == "TEXT_INPUT") {
            _overlayVisible.value = true
            resetAutoHideTimer()
        } else if (mode == "OFF") {
            _overlayVisible.value = false
        }
        val current = _currentProfile.value
        if (current != null && current.mode != mode) {
            viewModelScope.launch {
                val updated = current.copy(mode = mode)
                repository.updateProfile(updated)
                _currentProfile.value = updated
            }
        }
        showStatus("Mode: $mode")
    }

    fun cycleMode() {
        val nextMode = when (_activeMode.value) {
            "OFF" -> "TEXT_INPUT"
            "TEXT_INPUT" -> "CURSOR_CONTROL"
            "CURSOR_CONTROL" -> "OFF"
            else -> "TEXT_INPUT"
        }
        setMode(nextMode)
    }

    fun toggleOverlay() {
        _overlayVisible.value = !_overlayVisible.value
        showStatus(if (_overlayVisible.value) "Overlay HUD: Visible" else "Overlay HUD: Hidden")
    }

    // ==========================================
    // KEYBOARD LAYOUTS (K1..K16) & MODIFIERS
    // ==========================================
    fun selectLayout(layoutId: String) {
        _activeLayoutId.value = layoutId
        val layout = _allLayouts.value.firstOrNull { it.id == layoutId } ?: return
        _parsedZones.value = layout.getZonesForModifier(_activeModifierLayer.value)

        val current = _currentProfile.value ?: return
        val updated = current.copy(activeLayoutId = layoutId)
        _currentProfile.value = updated
        viewModelScope.launch {
            repository.updateProfile(updated)
            showStatus("Active Layout: ${layout.name} (${layout.id})")
        }
    }

    fun activateLayout(layoutId: String) {
        selectLayout(layoutId)
    }

    fun cycleLayouts() {
        val list = _allLayouts.value
        if (list.isEmpty()) return
        val currentIdx = list.indexOfFirst { it.id == _activeLayoutId.value }
        val nextIdx = if (currentIdx >= 0) (currentIdx + 1) % list.size else 0
        val nextLayout = list[nextIdx]
        selectLayout(nextLayout.id)
    }

    fun setModifierLayer(modifier: String) {
        // Toggle if already selected, otherwise switch to it
        val targetMod = if (_activeModifierLayer.value == modifier && modifier != "Base") "Base" else modifier
        _activeModifierLayer.value = targetMod

        val activeLayout = _allLayouts.value.firstOrNull { it.id == _activeLayoutId.value } ?: _allLayouts.value.firstOrNull()
        if (activeLayout != null) {
            _parsedZones.value = activeLayout.getZonesForModifier(targetMod)
        }
        showStatus("Layer: $targetMod")
    }

    fun addNewLayout(name: String = "") {
        val existing = _allLayouts.value.toMutableList()
        val count = existing.size + 1
        val newId = if (count <= 16) "K$count" else "K${System.currentTimeMillis() % 1000}"
        val newName = if (name.isNotBlank()) name else "Layout $newId"

        val baseZones = DefaultProfiles.jsonToZones(DefaultProfiles.getDefaultZonesJson())
        val newLayout = KeyboardLayout(
            id = newId,
            name = newName,
            layers = mapOf("Base" to baseZones)
        )
        existing.add(newLayout)
        _allLayouts.value = existing
        selectLayout(newId)
        saveLayoutsToProfile()
        showStatus("Created layout: $newName ($newId)")
    }

    fun renameLayout(layoutId: String, newName: String) {
        val existing = _allLayouts.value.map {
            if (it.id == layoutId) it.copy(name = newName) else it
        }
        _allLayouts.value = existing
        saveLayoutsToProfile()
        showStatus("Renamed layout $layoutId to '$newName'")
    }

    fun exportLayoutsJson(): String {
        return KeyboardLayout.layoutsToJson(_allLayouts.value)
    }

    fun importLayoutsJson(jsonStr: String): Boolean {
        return try {
            val imported = KeyboardLayout.jsonToLayouts(jsonStr)
            if (imported.isNotEmpty()) {
                _allLayouts.value = imported
                selectLayout(imported.first().id)
                saveLayoutsToProfile()
                showStatus("Imported ${imported.size} layouts successfully!")
                true
            } else false
        } catch (e: Exception) {
            showStatus("Failed to import layouts: ${e.localizedMessage}")
            false
        }
    }

    private fun saveLayoutsToProfile() {
        val current = _currentProfile.value ?: return
        val json = KeyboardLayout.layoutsToJson(_allLayouts.value)
        val updated = current.copy(
            layoutsJson = json,
            activeLayoutId = _activeLayoutId.value
        )
        _currentProfile.value = updated
        viewModelScope.launch { repository.updateProfile(updated) }
    }

    // ==========================================
    // ZONE & KEY CUSTOMIZATION
    // ==========================================
    fun updateZoneSymbol(zoneIndex: Int, keyIndex: Int, newSymbol: String) {
        val currentZones = _parsedZones.value.toMutableList()
        val zone = currentZones.firstOrNull { it.index == zoneIndex } ?: return
        val updatedSymbols = zone.symbols.toMutableList()
        if (keyIndex in updatedSymbols.indices) {
            updatedSymbols[keyIndex] = newSymbol
            val updatedZone = zone.copy(symbols = updatedSymbols)
            val idxInList = currentZones.indexOfFirst { it.index == zoneIndex }
            if (idxInList >= 0) {
                currentZones[idxInList] = updatedZone
                _parsedZones.value = currentZones

                // Save into the active layout's currently selected modifier layer!
                val activeId = _activeLayoutId.value
                val modLayer = _activeModifierLayer.value
                val updatedLayouts = _allLayouts.value.map { layout ->
                    if (layout.id == activeId) {
                        layout.withUpdatedZones(modLayer, currentZones)
                    } else layout
                }
                _allLayouts.value = updatedLayouts

                val current = _currentProfile.value ?: return
                val updatedLayoutsJson = KeyboardLayout.layoutsToJson(updatedLayouts)
                val updatedZonesJson = DefaultProfiles.zonesToJson(currentZones)
                val updatedProf = current.copy(
                    zonesJson = updatedZonesJson,
                    layoutsJson = updatedLayoutsJson
                )
                _currentProfile.value = updatedProf
                viewModelScope.launch {
                    repository.updateProfile(updatedProf)
                    showStatus("Saved key '$newSymbol' to $activeId ($modLayer)")
                }
            }
        }
    }

    // ==========================================
    // BUTTON COMBINATIONS (TEXT INPUT & CURSOR)
    // ==========================================
    fun updateButtonCombination(combo: ButtonCombination, isCursor: Boolean = false) {
        val current = _currentProfile.value ?: return
        if (isCursor) {
            val list = _cursorCombinations.value.map { if (it.id == combo.id) combo else it }
            _cursorCombinations.value = list
            val json = ButtonCombination.combinationsToJson(list)
            val updated = current.copy(cursorCombinationsJson = json)
            _currentProfile.value = updated
            viewModelScope.launch { repository.updateProfile(updated) }
        } else {
            val list = _buttonCombinations.value.map { if (it.id == combo.id) combo else it }
            _buttonCombinations.value = list
            val json = ButtonCombination.combinationsToJson(list)
            val updated = current.copy(buttonCombinationsJson = json)
            _currentProfile.value = updated
            viewModelScope.launch { repository.updateProfile(updated) }
        }
    }

    fun addCombination(isCursor: Boolean = false) {
        val newCombo = ButtonCombination(
            buttons = listOf("KEYCODE_BUTTON_START", "KEYCODE_BUTTON_Y"),
            actionCode = "MEDIA_VOLUME_UP"
        )
        val current = _currentProfile.value ?: return
        if (isCursor) {
            val list = _cursorCombinations.value + newCombo
            _cursorCombinations.value = list
            val json = ButtonCombination.combinationsToJson(list)
            val updated = current.copy(cursorCombinationsJson = json)
            _currentProfile.value = updated
            viewModelScope.launch { repository.updateProfile(updated) }
        } else {
            val list = _buttonCombinations.value + newCombo
            _buttonCombinations.value = list
            val json = ButtonCombination.combinationsToJson(list)
            val updated = current.copy(buttonCombinationsJson = json)
            _currentProfile.value = updated
            viewModelScope.launch { repository.updateProfile(updated) }
        }
        showStatus("Combination added")
    }

    fun deleteCombination(id: String, isCursor: Boolean = false) {
        val current = _currentProfile.value ?: return
        if (isCursor) {
            val list = _cursorCombinations.value.filter { it.id != id }
            _cursorCombinations.value = list
            val json = ButtonCombination.combinationsToJson(list)
            val updated = current.copy(cursorCombinationsJson = json)
            _currentProfile.value = updated
            viewModelScope.launch { repository.updateProfile(updated) }
        } else {
            val list = _buttonCombinations.value.filter { it.id != id }
            _buttonCombinations.value = list
            val json = ButtonCombination.combinationsToJson(list)
            val updated = current.copy(buttonCombinationsJson = json)
            _currentProfile.value = updated
            viewModelScope.launch { repository.updateProfile(updated) }
        }
        showStatus("Combination removed")
    }

    // ==========================================
    // DEADZONE & SENSITIVITY
    // ==========================================
    fun updateDeadzoneAndSens(
        leftDeadzone: Float,
        leftSens: Float,
        rightDeadzone: Float,
        rightSens: Float
    ) {
        val current = _currentProfile.value ?: return
        val updated = current.copy(
            leftDeadzone = leftDeadzone,
            leftSensitivity = leftSens,
            rightDeadzone = rightDeadzone,
            rightSensitivity = rightSens
        )
        _currentProfile.value = updated
        viewModelScope.launch {
            repository.updateProfile(updated)
        }
    }

    // ==========================================
    // CURSOR CONTROL SETTINGS
    // ==========================================
    fun updateCursorSpeedAndCurve(sensitivity: Float, accelerationCurve: String) {
        val current = _currentProfile.value ?: return
        val updated = current.copy(
            mouseSensitivity = sensitivity,
            mouseAccelerationCurve = accelerationCurve
        )
        _currentProfile.value = updated
        viewModelScope.launch {
            repository.updateProfile(updated)
        }
    }

    fun setStickMoveCursor(isLeft: Boolean, moveCursor: Boolean) {
        val current = _currentProfile.value ?: return
        val updated = if (isLeft) {
            current.copy(leftStickMoveCursor = moveCursor)
        } else {
            current.copy(rightStickMoveCursor = moveCursor)
        }
        _currentProfile.value = updated
        viewModelScope.launch {
            repository.updateProfile(updated)
            showStatus("${if (isLeft) "Left" else "Right"} Stick Move Cursor: ${if (moveCursor) "ON" else "OFF"}")
        }
    }

    fun updateStickRadialAction(isLeft: Boolean, directionIndex: Int, action: String) {
        val current = _currentProfile.value ?: return
        if (isLeft) {
            val list = _leftStickRadialActions.value.toMutableList()
            if (directionIndex in list.indices) {
                list[directionIndex] = action
                _leftStickRadialActions.value = list
                val json = DefaultProfiles.radialActionsToJson(list)
                val updated = current.copy(leftStickRadialActionsJson = json)
                _currentProfile.value = updated
                viewModelScope.launch { repository.updateProfile(updated) }
            }
        } else {
            val list = _rightStickRadialActions.value.toMutableList()
            if (directionIndex in list.indices) {
                list[directionIndex] = action
                _rightStickRadialActions.value = list
                val json = DefaultProfiles.radialActionsToJson(list)
                val updated = current.copy(rightStickRadialActionsJson = json)
                _currentProfile.value = updated
                viewModelScope.launch { repository.updateProfile(updated) }
            }
        }
    }

    fun updateCursorButtonMapping(keyCodeName: String, actionCode: String) {
        val currentMappings = _parsedCursorMappings.value.toMutableMap()
        currentMappings[keyCodeName] = actionCode
        _parsedCursorMappings.value = currentMappings

        val current = _currentProfile.value ?: return
        val updatedJson = DefaultProfiles.mappingsToJson(currentMappings)
        val updatedProf = current.copy(cursorMappingsJson = updatedJson)
        _currentProfile.value = updatedProf
        viewModelScope.launch {
            repository.updateProfile(updatedProf)
            showStatus("Mapped $keyCodeName -> $actionCode (Cursor Mode)")
        }
    }

    // ==========================================
    // OVERLAY SETTINGS
    // ==========================================
    fun updateOverlaySettings(
        scale: Float,
        opacity: Float,
        autoHideSeconds: Float,
        theme: String
    ) {
        val current = _currentProfile.value ?: return
        val updated = current.copy(
            overlayScale = scale,
            overlayOpacity = opacity,
            overlayAutoHideSeconds = autoHideSeconds,
            overlayTheme = theme
        )
        _currentProfile.value = updated
        resetAutoHideTimer()
        viewModelScope.launch {
            repository.updateProfile(updated)
            showStatus("Overlay appearance updated")
        }
    }

    // ==========================================
    // BUTTON MAPPING
    // ==========================================
    fun updateButtonMapping(keyCodeName: String, actionCode: String) {
        val currentMappings = _parsedMappings.value.toMutableMap()
        currentMappings[keyCodeName] = actionCode
        _parsedMappings.value = currentMappings

        val current = _currentProfile.value ?: return
        val updatedJson = DefaultProfiles.mappingsToJson(currentMappings)
        val updatedProf = current.copy(mappingsJson = updatedJson)
        _currentProfile.value = updatedProf
        viewModelScope.launch {
            repository.updateProfile(updatedProf)
            showStatus("Mapped $keyCodeName -> $actionCode")
        }
    }

    // ==========================================
    // PROFILE MANAGEMENT
    // ==========================================
    fun createProfile(name: String, description: String) {
        viewModelScope.launch {
            val base = _currentProfile.value
            val newProfile = ProfileEntity(
                name = name,
                description = description,
                isDefault = false,
                mode = _activeMode.value,
                leftDeadzone = base?.leftDeadzone ?: 0.22f,
                leftSensitivity = base?.leftSensitivity ?: 1.0f,
                rightDeadzone = base?.rightDeadzone ?: 0.22f,
                rightSensitivity = base?.rightSensitivity ?: 1.0f,
                mouseSensitivity = base?.mouseSensitivity ?: 1.5f,
                mouseAccelerationCurve = base?.mouseAccelerationCurve ?: "EXPONENTIAL",
                leftStickMoveCursor = base?.leftStickMoveCursor ?: true,
                rightStickMoveCursor = base?.rightStickMoveCursor ?: false,
                leftStickRadialActionsJson = base?.leftStickRadialActionsJson ?: DefaultProfiles.radialActionsToJson(DefaultProfiles.getDefaultRadialStickActions()),
                rightStickRadialActionsJson = base?.rightStickRadialActionsJson ?: DefaultProfiles.radialActionsToJson(DefaultProfiles.getDefaultRadialStickActions()),
                cursorMappingsJson = base?.cursorMappingsJson ?: DefaultProfiles.getDefaultCursorMappingsJson(),
                cursorCombinationsJson = base?.cursorCombinationsJson ?: ButtonCombination.combinationsToJson(ButtonCombination.getDefaultCombinations()),
                overlayScale = base?.overlayScale ?: 1.0f,
                overlayOpacity = base?.overlayOpacity ?: 0.90f,
                overlayPosX = base?.overlayPosX ?: 0.5f,
                overlayPosY = base?.overlayPosY ?: 0.65f,
                overlayAutoHideSeconds = base?.overlayAutoHideSeconds ?: 0f,
                overlayTheme = base?.overlayTheme ?: "CYBERPUNK",
                shortcutTextInput = base?.shortcutTextInput ?: "L3 + R3",
                shortcutOverlay = base?.shortcutOverlay ?: "BACK + START",
                shortcutCursor = base?.shortcutCursor ?: "L1 + R1",
                activeLayoutId = base?.activeLayoutId ?: "K1",
                layoutsJson = base?.layoutsJson ?: KeyboardLayout.layoutsToJson(KeyboardLayout.createDefaultLayouts()),
                buttonCombinationsJson = base?.buttonCombinationsJson ?: ButtonCombination.combinationsToJson(ButtonCombination.getDefaultCombinations()),
                zonesJson = base?.zonesJson ?: DefaultProfiles.getDefaultZonesJson(),
                mappingsJson = base?.mappingsJson ?: DefaultProfiles.getDefaultMappingsJson()
            )
            val id = repository.insertProfile(newProfile)
            val inserted = repository.getProfileById(id)
            if (inserted != null) {
                loadProfile(inserted)
                showStatus("Created profile: $name")
            }
        }
    }

    fun duplicateCurrentProfile() {
        val current = _currentProfile.value ?: return
        createProfile("${current.name} (Copy)", "Copy of ${current.name}")
    }

    fun deleteProfile(profile: ProfileEntity) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
            showStatus("Deleted profile: ${profile.name}")
            val remaining = repository.getDefaultProfile()
            if (remaining != null) {
                loadProfile(remaining)
            }
        }
    }

    fun setAsDefaultProfile(profile: ProfileEntity) {
        viewModelScope.launch {
            repository.setDefaultProfile(profile.id)
            _currentProfile.value = profile.copy(isDefault = true)
            showStatus("Set as default: ${profile.name}")
        }
    }

    // ==========================================
    // IMPORT & EXPORT
    // ==========================================
    fun exportCurrentProfileJson(): String {
        val profile = _currentProfile.value ?: return "{}"
        return repository.exportProfileToJson(profile)
    }

    fun importProfileFromJson(jsonText: String): Boolean {
        return try {
            val imported = repository.importProfileFromJson(jsonText)
            viewModelScope.launch {
                val id = repository.insertProfile(imported)
                val saved = repository.getProfileById(id)
                if (saved != null) {
                    loadProfile(saved)
                    showStatus("Profile imported: ${saved.name}")
                }
            }
            true
        } catch (e: Exception) {
            showStatus("Failed to import JSON: ${e.localizedMessage}")
            false
        }
    }
}
