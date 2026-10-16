# Persistent Floating Overlay, Global Background Input & System-Wide Cursor Control

Enables FlickPad as a true system-wide background gaming and input service, persisting the Floating Overlay across all Android apps according to settings, capturing gamepad shortcuts globally via Accessibility Service key filtering, providing full-screen mouse cursor navigation with face button click/drag, and showing a top-center styled mode banner.

### User Review & Critical Decisions

> [!IMPORTANT]
> Based on your confirmed preferences in Phase 1:
> - **Floating Overlay Persistence**: When Text Input mode is active, the Floating Overlay keyboard is visible and persists over any open application. It automatically respects the configured timeout (`overlayAutoHideSeconds`) or stays indefinitely if set to 0. It instantly awakens upon stick movement or gamepad hotkeys.
> - **Global Gamepad Interception in Any App**: By configuring `FlickPadAccessibilityService` with `flagRequestFilterKeyEvents`, gamepad hotkeys (such as L3+R3, mode switches, and face buttons) are captured globally across the entire operating system, allowing instant mode switching without needing the main app open.
> - **Full-Screen System Cursor**: A precision mouse pointer traverses 100% of the display, including the status bar. Face buttons (A/B/X/Y) act as mouse buttons where holding a button holds the mouse button down for dragging rather than spamming clicks. Clicking the status bar area pulls down the notification shade.
> - **Mode Change Banner**: A brief, stylized top-center floating pill banner displaying `[off]`, `[text_input]`, or `[cursor]` appears only when the input mode transitions.

---

### 1. Overview & Core Concept

FlickPad's primary mission is to serve as an in-game and system-wide radial text input and cursor utility. The Android application GUI is the configuration hub; the core engine runs unobtrusively in the background via:
1. **FlickPadOverlayService (Foreground Service)**: Maintains system overlay windows for the floating keyboard HUD, draggable badge, mode toast, and precision cursor.
2. **FlickPadAccessibilityService (Global Input & Gesture Service)**: Intercepts gamepad key events across all applications, dispatches real touch clicks/drags at the cursor position, and interacts with system UI (status bar pull-down, Back, Home).
3. **FlickPadInputMethodService (IME)**: Injects text directly into in-game chat boxes, search bars, and text fields via `InputConnection`.

---

### 2. User Experience & Visual Design

#### 2.1 Outside-App Background Architecture
```
┌────────────────────────────────────────────────────────────────────────┐
│                   ANY GAME / APP / ANDROID LAUNCHER                    │
│                                                                        │
│   [Gamepad Shortcut: L3 + R3] ───► [Accessibility Key Filter]          │
│                                            │                           │
│                                            ▼                           │
│                     [Top-Center Banner: "[text_input]"]                │
│                                            │                           │
│                                            ▼                           │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │               PERSISTENT FLOATING OVERLAY HUD                  │   │
│   │  ⚡ FLICKPAD [K1: Default]                      [✕ MINIMIZE]   │   │
│   │ ┌───────────┐ ┌───────────┐ ┌───────────┐                      │   │
│   │ │ 3   x   4 │ │ (   u   ) │ │ 7   $   8 │                      │   │
│   │ ├───────────┤ ├───────────┤ ├───────────┤                      │   │
│   │ │ #   !   = │ │ i   e   s │ │ /   y   \ │  ◄── Left Stick      │   │
│   │ ├───────────┤ ├───────────┤ ├───────────┤      Selects Zone    │   │
│   │ │ ]   {   } │ │ @   b   & │ │ £   §   ° │  ◄── Right Stick     │   │
│   │ └───────────┘ └───────────┘ └───────────┘      Arms & Flicks   │   │
│   └────────────────────────────────────────────────────────────────┘   │
│                                    │                                   │
│                        (Flick Release / Space)                         │
│                                    ▼                                   │
│                           IN-GAME TEXT FIELD                           │
└────────────────────────────────────────────────────────────────────────┘
```

