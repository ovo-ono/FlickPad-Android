package com.example.model

enum class GamepadAction(val code: String, val title: String, val category: String) {
    // None
    NONE("NONE", "None", "General"),

    // Layout Switching
    CYCLE_LAYOUTS("CYCLE_LAYOUTS", "Cycle Layouts", "Layout"),
    LAYOUT_K1("LAYOUT_K1", "Layout K1", "Layout"),
    LAYOUT_K2("LAYOUT_K2", "Layout K2", "Layout"),
    LAYOUT_K3("LAYOUT_K3", "Layout K3", "Layout"),
    LAYOUT_K4("LAYOUT_K4", "Layout K4", "Layout"),
    LAYOUT_K5("LAYOUT_K5", "Layout K5", "Layout"),
    LAYOUT_K6("LAYOUT_K6", "Layout K6", "Layout"),
    LAYOUT_K7("LAYOUT_K7", "Layout K7", "Layout"),
    LAYOUT_K8("LAYOUT_K8", "Layout K8", "Layout"),
    LAYOUT_K9("LAYOUT_K9", "Layout K9", "Layout"),
    LAYOUT_K10("LAYOUT_K10", "Layout K10", "Layout"),
    LAYOUT_K11("LAYOUT_K11", "Layout K11", "Layout"),
    LAYOUT_K12("LAYOUT_K12", "Layout K12", "Layout"),
    LAYOUT_K13("LAYOUT_K13", "Layout K13", "Layout"),
    LAYOUT_K14("LAYOUT_K14", "Layout K14", "Layout"),
    LAYOUT_K15("LAYOUT_K15", "Layout K15", "Layout"),
    LAYOUT_K16("LAYOUT_K16", "Layout K16", "Layout"),

    // Standard Keyboard Function & Control Keys
    KEY_ESC("KEY_ESC", "Esc", "Keyboard"),
    KEY_TAB("KEY_TAB", "Tab", "Keyboard"),
    KEY_CAPS_LOCK("KEY_CAPS_LOCK", "Caps Lock", "Keyboard"),
    KEY_SHIFT("KEY_SHIFT", "Shift", "Keyboard"),
    KEY_CTRL("KEY_CTRL", "Ctrl", "Keyboard"),
    KEY_ALT("KEY_ALT", "Alt", "Keyboard"),
    KEY_WIN("KEY_WIN", "Win / Meta", "Keyboard"),
    KEY_SPACE("KEY_SPACE", "Space", "Keyboard"),
    KEY_ENTER("KEY_ENTER", "Enter", "Keyboard"),
    KEY_BACKSPACE("KEY_BACKSPACE", "Backspace", "Keyboard"),
    KEY_DELETE("KEY_DELETE", "Delete", "Keyboard"),
    KEY_INSERT("KEY_INSERT", "Insert", "Keyboard"),
    KEY_HOME("KEY_HOME", "Home", "Keyboard"),
    KEY_END("KEY_END", "End", "Keyboard"),
    KEY_PAGE_UP("KEY_PAGE_UP", "Page Up", "Keyboard"),
    KEY_PAGE_DOWN("KEY_PAGE_DOWN", "Page Down", "Keyboard"),
    KEY_PRINTSCREEN("KEY_PRINTSCREEN", "Print Screen", "Keyboard"),
    KEY_SCROLL_LOCK("KEY_SCROLL_LOCK", "Scroll Lock", "Keyboard"),
    KEY_PAUSE("KEY_PAUSE", "Pause / Break", "Keyboard"),

    // Function Keys F1 - F12
    KEY_F1("KEY_F1", "F1", "Function Keys"),
    KEY_F2("KEY_F2", "F2", "Function Keys"),
    KEY_F3("KEY_F3", "F3", "Function Keys"),
    KEY_F4("KEY_F4", "F4", "Function Keys"),
    KEY_F5("KEY_F5", "F5", "Function Keys"),
    KEY_F6("KEY_F6", "F6", "Function Keys"),
    KEY_F7("KEY_F7", "F7", "Function Keys"),
    KEY_F8("KEY_F8", "F8", "Function Keys"),
    KEY_F9("KEY_F9", "F9", "Function Keys"),
    KEY_F10("KEY_F10", "F10", "Function Keys"),
    KEY_F11("KEY_F11", "F11", "Function Keys"),
    KEY_F12("KEY_F12", "F12", "Function Keys"),

    // Text Tools
    SPACE("SPACE", "Spacebar", "Text Tools"),
    BACKSPACE("BACKSPACE", "Backspace", "Text Tools"),
    ENTER("ENTER", "Enter / Send", "Text Tools"),
    SHIFT_TOGGLE("SHIFT_TOGGLE", "Shift Toggle", "Text Tools"),
    CAPS_LOCK("CAPS_LOCK", "Caps Lock", "Text Tools"),
    TAB("TAB", "Tab Key", "Text Tools"),
    CLEAR_ALL("CLEAR_ALL", "Clear Text", "Text Tools"),
    COPY_CLIPBOARD("COPY_CLIPBOARD", "Copy", "Text Tools"),
    PASTE_CLIPBOARD("PASTE_CLIPBOARD", "Paste", "Text Tools"),
    CUT_CLIPBOARD("CUT_CLIPBOARD", "Cut", "Text Tools"),
    UNDO("UNDO", "Undo", "Text Tools"),
    REDO("REDO", "Redo", "Text Tools"),
    SELECT_ALL("SELECT_ALL", "Select All", "Text Tools"),

