package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.InputDevice
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.example.ui.FlickPadViewModel

class FlickPadAccessibilityService : AccessibilityService() {

    companion object {
        var activeInstance: FlickPadAccessibilityService? = null
            private set

        val isServiceRunning: Boolean
            get() = activeInstance != null

        fun clickAt(x: Float, y: Float) {
            val service = activeInstance ?: return
            val path = Path().apply {
                moveTo(x.coerceAtLeast(0f), y.coerceAtLeast(0f))
            }
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 75))
                .build()
            service.dispatchGesture(gesture, null, null)
        }

        fun drag(startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long = 200) {
            val service = activeInstance ?: return
            val path = Path().apply {
                moveTo(startX.coerceAtLeast(0f), startY.coerceAtLeast(0f))
                lineTo(endX.coerceAtLeast(0f), endY.coerceAtLeast(0f))
            }
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, durationMs))
                .build()
            service.dispatchGesture(gesture, null, null)
        }

        fun openNotifications(): Boolean {
            val service = activeInstance ?: return false
            return service.performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
        }

        fun openQuickSettings(): Boolean {
            val service = activeInstance ?: return false
            return service.performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
        }

        fun performBack(): Boolean {
            val service = activeInstance ?: return false
            return service.performGlobalAction(GLOBAL_ACTION_BACK)
        }

        fun performHome(): Boolean {
            val service = activeInstance ?: return false
            return service.performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        activeInstance = this
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val src = event.source
        val isGamepad = (src and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
                (src and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK ||
                KeyEvent.isGamepadButton(event.keyCode)

        if (isGamepad) {
            val vm = FlickPadViewModel.activeInstance
            if (vm != null) {
                return if (event.action == KeyEvent.ACTION_DOWN) {
                    vm.onGamepadKeyDown(event.keyCode)
                } else if (event.action == KeyEvent.ACTION_UP) {
                    vm.onGamepadKeyUp(event.keyCode)
                } else {
                    false
                }
            }
        }
        return super.onKeyEvent(event)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        if (activeInstance == this) {
            activeInstance = null
        }
    }
}