#### 2.2 System Cursor Mode
- **Status Bar & Whole-Display Reach**: Floating cursor window uses `FLAG_LAYOUT_NO_LIMITS`, `FLAG_LAYOUT_IN_SCREEN`, and `LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES`, permitting free travel from $(0, 0)$ across cutouts, status bar, and navigation bar.
- **Mouse Face Buttons**:
  - `BUTTON_A`: Left click. Holding maintains mouse-down state for continuous selection or drag-and-drop. Cursor arrow illuminates in bright Cyan.
  - `BUTTON_B`: Right click / System Back. Cursor illuminates in Amber.
  - `Top Edge Interaction`: Left clicking or deflecting upward into the status bar area ($y \le 60\text{px}$) triggers notification shade expansion (`GLOBAL_ACTION_NOTIFICATIONS`).

#### 2.3 Top-Center Mode Notification Pill
- Appears at `Gravity.TOP or Gravity.CENTER_HORIZONTAL` with a brief 1.2s timeout.
- Aesthetic matches the terminal preview toggles: pure black background, 2dp `TerminalGreen` border, monospace green bold text: `[off]`, `[text_input]`, or `[cursor]`.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Global Key Capture via AccessibilityService vs. IME-only capture**
  - *Chosen Approach*: Add `flagRequestFilterKeyEvents` and `onKeyEvent()` to `FlickPadAccessibilityService`.
  - *Why*: In Android, when a user is playing a full-screen game without a text field currently focused, an IME receives no key events. The Accessibility Service allows gamepad shortcuts (like switching to text input or cursor mode) to be detected from any game or app.
- **Decision 2: Automatic Background Service Lifecycle**
  - *Chosen Approach*: Launch `FlickPadOverlayService` automatically when the application starts if overlay permission is granted, and keep it active in the background. Provide a clear permission status indicator in settings.
  - *Why*: Ensures seamless background utility immediately after installation or launch, matching user expectations for an input controller.
- **Decision 3: Overlay Persistence with Configurable Auto-Hide**
  - *Chosen Approach*: When mode is `TEXT_INPUT`, the Floating Overlay window is explicitly shown (`View.VISIBLE`). If `overlayAutoHideSeconds > 0`, it smoothly fades/hides after the specified seconds of idle time and immediately wakes up on stick deflection or gamepad input. If set to `0`, it remains perpetually docked on screen.

---

### 4. Technical Architecture & Component Structure

```
┌────────────────────────────────────────────────────────────────────────┐
│                        FLICKPAD ARCHITECTURE                           │
│                                                                        │
│   ┌──────────────────────────────────────────────────────────────┐     │
│   │                 FlickPadAccessibilityService                 │     │
│   │ - Intercepts gamepad hotkeys globally across all apps        │     │
│   │ - Dispatches touch gestures (clicks, holds, drags)           │     │
│   │ - Controls status bar (openNotifications) and navigation     │     │
│   └──────────────────────────────┬───────────────────────────────┘     │
│                                  │                                     │
│                                  ▼                                     │
│   ┌──────────────────────────────────────────────────────────────┐     │
│   │                      FlickPadViewModel                       │     │
│   │ - Manages activeMode ("OFF", "TEXT_INPUT", "CURSOR_CONTROL") │     │
│   │ - Dispatches mode change toasts, stick coords, armed sectors │     │
│   │ - Auto-starts background overlay service                     │     │
│   └───────────────┬──────────────────────────────┬───────────────┘     │
│                   │                              │                     │
│                   ▼                              ▼                     │
│   ┌──────────────────────────────┐ ┌─────────────────────────────────┐ │
│   │    FlickPadOverlayService    │ │   FlickPadInputMethodService    │ │
│   │ - Persistent Floating HUD    │ │ - Direct commitText injection   │ │
│   │ - Full-screen cursor overlay │ │ - Backspace & Enter keys        │ │
│   │ - Top-center mode toasts     │ │ - Zero-crash native IME view    │ │
│   └──────────────────────────────┘ └─────────────────────────────────┘ │
└────────────────────────────────────────────────────────────────────────┘
```

#### Verification Plan
1. **Compilation**: Run `compile_applet` to ensure zero compilation or resource errors.
2. **Unit Tests**: Run `gradle :app:testDebugUnitTest` to verify radial sector calculation, profile serialization, and engine algorithms.
3. **Outside-App Verification**: Verify that `FlickPadOverlayService` and `FlickPadAccessibilityService` are properly declared in `AndroidManifest.xml` with appropriate flags, permissions, and callbacks.