    // Media Controls
    MEDIA_PLAY_PAUSE("MEDIA_PLAY_PAUSE", "Play / Pause", "Media Controls"),
    MEDIA_VOLUME_UP("MEDIA_VOLUME_UP", "Volume Up", "Media Controls"),
    MEDIA_VOLUME_DOWN("MEDIA_VOLUME_DOWN", "Volume Down", "Media Controls"),
    MEDIA_MUTE("MEDIA_MUTE", "Mute Audio", "Media Controls"),
    MEDIA_NEXT("MEDIA_NEXT", "Next Track", "Media Controls"),
    MEDIA_PREVIOUS("MEDIA_PREVIOUS", "Previous Track", "Media Controls"),

    // Screen Controls
    SCREEN_CAPTURE("SCREEN_CAPTURE", "Screenshot", "Screen Controls"),
    TOGGLE_OVERLAY("TOGGLE_OVERLAY", "Toggle Overlay", "Screen Controls"),
    CYCLE_MODE("CYCLE_MODE", "Cycle Mode", "Screen Controls"),
    SCREEN_OFF("SCREEN_OFF", "Screen Off", "Screen Controls"),
    BRIGHTNESS_UP("BRIGHTNESS_UP", "Brightness Up", "Screen Controls"),
    BRIGHTNESS_DOWN("BRIGHTNESS_DOWN", "Brightness Down", "Screen Controls"),

    // Navigation Controls
    NAV_UP("NAV_UP", "Arrow Up", "Navigation"),
    NAV_DOWN("NAV_DOWN", "Arrow Down", "Navigation"),
    NAV_LEFT("NAV_LEFT", "Arrow Left", "Navigation"),
    NAV_RIGHT("NAV_RIGHT", "Arrow Right", "Navigation"),
    NAV_ESCAPE("NAV_ESCAPE", "Escape", "Navigation"),

    // Cursor & Mouse Actions
    MOUSE_LEFT_CLICK("MOUSE_LEFT_CLICK", "Left Click", "Cursor & Mouse"),
    MOUSE_RIGHT_CLICK("MOUSE_RIGHT_CLICK", "Right Click", "Cursor & Mouse"),
    MOUSE_MIDDLE_CLICK("MOUSE_MIDDLE_CLICK", "Middle Click", "Cursor & Mouse"),
    MOUSE_DOUBLE_CLICK("MOUSE_DOUBLE_CLICK", "Double Click", "Cursor & Mouse"),
    MOUSE_DRAG_LOCK("MOUSE_DRAG_LOCK", "Drag Lock", "Cursor & Mouse"),
    MOUSE_SCROLL_CLICK("MOUSE_SCROLL_CLICK", "Scroll Click", "Cursor & Mouse"),
    BROWSER_BACK("BROWSER_BACK", "Browser Back", "Cursor & Mouse"),
    BROWSER_FORWARD("BROWSER_FORWARD", "Browser Forward", "Cursor & Mouse"),
    SCROLL_UP_ACTION("SCROLL_UP_ACTION", "Scroll Up", "Cursor & Mouse"),
    SCROLL_DOWN_ACTION("SCROLL_DOWN_ACTION", "Scroll Down", "Cursor & Mouse"),
    ZOOM_IN_ACTION("ZOOM_IN_ACTION", "Zoom In", "Cursor & Mouse"),
    ZOOM_OUT_ACTION("ZOOM_OUT_ACTION", "Zoom Out", "Cursor & Mouse");

    companion object {
        fun fromCode(code: String): GamepadAction {
            return entries.firstOrNull { it.code == code } ?: NONE
        }
    }
}

data class GamepadButtonInfo(
    val keyCodeName: String,
    val displayName: String,
    val iconDescription: String
)

object GamepadButtons {
    // Equal-size PlayStation style button symbols (✕, ○, □, △) matched with Xbox (A, B, X, Y).
    // Renamed Back / Select to Select, and Start / Options to Start.
    val ALL_BUTTONS = listOf(
        GamepadButtonInfo("KEYCODE_BUTTON_A", "A / ✕", ""),
        GamepadButtonInfo("KEYCODE_BUTTON_B", "B / ○", ""),
        GamepadButtonInfo("KEYCODE_BUTTON_X", "X / □", ""),
        GamepadButtonInfo("KEYCODE_BUTTON_Y", "Y / △", ""),
        GamepadButtonInfo("KEYCODE_BUTTON_L1", "LB / L1", ""),
        GamepadButtonInfo("KEYCODE_BUTTON_R1", "RB / R1", ""),
        GamepadButtonInfo("KEYCODE_BUTTON_L2", "LT / L2", ""),
        GamepadButtonInfo("KEYCODE_BUTTON_R2", "RT / R2", ""),
        GamepadButtonInfo("KEYCODE_BUTTON_THUMBL", "L3", ""),
        GamepadButtonInfo("KEYCODE_BUTTON_THUMBR", "R3", ""),
        GamepadButtonInfo("KEYCODE_BUTTON_SELECT", "Select", ""),
        GamepadButtonInfo("KEYCODE_BUTTON_START", "Start", ""),
        GamepadButtonInfo("KEYCODE_DPAD_UP", "D-Pad Up", ""),
        GamepadButtonInfo("KEYCODE_DPAD_DOWN", "D-Pad Down", ""),
        GamepadButtonInfo("KEYCODE_DPAD_LEFT", "D-Pad Left", ""),
        GamepadButtonInfo("KEYCODE_DPAD_RIGHT", "D-Pad Right", "")
    )
}
